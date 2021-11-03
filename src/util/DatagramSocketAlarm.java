package util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

/**
 * A general task for sending an interrupt to a thread after a timeout.
 * @author Salvatore Correnti.
 *
 */
public final class DatagramSocketAlarm implements Runnable {

	private DatagramSocket socket;
	private int timeval;
	
	
	public DatagramSocketAlarm(DatagramSocket socket, int timeval) {
		if (socket == null || timeval <= 0) throw new IllegalArgumentException(); 
		this.socket = socket;
		this.timeval = timeval;
	}
		
	public void run() {
		try {
			Thread.sleep(this.timeval);
			this.socket.close();
		} catch (Exception e) {}
	}

}
