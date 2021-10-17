package assignments.lab05;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Main {
	
	/*
	 * Input da riga di comando: si assume che l'input da riga di comando sia della forma:
	 * <nome_programma> <dirpath> [<k>], dove <dirpath> è una stringa che rappresenta il
	 * path (relativo) della cartella da analizzare e <k> è un intero positivo che rappresenta
	 * il numero di thread consumatori da creare; se <k> non viene fornito, di default vengono
	 * creati DEFAULT_K consumatori.
	 */
	
	/* Valore di default per k (#consumatori) quando non fornito da riga di comando. */
	private static final int DEFAULT_K = 4;
	
	public static void main(String[] args) {
		String dirpath;
		int k;
		LinkedSyncQueue<File> queue;
		if ((args.length < 1) || (args.length >= 3)) {
			System.err.println("Usage: <program_name> <path> [<k>], where <path> is a string representing"
					+ " a directory and <k> is a positive integer representing number of consumer threads"
					+ " (default is 4)");
			System.exit(1);
		}
		dirpath = args[0];
		if (args.length == 2) k = Integer.parseInt(args[1]); //NumberFormatException if it does NOT represent an integer
		else k = DEFAULT_K;
		if (k <= 0) {
			System.err.println("Error: <k> must be positive!");
			System.exit(1);
		}
		queue = new LinkedSyncQueue<File>(); 
		Thread producer = new Thread(new Producer(queue, dirpath));
		List<Thread> consumers = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			consumers.add(new Thread(new Consumer(queue)));
		}
		producer.start();
		for (Thread c : consumers) c.start();
	}
}