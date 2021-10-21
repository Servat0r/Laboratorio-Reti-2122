package lab06.all;

import java.io.*;
import java.net.Socket;

public final class Client {

	/*
	 * DFL_PORT = porta di default per connettersi al server.
	 * DFL_ADDRESS = indirizzo di default per connettersi al server.
	 */
	public static final int DFL_PORT = 10300;
	public static final String DFL_ADDRESS = "localhost";
	
	public static void main(String[] args) throws IOException {
		String address = (args.length >= 1 ? args[0] : DFL_ADDRESS);
		int port = (args.length >= 2 ? Integer.parseInt(args[1]) : DFL_PORT);
		try (Socket socket = new Socket(address, port)){
			System.out.println("CLIENT STARTED ...\n");
			InputStream in = socket.getInputStream();
			StringBuilder sb = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) sb.append((char)c);
			System.out.println(sb.toString());
		}
		System.out.println("\nCLIENT FINISHED ...\n");
	}
}