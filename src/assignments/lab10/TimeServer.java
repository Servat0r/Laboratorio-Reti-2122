package assignments.lab10;

import java.io.IOException;
import java.net.*;
import java.util.Date;

import util.common.Common;

public final class TimeServer implements AutoCloseable {

	private static final int DFL_WAIT_TIME = 4000; /* 4 secondi */
	public static final int DFL_PORT = 10200;
	
	private MulticastSocket socket;
	private InetAddress address;
	private int waitTime;
	
	public TimeServer(String ip, int port, int waitTime) throws IOException {
		Common.notNull(ip); Common.positive(port); Common.positive(waitTime);
		this.address = InetAddress.getByName(ip);
		if (!this.address.isMulticastAddress()) throw new IllegalArgumentException();
		this.socket = new MulticastSocket(port);
		this.waitTime = waitTime;
	}

	public TimeServer(String ip, int port) throws IOException { this(ip, port, DFL_WAIT_TIME); }
	
	public TimeServer(String ip) throws IOException { this(ip, DFL_PORT, DFL_WAIT_TIME); }

	@SuppressWarnings("deprecation")
	public void run(int nTimes) throws IOException, InterruptedException {
		Common.notNeg(nTimes);
		String now;
		DatagramPacket dp;
		int port = this.socket.getLocalPort();
		this.socket.joinGroup(this.address);
		int i = 0;
		while ((nTimes <= 0) || (i < nTimes)) {
			now = new Date().toString();
			dp = new DatagramPacket(now.getBytes(), now.length(), this.address, port);
			this.socket.send(dp);
			System.out.printf("Message sent for the #%d time%n", i++);
			Thread.sleep(this.waitTime);
		}
		this.socket.leaveGroup(this.address);
	}
	
	public void run() throws IOException, InterruptedException { this.run(0); }
	
	public void close() throws Exception {
		this.socket.close();		
	}
}