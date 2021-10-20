package assignments.lab05;

import java.io.*;

public final class Consumer implements Runnable {

	private LinkedSyncQueue<File> queue; //Coda contenente i nomi delle cartelle.
	private final int id; //Id univoco del consumatore.
	private static int NEXT_ID = 1; //Campo statico per l'assegnazione di un id univoco a ogni consumatore.
	private FileOutputStream files_out; //FileOutputStream su cui scrivere i file trovati
	
	public Consumer(LinkedSyncQueue<File> queue, FileOutputStream files_out) {
		if ((queue == null) || (files_out == null)) throw new NullPointerException();
		this.queue = queue;
		this.id = NEXT_ID++;
		this.files_out = files_out;
	}
	
	private static StringBuilder makeFSOutString(int id, File file, File dir) {
		StringBuilder out_string = new StringBuilder("FILE FOUND [consumer #");
		out_string.append(id);
		out_string.append("]: '");
		out_string.append(file.getName());
		out_string.append("' : '");
		out_string.append(dir.getPath());
		out_string.append("'\n");
		return out_string;
	}
	
	public void run() {
		File nextdir;
		File[] files;
		StringBuilder out_string;
		boolean success = true;
		try {
			while (true) {
				nextdir = this.queue.get();
				if (nextdir == null) break; //Coda chiusa da qualcuno.
				files = nextdir.listFiles();
				for (File f : files) {
					if (!f.isDirectory()) {
						out_string = Consumer.makeFSOutString(id, f, nextdir);
						//Le scritture sul file in output da parte di diversi thread NON si sovrapporranno MAI
						synchronized (files_out) {
							System.out.println(out_string.toString());
							this.files_out.write(out_string.toString().getBytes());
							this.files_out.flush();
						}
					}
				}
			}
		} catch (InterruptedException ie) {
			System.err.printf("Interruption occurred for #%d CONSUMER thread%n", this.id);
			ie.printStackTrace();
			this.queue.shutdown(); //Notifica agli altri thread che è necessario terminare.
			success = false;
		} catch (IOException ioe) {
			System.err.printf("IOException occurred for #%d CONSUMER thread%n", this.id);
			ioe.printStackTrace();
			this.queue.shutdown(); //Notifica agli altri thread che è necessario terminare.
			success = false;
		}
		if (success) System.out.printf("Consumer #%d terminating with success ...%n", this.id);
		else System.out.printf("Consumer #%d terminating with failure ...%n", this.id);
	}
}