package assignments.lab08;

import java.util.*;

import util.common.Common;

public final class BankAccount {
	private String name;
	private List<Transfer> transfers;
	
	public BankAccount(String name) {
		Common.notNull(name);
		this.name = name;
		this.transfers = new ArrayList<Transfer>();
	}

	public final String getName() {
		return this.name;
	}

	public final void setName(String name) {
		Common.notNull(name);
		this.name = name;
	}
	
	public final List<Transfer> getTransfers() {
		return this.transfers;
	}

	public final boolean addTransfer(Date date, Causale causale) {
		Common.notNull(date); Common.notNull(causale);
		return this.transfers.add(new Transfer(date, causale));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Transfer t : this.transfers) sb.append(t.toString() + "\n");
		return "BankAccount[" + this.name + " {\n" + sb.toString() + "}\n]";
	}
	
}
