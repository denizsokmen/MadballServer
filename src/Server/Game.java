package Server;


import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import Packet.*;

public class Game implements Runnable {

	public Server server;
	public static int serial = 0;
	public LinkedList<Client> clientList = new LinkedList<Client>();
	public ArrayList<Entity> entityList;
	public Ball ball;
	public Timer fpscontrol;

	@Override
	public void run() {
		entityList = new ArrayList<Entity>();
		fpscontrol = new Timer();

		server = new Server(2593, this) {
			protected void stopped() {
			}

			@Override
			protected void messageReceived(ByteBuffer message, SelectionKey key) {
				Client cli = game.getClient(key);
				if (cli != null) {
					cli.messages.add(message);
				}
			}

			@Override
			protected void connection(SelectionKey key) {
				System.out.println("Client connected");
				synchronized (game.entityList) {
					Iterator<Entity> it = game.entityList.iterator();
					while (it.hasNext()) {
						Entity e = it.next();

						CreateObjectPacket pk = new CreateObjectPacket();
						pk.x = e.pos.x;
						pk.y = e.pos.y;
						pk.z = e.pos.z;
						pk.serial = e.serial;
						pk.obj = e.type;

						server.write(key, pk.toByteArray());
					}
					game.addClient(new Client(key, game));
				}

				

			}

			@Override
			protected void disconnected(SelectionKey key) {
				System.out.println("Client disconnected");
			}

			@Override
			protected void started(boolean alreadyStarted) {
				System.out.println("Server has started");
			}

		};

		new Thread(server).start();
		ball = new Ball();
		addEntity(ball);

		while (true) {
			//if (fpscontrol.ticked(16)) {
				synchronized (clientList) {
					Iterator<Client> it = clientList.iterator();
					while (it.hasNext()) {
						Client c = it.next();

						if (!c.socket.isValid()) {
							c.destroy();
							it.remove();
						} else
							c.update();

					}
				}

				synchronized (entityList) {
					Iterator<Entity> it = entityList.iterator();
					while (it.hasNext()) {
						Entity e = it.next();
						if (e.dead) {
							removeEntity(e);
							it.remove();
						} else {
							e.onUpdate();

							if (e.needsUpdate) {
								MovementPacket p = new MovementPacket();
								p.x = e.pos.x;
								p.y = e.pos.y;
								p.z = e.pos.z;
								p.dir = e.dir;
								p.serial = e.serial;
								p.type = 1;
								server.sendMessage(p);
								e.needsUpdate = false;
							}
						}
					}
				}

			//}
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void processPacket(Packets pk) {

	}

	public void addEntity(Entity e) {
		
			serial++;
			e.serial = serial;
			e.game = this;
			entityList.add(e);

			CreateObjectPacket pk = new CreateObjectPacket();
			pk.x = e.pos.x;
			pk.y = e.pos.y;
			pk.z = e.pos.z;
			pk.serial = serial;
			pk.obj = 0;
			server.sendMessage(pk);
		
	}

	public void removeEntity(Entity e) {
		synchronized (entityList) {
			DestroyPacket pk = new DestroyPacket();
			pk.serial = e.serial;
			server.sendMessage(pk);
		}
	}

	public void addClient(Client c) {
		synchronized (clientList) {
			clientList.add(c);
		}
	}

	public Client getClient(SelectionKey key) {
		synchronized (clientList) {
			Iterator<Client> it = clientList.iterator();
			while (it.hasNext()) {
				Client cli = it.next();
				if (cli.socket == key)
					return cli;
			}
		}

		return null;
	}

	public void removeClient(Client c) {
		synchronized (clientList) {
			clientList.remove(c);
		}
	}

}
