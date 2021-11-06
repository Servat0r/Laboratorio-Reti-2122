package assignments.lab02;

import java.util.concurrent.*;

import util.threads.ThreadUtils;

public final class UfficioPostale {

	/*
	 * NUM_SPORTELLI = numero di sportelli dell'ufficio postale
	 * RETRY_DELAY = tempo di attesa prima che un cliente respinto riprovi ad accedere
	 */
	private static final int NUM_SPORTELLI = 4;
	private static final int RETRY_DELAY = 10;
	
	/*
	 * prima_sala = coda bloccante non limitata che rappresenta la coda dei clienti nella prima sala
	 * pool = thread pool che rappresenta gli sportelli dell'ufficio postale e la seconda sala d'attesa
	 * k = numero di clienti che possono attendere nella seconda sala, i.e. dimensione della coda gestita dal pool
	 */
	private BlockingQueue<Cliente> prima_sala;
	private ExecutorService pool;
	private int k;
	
	/**
	 * @param k Il numero massimo di persone nella seconda sala.
	 */
	public UfficioPostale(int k) {
		if (k <= 0) throw new IllegalArgumentException("k deve essere positivo");
		this.k = k;
		this.prima_sala = new LinkedBlockingQueue<>();
		this.pool = new ThreadPoolExecutor(
				NUM_SPORTELLI,
				NUM_SPORTELLI,
				0,
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(this.k),
				new ThreadPoolExecutor.AbortPolicy()
		);
	}
	
	/**
	 * Metodo principale: gestisce il flusso di clienti dell'ufficio postale.
	 * NOTA: Si è scelto di modellare in modo da rimuovere l'elemento in testa solo se la sottomissione al thread
	 * pool avviene con successo, mentre se questa viene rigettata allora l'elemento non viene rimosso dalla prima
	 * coda. Alternativamente si può usare una LinkedBlockingDeque.
	 * @return true se il flusso e la terminazione del pool di thread avviene correttamente, false altrimenti.
	 */
	public boolean execute(int N) {
		if (N <= 0) {
			System.err.println("N dev'essere positivo!");
			return false;
		}
		
		for (int i = 0; i < N; i++) {
			try {
				this.prima_sala.put(new Cliente(i));
			} catch (InterruptedException ie) {
				System.err.println("Errore durante l'inserimento dei clienti in coda");
				ie.printStackTrace();
				this.pool.shutdown();
				return false;
			}
		}
		
		Cliente c = null;
		boolean result = true;
		
		while (this.prima_sala.size() > 0) {
			try {
				/* 
				 * La peek() non restituisce mai null perché l'unico thread che opera su prima_sala è il main,
				 * e si è già verificato che la coda non è vuota.
				 */
				c = this.prima_sala.peek();
				this.pool.execute(c);
				/*
				 * Se il task è accettato dal thread pool, allora il riferimento è rimosso da prima_sala
				 * (la take() non si blocca mai per gli stessi motivi per cui peek() non restituisce mai null).
				 */
				this.prima_sala.take();
			} catch (InterruptedException e) {
				result = false;
				break;
			} catch (RejectedExecutionException ree) {
				ThreadUtils.Sleep(RETRY_DELAY);
			}
		}
		/* Si aspetta che tutti i clienti siano stati serviti */
		this.pool.shutdown();
		return result;
		
	}
	
	/* Nel seguito assumiamo che da riga di comando siano forniti i seguenti argomenti:
	 * - N = numero totale di clienti;
	 * - k = numero massimo di clienti in sala d'attesa (la seconda sala).
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Fornire come primo argomento il numero totale di clienti e come secondo argomento" +
		" il numero massimo di clienti in sala d'attesa");
			System.exit(1);
		}
		int N = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);
		try {
			UfficioPostale up = new UfficioPostale(k);
			if (!up.execute(N)) System.exit(1);
		} catch (IllegalArgumentException e) {
			System.err.println("Errore durante l'inizializzazione dell'ufficio postale");
			System.exit(1);
		}
	}
}