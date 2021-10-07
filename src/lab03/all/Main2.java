package lab03.all;

import java.util.*;
import java.util.concurrent.*;

public final class Main2 {

	private static final int NUM_READERS = 40;
	private static final int NUM_WRITERS = 40;
	
	public static void main(String[] args) {
		long time_unsync, time_lock, time_rwlock;
		
		Counter c1 = new Counter();
		List<Runnable> tasklist1 = new ArrayList<>(NUM_READERS + NUM_WRITERS);
		for (int i = 0; i < NUM_WRITERS; i++) tasklist1.add(new Writer(c1));
		for (int i = 0; i < NUM_READERS; i++) tasklist1.add(new Reader(c1));
		
		Counter c2 = new LockCounter();
		List<Runnable> tasklist2 = new ArrayList<>(NUM_READERS + NUM_WRITERS);
		for (int i = 0; i < NUM_WRITERS; i++) tasklist2.add(new Writer(c2));
		for (int i = 0; i < NUM_READERS; i++) tasklist2.add(new Reader(c2));
		
		Counter c3 = new LockCounter();
		List<Runnable> tasklist3 = new ArrayList<>(NUM_READERS + NUM_WRITERS);
		for (int i = 0; i < NUM_WRITERS; i++) tasklist3.add(new Writer(c3));
		for (int i = 0; i < NUM_READERS; i++) tasklist3.add(new Reader(c3));
		
		ExecutorService pool1 = Executors.newFixedThreadPool(4);
		ExecutorService pool2 = Executors.newFixedThreadPool(4);
		ExecutorService pool3 = Executors.newFixedThreadPool(4);
		
		System.out.println("Inizio senza lock");
		time_unsync = System.currentTimeMillis();
		for (int i = 0; i <tasklist1.size(); i++) {
			pool1.execute(tasklist1.get(i));
		}
		time_unsync = System.currentTimeMillis() - time_unsync;
		pool1.shutdownNow();
		System.out.println("Fine senza lock");
		
		System.out.println("Inizio con reentrant lock");
		time_lock = System.currentTimeMillis();
		for (int i = 0; i <tasklist2.size(); i++) {
			pool2.execute(tasklist2.get(i));
		}
		time_lock = System.currentTimeMillis() - time_lock;
		pool2.shutdownNow();
		System.out.println("Fine con reentrant lock");

		System.out.println("Inizio con read-write lock");
		time_rwlock = System.currentTimeMillis();
		for (int i = 0; i <tasklist3.size(); i++) {
			pool3.execute(tasklist3.get(i));
		}
		time_rwlock = System.currentTimeMillis() - time_rwlock;
		pool3.shutdownNow();
		System.out.println("Fine con read-write lock");

		System.out.printf("Tempo impiegato senza lock: %d ms%n", time_unsync);
		System.out.printf("Tempo impiegato con reentrant-lock: %d ms%n", time_lock);
		System.out.printf("Tempo impiegato con read-write-lock: %d ms%n", time_rwlock);
	}
}