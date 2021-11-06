package assignments.lab07;

import java.io.IOException;
import java.net.*;
import java.util.*;

import util.common.AutoCloseableAlarm;

public final class PingClient implements AutoCloseable {

	/* Final messages to be print when packets transmission ends */
	private static final String finalMsgHeader = "-----PING STATISTICS-----";
	private static final String finalMsg = "%d packets transmitted, %d packets received, %.2f%% packet loss, round-trip (ms) min/avg/max = %d/%.2f/%d";
	private static final String finalMsgInfinity = "%d packets transmitted, %d packets received, %f%% packet loss, round-trip (ms) min/avg/max = +oo/+oo/+oo";
	
	public static final int DFL_SO_TIMEOUT = 2000; /* Default timeout when no response is received */
	public static final int DFL_PING_TIMEOUT = 15000; /* Deafult timeout for ping activity */
	public static final int DFL_PINGS_NUM = 10; /* Default number of ping attempts */
	public static final int CLIENT_MAX_BUF_LENGTH = 1024; /* Receive buffer length (bytes) */
	
	private String serverName;
	private int serverPort;
	private InetAddress serverAddress;
	private DatagramSocket socket;
	private int pingsNum;

	private static double average(List<Long> items) {
		if (items == null) throw new NullPointerException();
		double avg = 0;
		for (Long db : items) avg += db;
		return avg / items.size();		
	}
	
	public PingClient(String serverName, int serverPort, int pingsNum) throws UnknownHostException, SocketException {
		this.serverName = serverName;
		this.serverPort = serverPort;
		this.serverAddress = InetAddress.getByName(this.serverName);
		this.socket = new DatagramSocket(0);
		this.socket.setSoTimeout(DFL_SO_TIMEOUT);
		this.pingsNum = pingsNum;
	}
	
	public PingClient(String serverName, int serverPort) throws UnknownHostException, SocketException { this(serverName, serverPort, DFL_PINGS_NUM); } 
	
	/**
	 * Mainloop of client program.
	 * @return true on success, false on error.
	 */
	public boolean mainloop() {
		DatagramPacket currentRequest;
		DatagramPacket currentResponse;
		int transmitted = 0;
		int received = 0;
		List<Long> RTTs = new ArrayList<>();
		long currentRTT = 0;
		try {
			Thread alarm = new Thread(new AutoCloseableAlarm(this, DFL_PING_TIMEOUT));
			alarm.setDaemon(true);
			alarm.start();
			for (int seqno = 0; seqno < this.pingsNum; seqno++) {
				String timestamp = new Date().toString();
				String currentString = "PING " + seqno + " " + timestamp;
				currentRequest = new DatagramPacket(currentString.getBytes(), currentString.length(), this.serverAddress, this.serverPort);
				currentResponse = new DatagramPacket(new byte[PingServer.SERVER_MAX_BUF_LENGTH], PingServer.SERVER_MAX_BUF_LENGTH);
				try {
					currentRTT = System.currentTimeMillis();
					this.socket.send(currentRequest);
					transmitted++;
					this.socket.receive(currentResponse);
					received++;
					currentRTT = System.currentTimeMillis() - currentRTT;
					RTTs.add(currentRTT);
					String recvmsg = new String(currentResponse.getData(), 0, currentResponse.getLength(), "Us-ASCII");
					System.out.printf("%s%n", recvmsg);
				} catch (SocketTimeoutException se) { System.out.println("*"); }
				catch (IOException ioe) { //Socket closed or similar
					if (this.socket.isClosed()) break;
					else { ioe.printStackTrace(); return false; }
				}
			}
			double lostRate = 100.0 * (1.0 - ((double)received)/((double)transmitted));
			System.out.println(finalMsgHeader);
			if (RTTs.size() == 0) {
				System.out.printf(finalMsgInfinity, transmitted, received, lostRate); 
			} else {
				System.out.printf(finalMsg, transmitted, received, lostRate, Collections.min(RTTs),  average(RTTs), Collections.max(RTTs));
			}
		} finally {} 
		return true;
	}
	
	public synchronized void close() throws Exception {
		if (!this.socket.isClosed()) this.socket.close();
	}
}