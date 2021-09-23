package util;

/**
 * Utilities for threads.
 * @author Salvatore Correnti
 *
 */

public class ThreadUtils {
	
	private ThreadUtils() {}
	
	/**
	 * @param millis - Length of sleep-time in milliseconds. 
	 * @return true if sleep is completed, false if it is interrupted before completion.
	 * @throws IllegalArgumentException if millis < 0.
	 */
	public static boolean Sleep(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	/**
	 * @param millis - Length of sleep-time in milliseconds.
	 * @param nanos - Length of sleep-time in nanoseconds.
	 * @return true if sleep is completed, false if it is interrupted before completion.
	 * @throws IllegalArgumentException if millis < 0.
	 */
	public static boolean Sleep(long millis, int nanos) {
		try {
			Thread.sleep(millis, nanos);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}	
}