package Server;

public class Player extends Entity {
	public Client client;

	public boolean canCapture;
	public Timer captureTimer;
	public float speedLimit;

	public Vector direction;
	public boolean immobilized;

	public Player() {
		super();
		direction = new Vector();
		accel.z = -0.1f;
		speedLimit = 2.9f;
		captureTimer = new Timer();
		canCapture = true;

		addAnim(1, 1000); // idle = 0
		addAnim(2, 150); // koþ = 1
		addAnim(3, 150); // þut = 2
		addAnim(2, 200); // kafa = 3
		addAnim(3, 100); // uçan kafa = 4
		addAnim(1, 2500); // dayak = 5
		addAnim(1, 2500); // yatma = 6
		curAnim = animset.get(0);
	}

	public void setSpeed(float x, float y, float z) {
		if (!immobilized) {
			speed.x = x;
			speed.y = y;
			speed.z = z;

		}
	}

	public void immobilize() {
		if (!immobilized) {
			immobilized = true;
			setAnim(5);
			if (game.ball.owner == this)
				game.ball.possess(null);
		}
	}

	public void onUpdate() {
		if (canCapture == false || immobilized) {
			if (captureTimer.ticked(1000)) {
				canCapture = true;
			}
		} else {
			if (game.ball != null) {
				if (Vector.dist(game.ball.pos, pos) < 25.0f) {
					if (game.ball.pos.z - pos.z > -5.0f
							&& game.ball.pos.z - pos.z < 25.0f) {

						if (game.ball.speed.magnitude() > 8.0f) {
							setSpeed(3.0f, 0.0f, 3.0f);
							immobilize();
						} else {

							if (getAnimIndex() == 0 || getAnimIndex() == 1)
								game.ball.possess(this);
							else if (getAnimIndex() == 2) { // þut çekerken
															// çarparsa
								direction.normalize();
								float mag = game.ball.speed.magnitude();
								game.ball.speed.x = mag * direction.x;
								game.ball.speed.y = mag * direction.y;
								game.ball.speed.z = 1.5f;
								game.ball.speed.add(speed);

							}
						}
					}

					if (game.ball.pos.z - pos.z >= 25.0f
							&& game.ball.pos.z - pos.z < 70.0f) {
						if (game.ball.speed.magnitude() > 8.0f) {
							setSpeed(3.0f, 0.0f, 3.0f);
							immobilize();
						} else {
							if (getAnimIndex() == 3) {// havada kafa atma þeysi
								direction.normalize();
								float mag = game.ball.speed.magnitude();
								game.ball.speed.x = mag * direction.x + speed.x;
								game.ball.speed.y = mag * direction.y + speed.y;
								game.ball.speed.z *= -1;
							} else {
								game.ball.speed.x *= -1.0f;
								game.ball.speed.y *= -1.0f;
								game.ball.speed.z *= -1.0f;
								game.ball.speed.add(speed);
								game.ball.pos.add(speed);
							}
						}
					}
				}
			}
		}

		limitSpeed();
		super.onUpdate();

		if (needsUpdate) {
			if (game.ball.owner == this)
				game.ball.needsUpdate = true;
		}

		if (pos.z < 0.0f) {
			speed.z = 0;
			pos.z = 0;
			if (getAnimIndex() == 4) { // uçan kafa bitiþi
				setAnim(0);
				speed.x = 0;
				speed.y = 0;
			}
			if (getAnimIndex() == 5) {
				setAnim(6); // 2.5 sn yerde yat
				speed.x = 0;
				speed.y = 0;
			}

		}

		if (speed.magnitude() != 0 && getAnimIndex() == 0) {
			setAnim(1);
		}
	}

	public void limitSpeed() {
		if (Math.abs(speed.x) > speedLimit)
			speed.x = Math.signum(speed.x) * speedLimit;

		if (Math.abs(speed.y) > speedLimit)
			speed.y = Math.signum(speed.y) * speedLimit;

		if (getAnimIndex() == 2) {
			speed.x = 0;
			speed.y = 0;
		}

	}

	public void onAnimEnd(int index) {
		if (index == 1) {
			if (speed.magnitude() == 0)
				setAnim(0);
		}
		if (index == 2) { // þut bitimi baþa dön
			setAnim(0);
			if (game.ball.owner == this) {
				game.ball.possess(null);

				if (direction.magnitude() == 0) {
					direction.x = dir;
					direction.y = 0;
					direction.z = 0;
				}
				direction.normalize();
				game.ball.speed.x = 5.0f * direction.x;
				game.ball.speed.y = 5.0f * direction.y;
				game.ball.speed.z = 3.0f;
				game.ball.speed.add(speed);
			}
		}

		if (index == 3) {
			setAnim(0);
		}

		if (index == 4) { // havada olduðu sürece uçan kafa
			if (pos.z <= 0.0f)
				setAnim(0);
		}

		if (index == 6) { // immobilize bitti
			setAnim(0);
			immobilized = false;
		}

	}

	public boolean canAnim() {
		return (getAnimIndex() == 0 || getAnimIndex() == 1);
	}

}
