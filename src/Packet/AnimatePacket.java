package Packet;

public class AnimatePacket extends Packets {
	// animate object
		// packet id :0x06
		// id ,serial , type, x, y, z = 18 bytes
		
		public int serial;
		public byte animSet;
		
		public AnimatePacket() {
			type=6;
		}

}
