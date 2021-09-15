package assignment01;
import java.lang.Math;

public final class PiCalculator extends Thread {
	
	private double accuracy; /* Accuratezza del calcolo */
	private Thread mainThread; /* Riferimento al thread main */

	public PiCalculator(double accuracy) {
		this.accuracy = accuracy;
		this.mainThread = null;
	}
	
	/* 
	 * Il "worker" thread calcola la stima di pi greco finché viene interrotto oppure
	 * raggiunge l'accuratezza voluta: in quest'ultimo caso interrompe il thread main
	 * in modo da risvegliarlo se la sleep() è ancora in esecuzione e fornire subito
	 * il risultato ottenuto.
	 */
	public void run() {
		if (this.mainThread == null) {
			System.err.println("Error on setup");
			return;
		}
		double result = 0.0;
		double factor = 1.0; //Switching +1 <-> -1 at each step
		double denom = 1.0;
		while ((Math.abs(Math.PI - result) > this.accuracy) && !this.isInterrupted()) {
			result += 4.0/(denom * factor);
			denom += 2.0;
			factor *= -1.0;
		}
		if (this.isInterrupted()) System.out.println("Timeout expired");
		else this.mainThread.interrupt(); 
		System.out.print("Estimated value of PI: ");
		System.out.println(result);
	}
	
	/*
	 * Il thread main aspetta per time millisecondi, dopodiché interrompe il calcolo
	 * in esecuzione da parte del thread worker, venendo eventualmente interrotto durante
	 * l'attesa dal thread worker che ha effettuato il calcolo con l'accuratezza richiesta.
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: <program_name> <accuracy> <timeout>, where timeout is in milliseconds");
			return;
		}
		long time = Long.parseLong(args[1]);
		PiCalculator t = new PiCalculator(Double.parseDouble(args[0]));
		t.mainThread = Thread.currentThread();
		t.start();
		
		try { Thread.sleep(time); }
		catch (InterruptedException e) { }
		t.interrupt();
	}
}