package assignments.lab08;

import java.util.*;

public final class BankAccount {
	private String name;
	private List<Transfer> transfers;
	
	public BankAccount(String name) {
		if (name == null) throw new NullPointerException();
		this.name = name;
		this.transfers = new ArrayList<Transfer>();
	}

	public final String getName() {
		return this.name;
	}

	public final void setName(String name) {
		this.name = name;
	}
	
	public final List<Transfer> getTransfers() {
		return this.transfers;
		//List<Transfer> tlist = new ArrayList<>();
		//for (Transfer t : tlist) tlist.add(t);
		//return tlist;
	}

	public final boolean addTransfer(Date date, Causale causale) {
		if (date == null || causale == null) throw new NullPointerException();
		return this.transfers.add(new Transfer(date, causale));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Transfer t : this.transfers) sb.append(t.toString() + "\n");
		return "BankAccount[" + this.name + " {\n" + sb.toString() + "}\n]";
	}
	
}
