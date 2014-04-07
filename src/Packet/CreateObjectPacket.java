package Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class CreateObjectPacket extends Packets {
	// send creation of an object
	// packet id :0x02
	// id ,serial , type, x, y, z = 18 bytes
	
	public int serial;
	public byte obj; // top oyuncu vs. vs.
	public float x, y, z;
	
	public CreateObjectPacket() {
		type=2;
	}

}
