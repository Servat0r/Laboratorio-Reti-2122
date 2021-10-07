package lab04.all;

final class Main {
	
	public static final long MAX_DELAY = 5000;
	
	public static void main(String[] args) {
		Dropbox d = new Dropbox();
		Thread producer = new Thread( new Producer(d) );
		Thread even_consumer = new Thread( new Consumer(true, d) );
		Thread odd_consumer = new Thread( new Consumer(false, d) );
		producer.start();
		even_consumer.start();
		odd_consumer.start();
		try {
			producer.join(MAX_DELAY);
			even_consumer.join(MAX_DELAY);
			odd_consumer.join(MAX_DELAY);
			if (even_consumer.isAlive()) even_consumer.interrupt();
			if (odd_consumer.isAlive()) odd_consumer.interrupt();
			if (producer.isAlive()) producer.interrupt();
		} catch (InterruptedException e) {
			System.err.println("Main: interrupted during join");
			System.exit(1);
		}
	}
}