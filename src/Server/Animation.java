package Server;

public class Animation {
	public int milPerFrame;
	public int frameCount;
	public int curFrame;
	public Timer timer;

	public Animation(int count, int spf) {
		frameCount = count;
		milPerFrame = spf;
		timer = new Timer();
	}
	

	public int checkFrame() {
		if (timer.ticked(milPerFrame)) {
			curFrame += 1;
			curFrame %= frameCount;
			if (curFrame==0)
				return -1;
		}
		return curFrame;
	}
	
	
}
