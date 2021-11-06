package assignments.lab07;

import java.net.UnknownHostException;

public final class PingClientMain {

	private static final String ERR_MSG_0 = "ERR -arg 0";
	private static final String ERR_MSG_1 = "ERR -arg 1";
	
	private static void errorExit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	public static void main(String[] args) {
		String serverName = null;
		int serverPort = 0;
		if (args.length == 0) errorExit(ERR_MSG_0);
		else {
			try {
				serverName = args[0];
				if (args.length < 2) errorExit(ERR_MSG_1);
				serverPort = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) { errorExit(ERR_MSG_1); }
		}
		try (PingClient client = new PingClient(serverName, serverPort)){
			boolean retval = client.mainloop();
			if (!retval) System.exit(1);
		} catch (UnknownHostException uhe) { errorExit(ERR_MSG_0); }
		catch (Exception e) { e.printStackTrace(); System.exit(1); }
	}
}