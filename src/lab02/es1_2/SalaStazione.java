package lab02.es1_2;

import java.util.concurrent.*;

import util.*;
import util.threads.ThreadPoolUtils;
import util.threads.ThreadUtils;

public final class SalaStazione {
	
	//Numero di emettitrici
	private static final int NUM_EMETTITRICI = 5;
	//Massimo numero di viaggiatori in sala
	private static final int MAX_VIAGGIATORI = 10;
	//Numero di viaggiatori che arrivano
	private static final int TRAV_NUM = 50;
	//Ritardo fra un viaggiatore e il successivo (ms)
	private static final int TRAV_ARR_DELAY = 50;
	//Massimo tempo di attesa prima di forzare la terminazione
	private static final int TERM_DELAY = 5000;
	
	
	public static void main(String[] args) {
		ExecutorService service = new ThreadPoolExecutor(
				NUM_EMETTITRICI,
				NUM_EMETTITRICI,
				0, //Tutti i thread sono "core-threads" quindi questo timeout è irrilevante
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(MAX_VIAGGIATORI),
				new ThreadPoolExecutor.AbortPolicy());
		for (int i = 0; i < TRAV_NUM; i++) {
			Viaggiatore v = new Viaggiatore(i);
			try {
				service.execute(v);
			} catch (RejectedExecutionException re) {
				System.err.printf("Viaggiatore no. %d: sala esaurita\n", v.getId());
			}
			if (!ThreadUtils.Sleep(TRAV_ARR_DELAY)) {
				System.err.println("Sleep interrotta!");
				System.exit(1);
			}
		}
		ThreadPoolUtils.shutdown(service, TERM_DELAY);
	}
}