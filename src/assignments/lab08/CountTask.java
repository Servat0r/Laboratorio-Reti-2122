package assignments.lab08;

import java.util.*;

import util.common.Common;

//Contatore del tipo delle causali
public final class CountTask implements Runnable {

	private Map<Causale, Integer> counter;
	private BankAccount account;
	
	public CountTask(Map<Causale, Integer> counter, BankAccount account) {
		Common.notNull(account);
		Common.notNull(counter);
		this.counter = counter;
		this.account = account;
	}
	
	public void run() {
		for (Transfer transfer : this.account.getTransfers()) {
			synchronized (this.counter) {
				Causale c = transfer.getCausale();
				int num = this.counter.get(c);
				this.counter.replace(c, num+1);
			}
		}
	}	
}