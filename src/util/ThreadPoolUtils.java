package util;

import java.util.concurrent.*;

/**
 * Utilities for threadpools.
 * @author Salvatore Correnti
 *
 */
public final class ThreadPoolUtils {
	
	private ThreadPoolUtils() {}
	
	public static void shutdown(ExecutorService service, int term_delay) {
		service.shutdown();
		try {
			if (!service.awaitTermination(term_delay, TimeUnit.MILLISECONDS)) {
				service.shutdownNow();
			}
		} catch (InterruptedException ie) {
			service.shutdownNow();
		}
	}

	public static void shutdown(ExecutorService service, int term_delay, TimeUnit unit) {
		service.shutdown();
		try {
			if (!service.awaitTermination(term_delay, unit)) {
				service.shutdownNow();
			}
		} catch (InterruptedException ie) {
			service.shutdownNow();
		}
	}
}
