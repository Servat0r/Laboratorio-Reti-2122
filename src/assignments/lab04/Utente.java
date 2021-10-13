package assignments.lab04;

import java.util.concurrent.ThreadLocalRandom;

public abstract class Utente implements Runnable {

	/*
	 * MAX_STAYING_TIME = massimo intervallo (ms) di permanenza di un utente nel laboratorio
	 * MAX_DELAY_TIME = massimo intervallo (ms) fra due accessi consecutivi al laboratorio
	 * MAX_K/MIN_K = massimo/minimo numero di accessi al laboratorio da parte di un singolo utente
	 */
	public static final int MAX_STAYING_TIME = 1000;
	public static final int MAX_DELAY_TIME = 2000; //<= 2 secondi fra due accessi consecutivi al laboratorio
	public static final int MIN_K = 1;
	public static final int MAX_K = 10;
	
	public abstract int getK();
	
	//Utility per le stampe a schermo
	public abstract String str();
	
	/*
	 * Metodi di ingresso e uscita nel laboratorio; per ognuno di essi si richiede:
	 * - restituire true in caso di successo, false altrimenti;
	 * - restituire false in caso di InterruptedException. 
	 */
	public abstract boolean entra();
	public abstract boolean esci();
		
	public void run() {
		for (int i = 1; i <= getK(); i++) {
			if (!this.entra()) {
				System.out.printf("%s[id = %d][iterazione = %d] - FALLITO ingresso nel laboratorio%n", str(),
						Thread.currentThread().getId(), i);
				return;
			}
			System.out.printf("%s[id = %d][iterazione = %d] - ingresso nel laboratorio%n", str(),
					Thread.currentThread().getId(), i);
			
			try {
				Thread.sleep(ThreadLocalRandom.current().nextInt(Utente.MAX_STAYING_TIME));
			} catch (InterruptedException e) { }
			finally {
				if (!this.esci()) {
					System.out.printf("%s[id = %d][iterazione = %d] - FALLITA uscita dal laboratorio%n", str(),
							Thread.currentThread().getId(), i);
					return;
				}
			}
			
			System.out.printf("%s[id = %d][iterazione = %d] - uscita dal laboratorio%n", str(),
					Thread.currentThread().getId(), i);
			
			try {
				if (i < getK()) Thread.sleep(ThreadLocalRandom.current().nextInt(Utente.MAX_DELAY_TIME));
			} catch (InterruptedException e) { }
		}
		System.out.printf("%s[id = %d] - in terminazione%n", str(),
				Thread.currentThread().getId());
	}
}