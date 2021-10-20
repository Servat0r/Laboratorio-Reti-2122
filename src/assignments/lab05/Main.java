package assignments.lab05;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
	
	/* Path (relativo) del file di output contenente tutti i file individuati. */
	private static final String OUT_PATH = "files.txt";
	
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
		File files_out = new File(OUT_PATH);
		FileOutputStream fsout = null;
		try {
			files_out.createNewFile();
			fsout = new FileOutputStream(files_out);
		} catch (IOException ioe) { //L'oggetto FileOutputStream non è mai stato creato!
			System.err.println("Unable to create output file");
			ioe.printStackTrace();
			System.exit(1);
		}
		for (int i = 0; i < k; i++) {
			consumers.add(new Thread(new Consumer(queue, fsout)));
		}
		producer.start();
		for (Thread c : consumers) c.start();
		try {
			//Aspetta che tutti gli altri thread terminino per chiudere l'oggetto FileOutputStream.
			producer.join();
			for (Thread c : consumers) c.join();
			fsout.close();
		} catch (IOException ioe) {
			System.err.println("Error when closing file stream");
			ioe.printStackTrace();
			System.exit(1);
		} catch (InterruptedException ie) {
			System.err.println("Interruption occurred when joining producer and consumers");
			ie.printStackTrace();
			System.exit(1);
		}
	}
}