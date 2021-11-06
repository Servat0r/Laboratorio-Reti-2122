package assignments.lab07;

public final class PingServerMain {

	private static final String ERR_MSG_0 = "ERR -arg 0";
	private static final String ERR_MSG_1 = "ERR -arg 1";
		
	private static void errorExit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	public static void main(String[] args) {
		int port = 0;
		long seed = PingServer.DFL_SEED;
		double lostProb = PingServer.DFL_LOST_PROB;
		
		if (args.length == 0) errorExit(ERR_MSG_0);
		else {
			try {port = Integer.parseInt(args[0]);}
			catch (NumberFormatException nfe) {errorExit(ERR_MSG_0);}
		}
		if (args.length >= 2) {
			try {seed = Long.parseLong(args[1]);}
			catch (NumberFormatException nfe) {errorExit(ERR_MSG_1);}
		}
		try (PingServer server = new PingServer(port, seed, lostProb)) {
			boolean retval = server.mainloop();
			if (!retval) System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}