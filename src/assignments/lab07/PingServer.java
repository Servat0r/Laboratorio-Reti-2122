package assignments.lab07;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Random;

public final class PingServer implements AutoCloseable {
	
	public static final long DFL_SEED = 1000;
	public static final double DFL_LOST_PROB = 0.25;
	public static final int SERVER_MAX_BUF_LENGTH = 1024;
		
	private int port;
	private long seed;
	private double lostProb;
	private DatagramSocket socket;
	
	public PingServer(int port, long seed, double lostProb) throws IOException {
		this.port = port;
		this.seed = seed;
		this.lostProb = lostProb;
		this.socket = new DatagramSocket(this.port);
	}
	
	public PingServer(int port, long seed) throws IOException { this(port, seed, DFL_LOST_PROB); }
	
	public PingServer(int port) throws IOException { this(port, DFL_SEED, DFL_LOST_PROB); }
	
	public boolean mainloop() {
		double isLost = 0.0; //Probability to lost packet at each step
		long delay = 0;
		Random r = new Random(seed);
		while (true) {
			try {
				DatagramPacket request = new DatagramPacket(new byte[PingClient.CLIENT_MAX_BUF_LENGTH], PingClient.CLIENT_MAX_BUF_LENGTH);
				this.socket.receive(request);
				DatagramPacket response = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());
				isLost = Math.random();
				if (isLost <= lostProb) continue; //Ignore packet (simulates packet lost)
				delay = (Math.abs(r.nextLong()) % PingClient.DFL_SO_TIMEOUT);
				Thread.sleep(delay);
				System.out.println("ricevuto un pacchetto da" + request.getAddress() + request.getPort());
				socket.send(response);
			} catch (InterruptedException ie) {ie.printStackTrace(); return false; }
			catch (IOException | RuntimeException ex) {ex.printStackTrace(); return false;}
		}
	}
	
	public synchronized void close() throws Exception {
		if (!this.socket.isClosed()) this.socket.close();
	}
}