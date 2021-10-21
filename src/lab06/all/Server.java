package lab06.all;

import java.io.*;
import java.net.*;

public final class Server implements AutoCloseable {
	
	/*
	 * PORT = porta di default per l'apertura del server; in caso di fallimento il costruttore lancia una BindException;
	 * DFL_NAME = nome di default per il file da trasferire;
	 * DFL_CONTENT = contenuto di default per il file da trasferire.
	 */
	public static final int PORT = 10300;
	public static final String DFL_NAME = "sample.txt";
	public static final String DFL_CONTENT = "abcdefghijklmnopqrstuvwxyz\n0123456789\n";
	
	private File file = null; /* File da trasferire. */
	private final ServerSocket listener; /* ListenSocket del server. */
	
	/**
	 * Costruisce un server creando il file corrispondente da trasferire con nome identificato da filename
	 * e contenuto identificato da content. Se il file esiste già il suo contenuto viene sovrascritto.
	 * @param filename Nome del file da trasferire.
	 * @param content Contenuto del file da trasferire.
	 * @throws IOException Se createNewFile(), write() o il costruttore dello stream di output o del
	 * ListenSocket falliscono.
	 */
	public Server(String filename, String content) throws IOException {
		if (filename == null || content == null) throw new NullPointerException();
		this.file = new File(filename);
		this.file.createNewFile();
		try (FileOutputStream fcontent = new FileOutputStream(this.file)){
			fcontent.write(content.getBytes());
		}
		this.listener = new ServerSocket(PORT);
	}
	
	/**
	 * Costruisce un server in modo che trasferisca un file già esistente, identificato da filename.
	 * Se il file non esiste viene lanciata un'eccezione.
	 * @param filename Nome del file da trasferire.
	 * @throws IOException Se il file da trasferire non esiste (FileNotFoundException) o se lanciata
	 * dagli altri metodi utilizzati.
	 */
	public Server(String filename) throws IOException {
		if (filename == null) throw new NullPointerException();
		this.file = new File(filename);
		if (!this.file.exists()) throw new FileNotFoundException(); //??
		this.listener = new ServerSocket(PORT);
	}
	
	/**
	 * Trasferisce il contenuto del file associato al server sullo stream di output fornito.
	 * @param out Stream di output su cui trasferire il contenuto del file.
	 * @return Il numero di bytes trasferiti (0 se il file è vuoto).
	 * @throws Exception Se lanciata dai metodi utilizzati.
	 */
	private long transferFile(OutputStream out) throws Exception {
		return (new FileTransfer(this.file, out)).call();
	}
	
	/**
	 * Chiude il ListenSocket del server.
	 */
	public void close() throws Exception { this.listener.close(); }
	
	/**
	 * Incapsula il metodo accept() del ListenSocket.
	 * @return Un Socket con cui scambiare dati con il client.
	 * @throws IOException Se lanciata dai metodi utilizzati.
	 */
	public Socket accept() throws IOException { return this.listener.accept(); }
	
	/*
	 * Assumiamo che l'input da riga di comando sia della forma: <nome_programma> [<nome_file> [<contenuto_file>]],
	 * dove <nome_file> è il nome del file da creare e <contenuto_file> è il contenuto da scrivere nel file.
	 * Se tali valori non sono forniti, ne vengono usati due di default.
	 */
	public static void main(String[] args) throws Exception {
		String dfl_name = (args.length >= 1 ? args[0] : DFL_NAME);
		String dfl_content = (args.length >= 2 ? args[1] : DFL_CONTENT);
		
		try (Server server = new Server(dfl_name, dfl_content);){
			System.out.println("Server is running...");
			while (true) {
				try (
					Socket s = server.accept();
					OutputStream out = s.getOutputStream();
				){ server.transferFile(out); }
			}
		}
	}
}