package lab04.all;

public final class Consumer implements Runnable {
	private boolean consume_evens;
	private Dropbox buffer;
	
	public Consumer(boolean consume_evens, Dropbox buffer) {
		this.consume_evens = consume_evens;
		this.buffer = buffer;
	}

	public void run() {
		try {	
			int value = this.buffer.take(this.consume_evens);
			System.out.printf("Consumer: letto valore %d%n", value);
		} catch (InterruptedException e) {
			System.out.println("Consumer interrotto!");
		}
	}
}