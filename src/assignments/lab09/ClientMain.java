package assignments.lab09;

import java.io.*;
import java.nio.channels.*;
import java.util.Scanner;

public final class ClientMain {
	
	public static void main(String[] args) {
		/* Determinazione host e porta */
		String host = (args.length > 0 ? args[0] : Client.DFL_HOST); 
		int port;
		try { port = Integer.parseInt(args[1]); }
		catch (RuntimeException ex) { port = Client.DFL_PORT; }
		
		try (
			Scanner s = new Scanner(System.in);
			Client c = new Client(host, port);
			ReadableByteChannel input = Channels.newChannel(new ByteArrayInputStream( s.nextLine().getBytes() ));
		){
			boolean b = c.run(input);
			if (!b) System.exit(1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
}