package lab03.all;

public final class Reader implements Runnable {

	private Counter counter;
	
	public Reader(Counter counter) {
		this.counter = counter;
	}
	
	@Override
	public void run() {
		System.out.println(this.counter.get());
	}
}