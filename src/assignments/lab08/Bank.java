package assignments.lab08;

import java.util.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.*;
import java.nio.channels.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import util.common.Common;

public final class Bank {
	
	private List<BankAccount> accounts;
	
	private static int BYTE_BUFFER_CAPACITY = 1;
		
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
		Common.notNull(clientName);
		if (this.isRegistered(clientName)) return false;
		this.accounts.add(new BankAccount(clientName));
		return true;
	}
	
	public boolean addTransfer(String clientName, Date date, Causale causale) {
		Common.notNull(clientName); Common.notNull(date); Common.notNull(causale);
		if (!this.isRegistered(clientName)) return false;
		BankAccount account = this.getAccountByName(clientName);
		return account.addTransfer(date, causale);
	}
		
	public void printToFile_NIO(String filename) throws IOException {
		Common.notNull(filename);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File file = new File(filename);
		file.createNewFile();
		WritableByteChannel fc = Channels.newChannel(new FileOutputStream(file));
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_CAPACITY);
		buffer.put((byte)'[');
		buffer.flip();
		fc.write(buffer);
		buffer.clear();
		for (int i = 0; i < this.accounts.size(); i++) {
			if (i != 0) {
				buffer.put((byte)',');
				buffer.flip();
				while (buffer.hasRemaining()) fc.write(buffer);
				buffer.clear();
			}
			ByteBuffer buffer2 = ByteBuffer.wrap(gson.toJson(this.accounts.get(i)).getBytes());
			while (buffer2.hasRemaining()) fc.write(buffer2);
		}
		buffer.put((byte)']');
		buffer.flip();
		fc.write(buffer);
		buffer.clear();
		fc.close();
	}
	
	public void printToFile_IO(String filename) throws IOException {
		Common.notNull(filename);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		File file = new File(filename);
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file);
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(fout));
		writer.setIndent("  ");
		writer.beginArray();
		Type bankAccountType = new TypeToken<BankAccount>() {}.getType();
		for (BankAccount account : this.accounts) {
			gson.toJson(account, bankAccountType, writer);
		}
		writer.endArray();
		writer.close();
		fout.close();
	}
	
}