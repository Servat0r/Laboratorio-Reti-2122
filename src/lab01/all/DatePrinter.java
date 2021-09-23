package lab01.all;

import java.util.Calendar;

final class DatePrinter {
	public static void main(String[] args) {
		while (true) {
			System.out.println(Calendar.getInstance().getTime());
			System.out.println(Thread.currentThread());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				System.err.println("Interrupted");
				return;
			}
		}
	}
}