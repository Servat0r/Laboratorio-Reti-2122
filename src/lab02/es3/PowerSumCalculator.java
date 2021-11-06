package lab02.es3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import util.threads.ThreadPoolUtils;

/**
 * Esempio di classe per calcolare la somma n^A + ... + n^B, dove n è un numero reale e (A,B) è una coppia di
 * interi, con A <= B, che rappresentano rispettivamente il minimo e il massimo esponente.
 * @author Salvatore Correnti
 *
 */
public final class PowerSumCalculator {

	/* Esponente minimo della somma */
	private static final int MIN_EXPONENT = 2;
	private static final int MAX_EXPONENT = 50;
	private static final int TERM_DELAY = 1000;
	
	public static void main(String[] args) { //Supponiamo che il numero da passare sia dato come primo argomento
		
		if (args.length != 1) {System.err.println("Fornisci n come unico argomento"); System.exit(1); }
		
		double n = Double.parseDouble(args[0]);
		System.out.printf("n = %f\n", n);
		List<Future<Double>> results = new ArrayList<Future<Double>>();
		
		ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int i = MIN_EXPONENT; i <= MAX_EXPONENT; i++) {
			results.add(pool.submit(new Power(n, i)));
		}
		double retval = 0.0;
		for (Future<Double> f : results) {
			try {
				retval += f.get().doubleValue();
			} catch (Exception e) {
				System.err.println("Errore durante il calcolo dell'espressione:");
				e.printStackTrace();
				System.exit(1);
			}
		}
		System.out.printf("Il risultato dell'operazione è: %f", retval);
		ThreadPoolUtils.shutdown(pool, TERM_DELAY);
	}
}