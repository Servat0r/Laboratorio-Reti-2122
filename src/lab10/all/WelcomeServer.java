package lab10.all;

import java.io.IOException;
import java.net.*;

import util.common.Common;

public final class WelcomeServer implements AutoCloseable {
	
	public static final int PORT = 4000;
	
	private static final int WAIT_TIME = 5000;
	
	public static final String MCAST_IP = "239.255.1.3";
	
	public static final String DFL_MSG = "WELCOME";
	
	private MulticastSocket socket;
	private final String msg;
	
	public WelcomeServer(int port, String msg) throws IOException {
		Common.notNull(msg);
		this.socket = new MulticastSocket(port);
		this.msg = msg;
	}

	@SuppressWarnings("deprecation")
	public void run(String mcastIP) throws IOException, InterruptedException {
		InetAddress ia = InetAddress.getByName(mcastIP);
		this.socket.joinGroup(ia);
		DatagramPacket dp = new DatagramPacket(this.msg.getBytes(), this.msg.length(), ia, this.socket.getLocalPort());
		while (true) {
			this.socket.send(dp);
			System.out.println("Message sent");
			Thread.sleep(WAIT_TIME);
		}
	}

	public void close() throws Exception { this.socket.close(); }
	
	public static void main(String[] args) {
		try (WelcomeServer server = new WelcomeServer(PORT, DFL_MSG)){ server.run(MCAST_IP); }
		catch (Exception ex) { ex.printStackTrace(System.out); }
	}
}