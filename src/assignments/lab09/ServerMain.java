package assignments.lab09;

public final class ServerMain {

	public static void main(String[] args) {
		/* Determinazione porta */
		int port;
		try { port = Integer.parseInt(args[0]); }
		catch (RuntimeException ex) {port = Server.DEFAULT_PORT; }
		
		/* Creazione e utilizzo server */
		try (Server server = new Server(port)){
			boolean retval = server.run();
			if (!retval) System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}