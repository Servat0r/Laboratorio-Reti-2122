package assignments.lab10;

public final class TimeServerMain {

	private static final String USAGE = "Usage: java TimeServerMain <IP> <Port>";
	
	public static void main(String[] args) {
		//Argomenti = <MulticastIP> <Port>
		if (args.length < 1) {
			System.err.println(USAGE);
			System.exit(1);
		}
		String ip = args[0];
		int port = TimeServer.DFL_PORT;
		if (args.length >= 2) {
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) { 
				System.err.println(USAGE);
				System.exit(1);
			}
		}
		try (TimeServer server = new TimeServer(ip, port)){ server.run(); }
		catch (Exception ex) { ex.printStackTrace(System.out); }
	}

}