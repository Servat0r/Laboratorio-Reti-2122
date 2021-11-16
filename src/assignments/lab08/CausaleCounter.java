package assignments.lab08;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.gson.*;
import com.google.gson.stream.*;

import util.common.Common;
import util.threads.ThreadPoolUtils;

/**
 * Contatore del numero di causali per ogni tipo definito per un file JSON.
 * @author Salvatore Correnti.
 */
public final class CausaleCounter {
	
	private CausaleCounter() {}
	
	/* Massimo termine di attesa prima della chiusura forzata del thread pool. In tal caso (0)
	 * per definizione di ThreadPoolUtils.shutdown() NON vi � timeout, quindi la chiamata finale
	 * equivarr� a uno shutdown() normale del pool.
	 */
	private static final int COUNT_TERM_DELAY = 0;

	/**
	 * Legge un file JSON contenente un array di conti correnti e per ogni Causale c calcola il numero di movimenti
	 * con quella causale restituendo il risultato in una mappa da Causale ad intero.
	 * @param filename Percorso (relativo alla directory corrente) del file da leggere.
	 * @return Una mappa da Causale a intero in caso di successo, null altrimenti.
	 * @throws Exception In caso di errore.
	 */
	public static Map<Causale, Integer> count(String filename) throws Exception {
		Common.notNull(filename);
		Causale[] c = Causale.values();
		Integer[] v = Common.newIntegerArray(c.length, 0);
		/* La mappa sottostante al termine dell'esecuzione sar� tale che per
		 * ogni c in Causale : counter.get(c) == #{movimenti con quella causale}.
		 */
		Map<Causale, Integer> counter = Common.newHashMapFromArrays(c, v);
		ExecutorService pool = new ThreadPoolExecutor(
			ThreadPoolUtils.getProcNum(), /* Numero dei processori logici disponibili (valore consigliato per il numero di core-threads) */
			ThreadPoolUtils.getProcNum(),
			0,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			new ThreadPoolExecutor.AbortPolicy()
		);
		/* Lettura del file */
		Gson gson = new Gson();
		FileInputStream fin = new FileInputStream(filename);
		JsonReader reader = new JsonReader(new InputStreamReader(fin));
		reader.beginArray();
		while (reader.hasNext()) {
			BankAccount account = gson.fromJson(reader, BankAccount.class);
			try {
				pool.execute(new CountTask(counter, account));
			} catch (RejectedExecutionException ree) {
				System.out.println("Rejected Execution");
				break;
			}
		}
		reader.endArray();
		/* Invio del segnale di chiusura al thread pool. */
		ThreadPoolUtils.shutdown(pool, COUNT_TERM_DELAY);
		reader.close();
		fin.close();
		return counter;
	}
}