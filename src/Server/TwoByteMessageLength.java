package Server;


public final class TwoByteMessageLength implements MessageLength {
	
	private final int NUM_BYTES = 2;
	private final long MAX_LENGTH = 65535;
	@Override public int byteLength() {
		return NUM_BYTES;
	}
	

	@Override public long maxLength() {
		return MAX_LENGTH;
	}

	@Override public long bytesToLength(byte[] bytes) {
		if (bytes.length!=NUM_BYTES) {
			throw new IllegalStateException("Wrong number of bytes, must be "+NUM_BYTES);
		}
		return ((long)(bytes[0] & 0xff) << 8) + (long)(bytes[1] & 0xff);
	}

	@Override public byte[] lengthToBytes(long len) {
		if (len<0 || len>MAX_LENGTH) {
			throw new IllegalStateException("Illegal size: less than 0 or greater than "+MAX_LENGTH);
		}
		return new byte[] {(byte)((len >>> 8) & 0xff), (byte)(len & 0xff)};
	}
}
