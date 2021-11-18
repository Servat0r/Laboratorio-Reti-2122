package assignments.lab09;

import java.nio.*;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Scanner;

import util.common.Common;

import java.net.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Client implements AutoCloseable {
	
	public static final String DFL_HOST = "localhost";
	public static final int DFL_PORT = 1919;
	
	public static final int BYTE_BUF_CAP = 1024;
	
	private SocketChannel client;
	private ByteBuffer sendBuf;
	private ByteBuffer recvBuf;
	
	public Client(String host, int port) throws IOException {
		Common.notNull(host);
		if (port <= 0) throw new IllegalArgumentException();
		SocketAddress address = new InetSocketAddress(host, port);
		this.client = SocketChannel.open(address);
		this.sendBuf = ByteBuffer.allocate(BYTE_BUF_CAP);
		this.recvBuf = ByteBuffer.allocate(BYTE_BUF_CAP);
	}
	
	public Client() throws IOException { this(DFL_HOST, DFL_PORT); }
	
	private int nextSend(ReadableByteChannel inCh) throws IOException {
		int r = inCh.read(this.sendBuf);
		if (r >= 0) {
			this.sendBuf.flip();
			while (this.sendBuf.hasRemaining()) this.client.write(this.sendBuf);
			this.sendBuf.clear();
		}
		return r;
	}
	
	private int nextRecv(byte[] recvBytes) throws IOException {
		int r = this.client.read(this.recvBuf);
		if (r >= 0) {
			this.recvBuf.flip();
			this.recvBuf.get(recvBytes, 0, this.recvBuf.remaining());
			this.recvBuf.clear();
		}
		return r; //Che indicherà quanti elementi significativi ci sono in recvBytes!
	}
	
	public boolean run() {
		try (
				Scanner s = new Scanner(System.in);
				ReadableByteChannel inCh = Channels.newChannel( new ByteArrayInputStream( s.nextLine().getBytes() ) )
		){
			while (this.nextSend(inCh) > -1) {}
			this.client.shutdownOutput(); //Il client non invierà più dati!
			byte[] recvbytes = new byte[BYTE_BUF_CAP];
			int recvlen;
			while ((recvlen = this.nextRecv(recvbytes)) > -1) {
				System.out.print(new String(recvbytes, 0, recvlen));
				Arrays.fill(recvbytes, (byte)0);
			}
			this.client.shutdownInput();
			this.client.close();
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