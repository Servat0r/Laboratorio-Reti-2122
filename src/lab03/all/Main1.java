package lab03.all;

import java.util.*;
import java.util.concurrent.*;

import util.threads.ThreadPoolUtils;

public final class Main1 {

	private static final int NUM_READERS = 20;
	private static final int NUM_WRITERS = 20;
	private static final int TERM_DELAY = 20; //2 secondi
	
	public static long runTest(ExecutorService pool, Counter c) {
		long time = System.nanoTime();
		for (int i = 0; i < NUM_WRITERS; i++) pool.execute(new Writer(c));
		for (int i = 0; i < NUM_READERS; i++) pool.execute(new Reader(c));
		ThreadPoolUtils.shutdown(pool, TERM_DELAY);
		time = System.nanoTime() - time;
		return time;
	}
	
	public static void main(String[] args) {
		long time_unsync, time_lock, time_rwlock;
		
		Counter c1 = new Counter();		
		Counter c2 = new LockCounter();
		Counter c3 = new RWLockCounter();
		
		ExecutorService pool1 = Executors.newCachedThreadPool();
		ExecutorService pool2 = Executors.newCachedThreadPool();
		ExecutorService pool3 = Executors.newCachedThreadPool();
		
		time_unsync = runTest(pool1, c1);
		time_lock = runTest(pool2, c2);
		time_rwlock = runTest(pool3, c3);
		
		System.out.printf("Tempo impiegato senza lock: %f ms%n", time_unsync/1000000.0);
		System.out.printf("Tempo impiegato con reentrant-lock: %f ms%n", time_lock/1000000.0);
		System.out.printf("Tempo impiegato con read-write-lock: %f ms%n", time_rwlock/1000000.0);
	}
}