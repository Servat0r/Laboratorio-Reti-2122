package assignments.lab09;

import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

import util.common.*;


public class Client implements AutoCloseable {

	private static final int BYTE_BUF_CAP = 1024;
	private static final long DFL_TIMEOUT = 3000;
	
	public static final String DFL_HOST = "localhost";
	public static final int DFL_PORT = 1919;
	
	private SocketChannel client;
	private Selector selector;
	private MessageBuffer msgBuf;
	private final long timeout;
	
	public Client(String host, int port, long timeout) throws IOException {
		Common.notNull(host);
		if (port <= 0) throw new IllegalArgumentException();
		SocketAddress address = new InetSocketAddress(host, port);
		this.client = SocketChannel.open(address);
		this.client.configureBlocking(false);
		this.selector = Selector.open();
		this.client.register(selector, SelectionKey.OP_READ);
		this.msgBuf = new MessageBuffer(BYTE_BUF_CAP);
		this.timeout = timeout;
	}
	
	public Client(String host, int port) throws IOException { this(host, port, DFL_TIMEOUT); }
	
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
		if (this.selector.select(this.timeout) == 0) return -1;
		Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
		iterator.next();
		iterator.remove();
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