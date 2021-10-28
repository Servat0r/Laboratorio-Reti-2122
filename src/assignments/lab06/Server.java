package assignments.lab06;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import util.ThreadPoolUtils;

/**
 * Server per il trasferimento di file locali ai client.
 * @author Salvatore Correnti
 */
public final class Server implements AutoCloseable {
	
	/*
	 * PORT = porta di default per l'apertura del server; in caso di fallimento il costruttore lancia una BindException;
	 * DFL_CORE_THREADS = numero di core threads di deafult;
	 * MAX_THREADS = massimo numero di thread workers attivi contemporaneamente;
	 * WAITQUEUE_CAP = capacità della coda di attesa del worker threads pool;
	 * THREADS_TTL = timeout per la terminazione di un non-core worker thread che non riceve ulteriori richieste;
	 * THREADS_TTL_UNIT = unità di misura per THREADS_TTL;
	 * MAX_DELAY_TIME = timeout per la chiusura forzata del server dopo una chiamata a shutdown(), se questa non ha provocato
	 * ancora la chiusura del server stesso;
	 * MAX_DELAY_UNIT = unità di misura per MAX_DELAY_TIME;
	 * DFL_ROOT_DIR = root directory di default per la ricerca dei file da inviare ai client (path RELATIVO!).
	 */
	public static final int PORT = 10300;
	public static final int DFL_CORE_THREADS = 4;
	public static final int MAX_THREADS = 16;
	public static final int WAITQUEUE_CAP = 32;
	public static final int THREADS_TTL = 2000;
	public static final TimeUnit THREADS_TTL_UNIT = TimeUnit.MILLISECONDS;
	public static final int MAX_DELAY_TIME = 10000;
	public static final TimeUnit MAX_DELAY_UNIT = TimeUnit.MILLISECONDS;
	public static final String DFL_ROOT_DIR = ".";
	
	
	private ServerSocket listener; /* ListenSocket del server. */
	private final ExecutorService workers; /* Workers threads per processare le richieste */
	private final String rootDirectory; /* Root directory per la ricerca dei file da inviare ai client. */
	
	public Server(String rootDirectory) throws IOException {
		if (rootDirectory == null) throw new NullPointerException();
		this.listener = new ServerSocket(PORT);
		this.rootDirectory = rootDirectory;
		this.workers = new ThreadPoolExecutor(
			DFL_CORE_THREADS,
			MAX_THREADS,
			THREADS_TTL,
			THREADS_TTL_UNIT,
			new ArrayBlockingQueue<>(WAITQUEUE_CAP),
			new ThreadPoolExecutor.AbortPolicy()
		);
	}
	
	/**
	 * Chiude il ListenSocket e il worker threads pool del server.
	 */
	public void close() throws Exception {
		this.listener.close();
		ThreadPoolUtils.shutdown(this.workers, MAX_DELAY_TIME, MAX_DELAY_UNIT);
	}
	
	/**
	 * Incapsula il metodo accept() del ListenSocket.
	 * @return Un Socket con cui scambiare dati con il client, null in caso di errore di I/O.
	 */
	public Socket accept() {
		if (this.listener.isClosed()) return null;
		try {
			Socket result = this.listener.accept();
			return result;
		} catch (IOException ioe) { return null; }
	}
	
	public boolean handleRequest() throws IOException {
		Socket connection = null; 
		try {
			connection = this.accept();
			if (connection == null) return false; //Listen Socket closed
			this.workers.execute(new FileTransfer(connection, this.rootDirectory));
			return true;
		} catch (RejectedExecutionException ree) { //For example if thread pool has been shutdown
			if (connection.isClosed()) System.out.println("PIPPO");
			connection.close();
			return false;
		}
	}
	
	public void mainloop() throws IOException {
		while (this.handleRequest()) { }
	}
}