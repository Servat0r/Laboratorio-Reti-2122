package util;

import java.util.concurrent.TimeUnit;

/**
 * A general task for sending an interrupt to a thread after a timeout.
 * @author Salvatore Correnti.
 *
 */
public final class Alarm implements Runnable {

	private Thread thread;
	private int timeval;
	
	public Alarm(Thread thread, int timeval) {
		if (thread == null || timeval <= 0) throw new IllegalArgumentException(); 
		this.thread = thread;
		this.timeval = timeval;
	}
		
	public void run() {
		try {
			Thread.sleep(this.timeval);
			this.thread.interrupt();
		} catch (InterruptedException ie) {}
	}

}
