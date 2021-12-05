package assignments.lab10;

import java.io.IOException;
import java.net.*;

import util.common.*;

public final class TimeClient implements AutoCloseable {
	
	private static final int BUF_LEN = 1024;
	private static final int DFL_SO_TIMEOUT = 15000;
	private static final String TIMEOUT_MSG = "Timeout expired when waiting for message";
	
	public static final int DFL_PORT = TimeServer.DFL_PORT;
	public static final int RECV_NTIMES = 10;
	
	private MulticastSocket socket;
	private byte[] buffer;
	
	public TimeClient(int port) throws IOException {
		Common.positive(port);
		this.socket = new MulticastSocket(port);
		this.socket.setSoTimeout(DFL_SO_TIMEOUT);
		this.buffer = new byte[BUF_LEN];
	}
	
	public TimeClient() throws IOException { this(DFL_PORT); }
		
	@SuppressWarnings("deprecation")
	public void run(String mcastIP) throws IOException {
		Common.notNull(mcastIP);
		InetAddress address = InetAddress.getByName(mcastIP);
		DatagramPacket dp = new DatagramPacket(this.buffer, this.buffer.length);
		this.socket.joinGroup(address);
		for (int i = 0; i < RECV_NTIMES; i++) {
			try { this.socket.receive(dp); }
			catch (SocketTimeoutException ste) {
				System.out.println(TIMEOUT_MSG);
				break;
			}
			System.out.println(new String(dp.getData()));
		}
		this.socket.leaveGroup(address);
	}
	
	public void close() throws Exception {
		this.socket.close();
	}	
}