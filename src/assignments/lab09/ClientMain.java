package assignments.lab09;

public final class ClientMain {
	
	public static void main(String[] args) {
		/* Determinazione host e porta */
		String host = (args.length > 0 ? args[0] : Client.DFL_HOST); 
		int port;
		try { port = Integer.parseInt(args[1]); }
		catch (RuntimeException ex) { port = Client.DFL_PORT; }
		
		try (Client c = new Client(host, port)){
			if (!c.run()) System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
}