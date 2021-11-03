package assignments.lab07;

import java.io.IOException;
import java.net.*;
import java.util.*;

import util.DatagramSocketAlarm;

public final class PingClient {

	public static final String ERR_MSG_0 = "ERR -arg 0";
	public static final String ERR_MSG_1 = "ERR -arg 1";
	
	public static final int DFL_SO_TIMEOUT = 2000; //2 sec
	public static final int DFL_PING_TIMEOUT = 15000; //15 sec
	
	public static final int NUM_PINGS = 10;
	
	public static final int RECV_BUF_LENGTH = 1024;
	
	private static final String finalMsg = "%d packets transmitted, %d packets received, %f%% packet loss, round-trip (ms) min/avg/max = %d/%f/%d";
	private static final String finalMsgInfinity = "%d packets transmitted, %d packets received, %f%% packet loss, round-trip (ms) min/avg/max = +oo/+oo/+oo";
	
	private static void errorExit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
	
	private static double average(List<Long> items) {
		if (items == null) throw new NullPointerException();
		double avg = 0;
		for (Long db : items) avg += db;
		return avg / items.size();		
	}
	
	public static void main(String[] args) {
		String serverName = null;
		int port = 0;
		InetAddress serverAddress = null;
		if (args.length == 0) errorExit(ERR_MSG_0);
		else {
			try {
				serverName = args[0];
				serverAddress = InetAddress.getByName(serverName);
				if (args.length < 2) errorExit(ERR_MSG_1);
				port = Integer.parseInt(args[1]);
			} catch (UnknownHostException uhe) { errorExit(ERR_MSG_0); }
			catch (NumberFormatException nfe) { errorExit(ERR_MSG_1); }
		}
		//Now serverName, port and serverAddress are correctly assigned!
		//System.out.printf("NAME = %s : PORT = %d : ADDRESS = %s%n", serverName, port, serverAddress.toString()); //TODO Debug
		DatagramPacket currentRequest;
		DatagramPacket currentResponse;
		int transmitted = 0;
		int received = 0;
		List<Long> RTTs = new ArrayList<>();
		long currentRTT = 0;
		try (DatagramSocket socket = new DatagramSocket(0)) {
			socket.setSoTimeout(DFL_SO_TIMEOUT);
			Thread alarm = new Thread(new DatagramSocketAlarm(socket, DFL_PING_TIMEOUT));
			alarm.setDaemon(true);
			alarm.start();
			for (int seqno = 0; seqno < NUM_PINGS; seqno++) {
				String timestamp = new Date().toString();
				String currentString = "PING " + seqno + timestamp;
				currentRequest = new DatagramPacket(currentString.getBytes(), currentString.length(), serverAddress, port);
				currentResponse = new DatagramPacket(new byte[RECV_BUF_LENGTH], RECV_BUF_LENGTH);
				try {
					currentRTT = System.currentTimeMillis();
					socket.send(currentRequest);
					transmitted++;
					socket.receive(currentResponse);
					received++;
					currentRTT = System.currentTimeMillis() - currentRTT;
					RTTs.add(currentRTT);
					String recvmsg = new String(currentResponse.getData(), 0, currentResponse.getLength(), "Us-ASCII");
					System.out.printf("%s%n", recvmsg);
				} catch (SocketTimeoutException se) { System.out.println("*"); }
				catch (IOException ioe) { //Socket closed or similar
					if (socket.isClosed()) break;
					else ioe.printStackTrace();
				}
			}
			double lostRate = 100.0 * (1.0 - ((double)received)/((double)transmitted));
			System.out.println("-----PING STATISTICS-----");
			if (RTTs.size() == 0) {
				System.out.printf(finalMsgInfinity, transmitted, received, lostRate); 
			} else {
				System.out.printf(finalMsg, transmitted, received, lostRate, Collections.min(RTTs),  average(RTTs), Collections.max(RTTs));
			}
		} catch (IOException se) { se.printStackTrace(); }
	}
}