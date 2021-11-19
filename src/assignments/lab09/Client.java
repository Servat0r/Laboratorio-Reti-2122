package assignments.lab09;

import java.nio.channels.*;
import java.util.*;

import util.common.*;

import java.net.*;
import java.io.*;

public class Client implements AutoCloseable {
	
	public static final String DFL_HOST = "localhost";
	public static final int DFL_PORT = 1919;
	
	public static final int BYTE_BUF_CAP = 1024;
	
	private SocketChannel client;
	private MessageBuffer msgBuf;
	
	public Client(String host, int port) throws IOException {
		Common.notNull(host);
		if (port <= 0) throw new IllegalArgumentException();
		SocketAddress address = new InetSocketAddress(host, port);
		this.client = SocketChannel.open(address);
		this.msgBuf = new MessageBuffer(BYTE_BUF_CAP);
	}
	
	public Client() throws IOException { this(DFL_HOST, DFL_PORT); }
	
	private int nextSend(ReadableByteChannel input) throws IOException {
		int r = this.msgBuf.readFromChannel(input);
		if (r >= 0) {
			while (this.msgBuf.hasRemaining()) this.msgBuf.writeToChannel(this.client);
			this.msgBuf.clear();
		}
		return r;
	}
	
	private int nextRecv(byte[] recvBytes) throws IOException {
		int r = this.msgBuf.readFromChannel(this.client);
		if (r >= 0) {
			int offset = 0;
			while (this.msgBuf.hasRemaining()) {
				offset += this.msgBuf.writeToArray(recvBytes, offset, r);
			}
		}
		return r;
	}
		
	public boolean run(ReadableByteChannel input) {
/*		try (
				Scanner s = new Scanner(System.in);
				ReadableByteChannel input = Channels.newChannel( new ByteArrayInputStream( s.nextLine().getBytes() ) )
		){
*/
		try {
			int sent = 0;
			while (true) {
				int ret = this.nextSend(input);
				if (ret < 0) break;
				else sent += ret;
			}
			if (sent > 0) {
				byte[] recvbytes = new byte[BYTE_BUF_CAP];
				int recvlen;
				while ((recvlen = this.nextRecv(recvbytes)) > -1) {
					System.out.print(new String(recvbytes, 0, recvlen));
					Arrays.fill(recvbytes, (byte)0);
				}
				System.out.println();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void close() throws Exception {
		this.client.close();
	}
}