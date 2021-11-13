package assignments.lab08;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import com.google.gson.*;

public final class Bank {
	
	private List<BankAccount> accounts;
		
	public Bank() {
		this.accounts = new ArrayList<>();
	}
	
	private boolean isRegistered(String clientName) {
		for (BankAccount account : this.accounts) {
			if (account.getName().equals(clientName)) return true;
		}
		return false;
	}
	
	private BankAccount getAccountByName(String clientName) {
		for (BankAccount account : this.accounts) {
			if (account.getName().equals(clientName)) return account;
		}
		return null;
	}
	
	public boolean addUser(String clientName) {
		if (clientName == null) throw new NullPointerException();
		if (this.isRegistered(clientName)) return false;
		this.accounts.add(new BankAccount(clientName));
		return true;
	}
	
	public boolean addTransfer(String clientName, Date date, Causale causale) {
		if (clientName == null || date == null || causale == null) throw new NullPointerException();
		if (!this.isRegistered(clientName)) return false;
		BankAccount account = this.getAccountByName(clientName);
		return account.addTransfer(date, causale);
	}
	
	public void printToFile(String filename) throws IOException {
		if (filename == null) throw new NullPointerException();
		File file = new File(filename);
		WritableByteChannel fc = Channels.newChannel(new FileOutputStream(file));
		ByteBuffer buffer = ByteBuffer.wrap(this.toJSON().getBytes()); //ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
		//TODO Modificare per gestire grossi file in RAM
		while (buffer.hasRemaining()) fc.write(buffer);
		fc.close();
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < this.accounts.size(); i++) {
			if (i != 0) { sb.append(","); }
			sb.append(gson.toJson(this.accounts.get(i)));
		}
		sb.append("]");
		return sb.toString();
		//return gson.toJson(this);
	}
}