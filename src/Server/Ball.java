package Server;

public class Ball extends Entity {

	public Player owner;
	public boolean canBeCaptured;
	public Timer capture;

	public Ball() {
		super();
		
		this.type = 1;
		speed.x = 1.0f;
		speed.y = 1.0f;
		pos.z = 50.0f;
		accel.z = -0.09f;
		addAnim(1,1000); // idle
		addAnim(2,150); // idle
		curAnim = animset.get(0);
	}
	
	public void possess(Player own) { // null da olabilir
		if (owner != null) {
			owner.canCapture=false;
			owner.captureTimer.tick();
		}
		
		owner = own;
	}

	public void onUpdate() {
		if (pos.z < 0.0f) {
			pos.z = 0;
			speed.z *= -0.8;
			speed.x *= 0.8;
			speed.y *= 0.8;
		}
		
		if (pos.x < 0.0f || pos.x > 2666.0f)
			speed.x *= -1.1f;

		if ( pos.y < 0.0f || pos.y > 1000.0f)
			speed.y *= -1.1f;
		
		if (Math.abs(speed.z) < 0.02)
			speed.z = 0;

		if (Math.abs(speed.x) < 0.02)
			speed.x = 0;

		if (Math.abs(speed.y) < 0.02)
			speed.y = 0;

		if (speed.magnitude() > 0) 
			setAnim(1);
		else
			setAnim(0);
		
		super.onUpdate();
		
		

		if (owner != null) {
			
			pos.x = owner.pos.x + owner.dir * 26.0f;
			pos.y = owner.pos.y - 5.0f;
			pos.z = owner.pos.z;
			speed.set(owner.speed);
			//setAnim(1);
		}
	}

}
