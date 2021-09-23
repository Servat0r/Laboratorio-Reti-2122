package lab02.es3;

import java.util.concurrent.Callable;
import java.lang.Math;

/**
 * Esempio di classe di tipo Callable per calcolare una potenza (intera) di un numero reale.
 * @author Salvatore Correnti
 */
public final class Power implements Callable<Double> {
	
	private final double base;
	private final int exponent;
	
	public Power(final double base, final int exponent) {
		this.base = base;
		this.exponent = exponent;
	}

	public Double call() throws Exception {
		System.out.printf("Esecuzione %f^%d in %d\n", this.base, this.exponent, Thread.currentThread().getId());
		return Double.valueOf(Math.pow(base, exponent));
	}

	
}