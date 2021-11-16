package assignments.lab08;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.lang.reflect.Type;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;

import util.common.Common;

/**
 * Implementazione della banca, comprensiva di serializzazione in JSON dell'insieme dei conti correnti sia usando java.io sia usando java.nio .
 * Gli utenti della banca sono identificati univocamente dal nome indicato per il conto corrente, e la banca mantiene una lista di conti correnti.
 * @author Salvatore Correnti
 */
public final class Bank {
	
	private List<BankAccount> accounts;
	
	private static int BYTE_BUFFER_CAPACITY = 1;
		
	public Bank() {
		this.accounts = new ArrayList<>();
	}
	
	/* Controlla se un utente è presente nella lista di conti correnti. */
	private boolean isRegistered(String clientName) {
		for (BankAccount account : this.accounts) {
			if (account.getName().equals(clientName)) return true;
		}
		return false;
	}
	
	/* Restituisce l'account corrispondente al nome passato se esistente, null altrimenti. */
	private BankAccount getAccountByName(String clientName) {
		for (BankAccount account : this.accounts) {
			if (account.getName().equals(clientName)) return account;
		}
		return null;
	}
	
	/**
	 * Aggiunge un nuovo utente con nome utente dato da clientName se questi non è già presente fra i conti correnti.
	 * @param clientName Nome del cliente da aggiungere.
	 * @return true se l'aggiunta avviene con successo, false altrimenti.
	 */
	public boolean addUser(String clientName) {
		Common.notNull(clientName);
		if (this.isRegistered(clientName)) return false;
		this.accounts.add(new BankAccount(clientName));
		return true;
	}
	
	/* Aggiunge un nuovo movimento all'account del cliente identificato da clientName (se presente). */
	public boolean addTransfer(String clientName, Date date, Causale causale) {
		Common.notNull(clientName); Common.notNull(date); Common.notNull(causale);
		if (!this.isRegistered(clientName)) return false;
		BankAccount account = this.getAccountByName(clientName);
		return account.addTransfer(date, causale);
	}
		
	/**
	 * Serializza l'insieme dei conti correnti in un file JSON come array di conti correnti, facendo uso della libreria java.nio per
	 * la scrittura sul file.
	 * @param filename Percorso (relativo alla directory corrente) del file di output.
	 * @throws IOException in caso di errore di I/O.
	 */
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
	
	/**
	 * Serializza l'insieme dei conti correnti in un file JSON come array di conti correnti, facendo uso della libreria java.io e di
	 * com.gson.stream.JsonWriter per la scrittura sul file.
	 * @param filename Percorso (relativo alla directory corrente) del file di output.
	 * @throws IOException in caso di errore di I/O.
	 */
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