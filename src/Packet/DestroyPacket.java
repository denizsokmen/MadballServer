package Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DestroyPacket extends Packets {
	// destroy an object
	// packet id :0x03
	// id ,serial = 5 byte
	public int serial;
	
	public DestroyPacket() {
		type=3;
	}
}
