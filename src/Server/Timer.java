package Server;

public class Timer {
	public long lastTime;

	public Timer() {
		tick();
	}

	public void tick() {
		lastTime = getTime();
	}

	public boolean ticked(long millis) {
		if (getTime() - lastTime >= millis) {
			lastTime=getTime();
			return true;
		}
		else
			return false;
	}

	public long getTime() {
		return System.nanoTime() / 1000000;
	}

	
}
