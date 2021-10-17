package assignments.lab05;

import java.io.File;

public final class Consumer implements Runnable {

	private LinkedSyncQueue<File> queue; //Coda contenente i nomi delle cartelle.
	private final int id; //Id univoco del consumatore.
	private static int NEXT_ID = 1; //Campo statico per l'assegnazione di un id univoco a ogni consumatore.
	
	public Consumer(LinkedSyncQueue<File> queue) {
		if (queue == null) throw new NullPointerException();
		this.queue = queue;
		this.id = NEXT_ID++;
	}
	
	public void run() {
		File nextdir;
		File[] files;
		try {
			while (true) {
				nextdir = this.queue.get();
				if (nextdir == null) break; //Queue has been closed by anyone
				files = nextdir.listFiles();
				for (File f : files) {
					if (!f.isDirectory()) {
						System.out.printf("FILE FOUND [#%d]: '%s' : '%s'%n", this.id, f.getName(), nextdir.getPath());
					}
				}
			}
		} catch (InterruptedException ie) {
			System.err.printf("Interruption occurred for #%d CONSUMER thread", this.id);
			this.queue.close(); //Notifica agli altri thread che è necessario terminare.
			return;
		}
	}
}