package Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class KeyReleasePacket extends Packets {

	//when a key is pressed.
	//packet id :0x05
	//id , key = 5 bytes
	public int key;
	
	public KeyReleasePacket() {
		type=5;
	}
	
}
