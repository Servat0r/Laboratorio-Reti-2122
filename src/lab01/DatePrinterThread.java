package lab01;

import java.util.Calendar;

final class DatePrinterThread extends Thread {

	public void run() {
		while (true) {
			System.out.println(Calendar.getInstance().getTime());
			System.out.println(Thread.currentThread().getName());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.err.println("Interrupted");
				return;
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread t = new DatePrinterThread();
		//t.setDaemon(true);
		t.start();
		/*
		try {
			t.join();
		} catch (InterruptedException e) {
			System.err.println("Interrupted");
		}
		*/
		System.out.println(Thread.currentThread().getName());		
		System.out.println("Main ended");
	}
}
