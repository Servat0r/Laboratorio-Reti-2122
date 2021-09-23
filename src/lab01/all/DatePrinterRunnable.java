package lab01.all;

import java.util.Calendar;

final class DatePrinterRunnable implements Runnable {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Runnable r = new DatePrinterRunnable();
		Thread t = new Thread(r);
		t.start();
		System.out.printf("Main ended\n%s\n", t.getName());
	}

	@Override
	public void run() {
		while (true) {
			System.out.println(Calendar.getInstance().getTime());
			System.out.println(Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.err.println("Interrupted");
			}
		}
	}

}
