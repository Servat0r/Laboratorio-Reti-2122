package assignments.lab06;

import java.io.IOException;

public final class ServerMain {

	/*
	 * Assumiamo che l'input da riga di comando sia della forma: <nome_programma> [<nome_file> [<contenuto_file>]],
	 * dove <nome_file> è il nome del file da creare e <contenuto_file> è il contenuto da scrivere nel file.
	 * Se tali valori non sono forniti, ne vengono usati due di default.
	 */
	public static void main(String[] args) throws Exception {
		String rootDirectory = (args.length > 0 ? args[0] : Server.DFL_ROOT_DIR);
		try (Server s = new Server(rootDirectory); ){
			CtrlCHandler c = new CtrlCHandler(s);
			System.out.println("Server is running ...");
			s.mainloop();
		} catch (IOException ioe) {
			System.err.println("IOException occurred:");
			ioe.printStackTrace();
			System.exit(1);
		}
	}
}