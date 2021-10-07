package lab04.all;

import java.util.concurrent.*;

public final class Producer implements Runnable {

	private Dropbox buffer;
	private static final int MIN_NUM = 0;
	private static final int MAX_NUM = 100;
	
	
	public Producer(Dropbox buffer) { this.buffer = buffer; }
	
	public void run() {
		int value = ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM);
		try {
			this.buffer.put(value);
		} catch (InterruptedException e) {
			System.out.println("Producer interrotto!");
		}
	}
}