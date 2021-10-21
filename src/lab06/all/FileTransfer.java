package lab06.all;

import java.io.*;
import java.util.concurrent.Callable;

/*
 * Task di trasferimento di un file su uno stream di output.
 */

public final class FileTransfer implements Callable<Long> {

	private File file;
	private OutputStream out;
	
	public FileTransfer(File file, OutputStream out) {
		if (file == null || out == null) throw new NullPointerException();
		this.file = file;
		this.out = out;
	}

	/**
	 * Trasferisce il file fornito in this.file sullo stream di output fornito su this.out.
	 * @return Il numero di bytes trasferiti (0 se il file è vuoto).
	 */
	public Long call() throws Exception {
		if (out == null) throw new NullPointerException();
		long res = 0;
		try (FileInputStream in = new FileInputStream(this.file);) {
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
				res++;
			}
		}
		return res;
	}
	
}