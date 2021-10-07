package lab04.all;

public final class Dropbox {
	private int value;
	private boolean empty;
	
	public Dropbox() {
		this.empty = true;
		this.value = 0;
	}
	
	public synchronized int take(boolean req) throws InterruptedException {
		while (this.empty || ((this.value % 2 == 0) != req)) this.wait();
		this.empty = true;
		int result = this.value;
		this.value = 0;
		this.notifyAll();
		return result;
	}
	
	public synchronized void put(int value) throws InterruptedException {
		while (!this.empty) this.wait();
		this.empty = false;
		this.value = value;
		this.notifyAll();
	}
}