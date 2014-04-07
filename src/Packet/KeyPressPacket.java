package Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;



public class KeyPressPacket extends Packets {

	//when a key is pressed.
	//packet id :0x04
	//id , key = 5 bytes
	public int key;
	
	public KeyPressPacket() {
		type=4;
	}
	
}
