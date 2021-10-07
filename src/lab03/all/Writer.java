package lab03.all;

public final class Writer implements Runnable {

	private Counter counter;
	
	public Writer(Counter counter) {
		this.counter = counter;
	}
	
	@Override
	public void run() {
		this.counter.increment();
	}
}