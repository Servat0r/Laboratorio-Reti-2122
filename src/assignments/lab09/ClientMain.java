package assignments.lab09;

import java.io.*;
import java.nio.channels.*;
import java.util.Scanner;

public final class ClientMain {
	
	private static final String QUIT_STR = "quit";
	private static final String HELP_STR = "Enter a message to send to the server. Enter '" + QUIT_STR + "' to exit.";
	private static final String PROMPT_STR = ">>> ";
	
	public static void main(String[] args) {
		/* Usage: <programName> [<hostName> [<portName>]] */
		/* Determining host and port. */
		String host = (args.length > 0 ? args[0] : Client.DFL_HOST); 
		int port = Client.DFL_PORT;
		try { port = Integer.parseInt(args[1]); }
		catch (RuntimeException ex) { port = Client.DFL_PORT; }
		
		try (
			Scanner s = new Scanner(System.in);
			Client c = new Client(host, port);
		){
			String msg;
			System.out.println(HELP_STR);
			/* Accepts console input until "quit". */
			while (true) {
				System.out.print(PROMPT_STR);
				msg = s.nextLine();
				if (msg.equals(QUIT_STR)) break;
				try (ReadableByteChannel input = Channels.newChannel(new ByteArrayInputStream(msg.getBytes()))){
					boolean b = c.run(input);
					if (!b) System.exit(1);
				} catch (Exception ex) {
					ex.printStackTrace();
					System.exit(1);
				}
			}
			/* Client channel is auto-closed. */
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
}