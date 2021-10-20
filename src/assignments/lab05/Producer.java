package assignments.lab05;

import java.io.File;
import java.util.LinkedList;

public final class Producer implements Runnable {
	
	private LinkedSyncQueue<File> queue;
	private String dirpath;
	
	public Producer(LinkedSyncQueue<File> queue, String dirpath) {
		if (queue == null || dirpath == null) throw new NullPointerException();
		this.queue = queue;
		this.dirpath = dirpath;
	}

	public void run() {
		File dir = new File(this.dirpath);
		if (!dir.isDirectory()) {
			System.err.printf("'%s' is not a directory%n", dirpath);
			this.queue.close(); //Notifica agli altri thread che è necessario terminare.
			return;
		}
		LinkedList<File> directories = new LinkedList<>();
		File[] subdirs;
		directories.add(dir);
		try {
			while (!directories.isEmpty()) {
				dir = directories.remove(0);
				this.queue.put(dir);
				subdirs = dir.listFiles();
				for (File f : subdirs) {
					if (f.isDirectory()) directories.add(f);
				}
			}
			this.queue.close();
		} catch (InterruptedException ie) {
			System.err.println("Interruption occurred for PRODUCER thread");
			ie.printStackTrace();
			this.queue.shutdown(); //Notifica agli altri thread che è necessario terminare.
			return;
		}
		System.out.println("Producer terminating...");
	}
}
