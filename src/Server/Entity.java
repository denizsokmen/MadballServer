package Server;

import java.util.ArrayList;

import Packet.AnimatePacket;

public class Entity {

	// 0. animasyon idle'dýr, oynamaz.
	public Game game;
	public int serial;
	public boolean needsUpdate = false;
	public Vector pos;
	public Vector speed;
	public Vector accel;
	public boolean dead = false;
	public byte type;
	public float dir;

	public Animation curAnim;
	public ArrayList<Animation> animset;

	public Entity() {
		animset = new ArrayList<Animation>();
		dir = 1.0f;
		pos = new Vector();
		speed = new Vector();
		accel = new Vector();
		
	}

	public void onUpdate() {
		if (curAnim != null) {
			int animRes = curAnim.checkFrame();
			
			if (animRes == -1) { // Frame bitiþi.
				if (getAnimIndex() != 0) // idle anim bitmez.
					onAnimEnd(getAnimIndex());
			}

		}

		if (speed.magnitude() > 0)
			needsUpdate = true;
		speed.add(accel);
		
		pos.add(speed);
	}

	public void onAnimEnd(int index) {

	}

	public void setAnim(Animation anim) {
		if (curAnim != anim) {
				curAnim = anim;
				curAnim.timer.tick();
				curAnim.curFrame = 0;

				AnimatePacket pk = new AnimatePacket();
				pk.serial=this.serial;
				pk.animSet = (byte) animset.indexOf(anim);
				game.server.sendMessage(pk);
			
		}
	}
	
	public void setAnim(int index) {
		setAnim(animset.get(index));
	}

	public int getAnimIndex() {
		return animset.indexOf(curAnim);
	}
	
	public void addAnim(int numfr, int spf) {
		Animation anim = new Animation(numfr, spf);
		animset.add(anim);
	}
}
