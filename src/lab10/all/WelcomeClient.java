package lab10.all;

import java.io.IOException;
import java.net.*;

public final class WelcomeClient implements AutoCloseable {

	public static final int PORT = WelcomeServer.PORT;
	
	public static final String MCAST_IP = WelcomeServer.MCAST_IP;
	
	private static final int BUF_LEN = 1024;
	
	private MulticastSocket socket;
	private byte[] buffer;
	
	public WelcomeClient(int port) throws IOException {
		this.socket = new MulticastSocket(port);
		this.buffer = new byte[BUF_LEN];
	}
	
	@SuppressWarnings("deprecation")
	public void run(String mcastIp) throws IOException {
		DatagramPacket dp = new DatagramPacket(this.buffer, this.buffer.length);
		InetAddress ia = InetAddress.getByName(mcastIp);
		this.socket.joinGroup(ia);
		this.socket.receive(dp);
		System.out.println(new String(dp.getData()));
	}
	
	public void close() throws Exception { this.socket.close(); }

	public static void main(String[] args) {
		try (WelcomeClient client = new WelcomeClient(PORT)){ client.run(MCAST_IP); }
		catch (Exception ex) { ex.printStackTrace(System.out); }		
	}
}