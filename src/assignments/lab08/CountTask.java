package assignments.lab08;

import java.util.concurrent.*;

import util.common.Common;

/**
 * Task di conteggio della frequenza delle varie causali in un conto corrente.
 * @author Salvatore Correnti
 */
public final class CountTask implements Runnable {

	private ConcurrentMap<Causale, Integer> counter;
	private BankAccount account;
	
	public CountTask(ConcurrentMap<Causale, Integer> counter, BankAccount account) {
		Common.notNull(account);
		Common.notNull(counter);
		this.counter = counter;
		this.account = account;
	}
	
	private void increment(Transfer t) {
		Causale c = t.getCausale();
		int num = this.counter.get(c);
		this.counter.put(c, num + 1);
	}
	
	public void run() {
		for (Transfer transfer : this.account.getTransfers()) {
			synchronized (this.counter) { this.increment(transfer); }
		}
	}
}