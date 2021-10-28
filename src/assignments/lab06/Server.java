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
	
	public static final int READY = 0;
	public static final int RUNNING = 1;
	public static final int CLOSED = 2;
	
	private ServerSocket listener; /* ListenSocket del server. */
	private final ExecutorService workers; /* Workers threads per processare le richieste */
	private final String rootDirectory; /* Root directory per la ricerca dei file da inviare ai client. */
	private int state;
	
	private synchronized void setState(int state) { this.state = state; }
	private synchronized int getState() { return this.state; }

	public Server(String rootDirectory) throws IOException {
		if (rootDirectory == null) throw new NullPointerException();
		this.listener = new ServerSocket(PORT);
		this.rootDirectory = rootDirectory;
		this.state = READY;
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
	public synchronized void close() throws Exception {
		this.listener.close();
		ThreadPoolUtils.shutdown(this.workers, MAX_DELAY_TIME, MAX_DELAY_UNIT);
		this.setState(CLOSED);
	}
	
	/**
	 * Incapsula il metodo accept() del ListenSocket.
	 * @return Un Socket con cui scambiare dati con il client, null in caso di errore di I/O.
	 */
	public Socket accept() {
		try {
			Socket result = this.listener.accept();
			return result;
		} catch (IOException ioe) { return null; }
	}
	
	public synchronized void handleRequest() throws IOException {
		Socket connection = null; 
		try {
			connection = this.accept();
			this.workers.execute(new FileTransfer(connection, this.rootDirectory));
		} catch (RejectedExecutionException ree) { connection.close(); }
	}
	
	public void mainloop() throws IOException {
		this.setState(RUNNING);
		try {
			while (true) this.handleRequest();
		} catch (IOException ioe) {
			if (this.getState() != CLOSED) throw ioe;
		}
	}
}