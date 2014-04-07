package Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class MovementPacket extends Packets {

	//position update packet of an object.
	//packet id :0x01
	//id ,serial , x, y, z = 17 bytes
	public int serial;
	public float x,y,z,dir;
	
	public MovementPacket() {
		type=1;
	}
	
}
