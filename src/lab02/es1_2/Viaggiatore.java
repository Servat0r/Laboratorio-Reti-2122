package lab02.es1_2;

import java.util.concurrent.ThreadLocalRandom;

import util.*;
import util.threads.ThreadUtils;

public final class Viaggiatore implements Runnable {

	private final int id;
	private static final int MIN_DELAY = 0;
	private static final int MAX_DELAY = 1000;
	
	public Viaggiatore(final int id) {
		this.id = id;
	}

	
	public final int getId() {
		return id;
	}
	
	public void run() {
		System.out.printf("Viaggiatore %d: sto acquistando un biglietto\n", this.id);
		ThreadUtils.Sleep(ThreadLocalRandom.current().nextInt(MIN_DELAY, MAX_DELAY));
		System.out.printf("Viaggiatore %d: ho acquistato il biglietto\n", this.id);
	}
}