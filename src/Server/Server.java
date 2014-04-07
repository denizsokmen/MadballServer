package Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import Packet.Packets;

public abstract class Server implements Runnable {

	private enum State {
		STOPPED, STOPPING, RUNNING
	}

	private static short DEFAULT_MESSAGE_SIZE = 20024;

	private final AtomicReference<State> state = new AtomicReference<State>(
			State.STOPPED);
	private final int port;
	private final MessageLength messageLength;
	private final Map<SelectionKey, ByteBuffer> readBuffers = new ConcurrentHashMap<SelectionKey, ByteBuffer>();
	private final int defaultBufferSize;
	private LinkedList<SelectionKey> clientList;
	protected Game game;

	protected Server(int port, Game gam) {
		this(port, new TwoByteMessageLength(), DEFAULT_MESSAGE_SIZE, gam);
	}

	protected Server(int port, MessageLength messageLength,
			int defaultBufferSize, Game game) {
		this.port = port;
		this.messageLength = messageLength;
		this.defaultBufferSize = defaultBufferSize;
		this.game = game;
		clientList = new LinkedList<SelectionKey>();
	}

	public int getPort() {
		return port;
	}

	public InetAddress getServer() {
		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	public boolean isRunning() {
		return state.get() == State.RUNNING;
	}

	public boolean isStopped() {
		return state.get() == State.STOPPED;
	}

	public void run() {
		if (!state.compareAndSet(State.STOPPED, State.RUNNING)) {
			started(true);
			return;
		}

		Selector selector = null;
		ServerSocketChannel server = null;
		try {
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);
			started(false);
			while (state.get() == State.RUNNING) {
				selector.select(100); // 100 ms delay en fazla
				for (Iterator<SelectionKey> i = selector.selectedKeys()
						.iterator(); i.hasNext();) {
					SelectionKey key = i.next();
					try {
						
						i.remove();
						if (key.isConnectable()) {
							((SocketChannel) key.channel()).finishConnect();
						}
						if (key.isAcceptable()) {
							SocketChannel client = server.accept();
							client.configureBlocking(false);
							client.socket().setTcpNoDelay(true);

							// client.socket().setTcpNoDelay(true);
							SelectionKey newkey = client.register(selector,
									SelectionKey.OP_READ
											| SelectionKey.OP_WRITE);

							synchronized (clientList) {
								clientList.add(newkey);
							}
							connection(newkey);
						}
						if (key.isReadable()) {

							for (ByteBuffer message : readIncomingMessage(key)) {
								messageReceived(message, key);
							}
						}
					} catch (IOException ioe) {
						resetKey(key);
						disconnected(key);
						// ioe.printStackTrace();
					}
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException("Server failure: " + e.getMessage());
		} finally {
			try {
				selector.close();
				server.socket().close();
				server.close();
				state.set(State.STOPPED);
				stopped();
			} catch (Exception e) {
				// fail
			}
		}
	}

	public boolean stop() {
		return state.compareAndSet(State.RUNNING, State.STOPPING);
	}

	protected void resetKey(SelectionKey key) {

		key.cancel();
		synchronized (clientList) {
			clientList.remove(key);
		}
		readBuffers.remove(key);
	}

	public void write(SelectionKey channelKey, byte[] buffer) {
		short len = (short) buffer.length;
		byte[] lengthBytes = messageLength.lengthToBytes(len);
		ByteBuffer writeBuffer = ByteBuffer.allocate(len + lengthBytes.length);
		writeBuffer.put(lengthBytes);
		writeBuffer.put(buffer);
		writeBuffer.flip();

		if (buffer != null && state.get() == State.RUNNING) {
			int bytesWritten = 0;
			try {
				SocketChannel channel = (SocketChannel) channelKey.channel();
				while (writeBuffer.remaining() > 0) {
					bytesWritten = channel.write(writeBuffer);

				}
				if (bytesWritten == -1) {
					resetKey(channelKey);
					disconnected(channelKey);
				}
			} catch (Exception e) {
				resetKey(channelKey);
				disconnected(channelKey);
			}
		}
	}

	public void broadcast(byte[] buf, SelectionKey fr) {
		synchronized (clientList) {
			Iterator<SelectionKey> i = clientList.iterator();
			while (i.hasNext()) {
				SelectionKey key = i.next();
				if (fr != key)
					write(key, buf);
			}
		}
	}

	public synchronized void sendMessage(Packets pk) {
		broadcast(pk.toByteArray(), null);
	}

	public byte[] lengthToBytes(long len) {
		if (len < 0 || len > 65535) {
			throw new IllegalStateException("");
		}
		return new byte[] { (byte) ((len >>> 8) & 0xff), (byte) (len & 0xff) };
	}

	public long bytesToLength(byte[] bytes) {
		if (bytes.length != 2) {
			throw new IllegalStateException("");
		}
		return ((long) (bytes[0] & 0xff) << 8) + (long) (bytes[1] & 0xff);
	}

	private List<ByteBuffer> readIncomingMessage(SelectionKey key)
			throws IOException {
		ByteBuffer readBuffer = readBuffers.get(key);
		if (readBuffer == null) {
			readBuffer = ByteBuffer.allocate(defaultBufferSize);
			readBuffers.put(key, readBuffer);
		}

		if (((ReadableByteChannel) key.channel()).read(readBuffer) == -1) {
			throw new IOException("Read on closed key");
		}

		readBuffer.flip();

		List<ByteBuffer> result = new ArrayList<ByteBuffer>();

		ByteBuffer msg = readMessage(key, readBuffer);
		while (msg != null) {
			result.add(msg);
			msg = readMessage(key, readBuffer);
		}

		return result;
	}

	private ByteBuffer readMessage(SelectionKey key, ByteBuffer readBuffer) {
		int bytesToRead;
		if (readBuffer.remaining() > messageLength.byteLength()) {
			byte[] lengthBytes = new byte[messageLength.byteLength()];
			readBuffer.get(lengthBytes);
			bytesToRead = (int) messageLength.bytesToLength(lengthBytes);
			if ((readBuffer.limit() - readBuffer.position()) < bytesToRead) {
				if (readBuffer.limit() == readBuffer.capacity()) {
					int oldCapacity = readBuffer.capacity();
					ByteBuffer tmp = ByteBuffer.allocate(bytesToRead
							+ messageLength.byteLength());
					readBuffer.position(0);
					tmp.put(readBuffer);
					readBuffer = tmp;
					readBuffer.position(oldCapacity);
					readBuffer.limit(readBuffer.capacity());
					readBuffers.put(key, readBuffer);
					return null;
				} else {
					readBuffer.position(readBuffer.limit());
					readBuffer.limit(readBuffer.capacity());
					return null;
				}
			}
		} else {
			readBuffer.position(readBuffer.limit());
			readBuffer.limit(readBuffer.capacity());
			return null;
		}
		byte[] resultMessage = new byte[bytesToRead];
		readBuffer.get(resultMessage, 0, bytesToRead);
		int remaining = readBuffer.remaining();
		readBuffer.limit(readBuffer.capacity());
		readBuffer.compact();
		readBuffer.position(0);
		readBuffer.limit(remaining);
		return ByteBuffer.wrap(resultMessage);
	}

	protected abstract void messageReceived(ByteBuffer message, SelectionKey key);

	protected abstract void connection(SelectionKey key);

	protected abstract void disconnected(SelectionKey key);

	protected abstract void started(boolean alreadyStarted);

	protected abstract void stopped();
}
