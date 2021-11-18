package assignments.lab09;

import java.nio.channels.*;
import java.net.*;
import java.util.*;
import util.common.MessageBuffer;
import java.io.IOException;

public class Server implements AutoCloseable {
	
	public static final int DEFAULT_PORT = 1919;
	
	private static final int BYTE_BUF_CAP = 1024; //1KB
	
	private static enum State {
		OPEN,
		CLOSED,
	};
	
	private final int port;
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private State state;
	
	public Server(int port) throws IOException {
		if (port <= 0) throw new IllegalArgumentException();
		this.port = port;
		/* ServerSocket and Selector initialization */
		this.serverChannel = ServerSocketChannel.open();
		ServerSocket ss = this.serverChannel.socket();
		InetSocketAddress address = new InetSocketAddress(this.port);
		ss.bind(address);
		this.serverChannel.configureBlocking(false);
		this.selector = Selector.open();
		this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.state = State.OPEN;
		System.out.println("Listening for connections on port " + this.port);
	}
	
	public Server() throws IOException { this(DEFAULT_PORT); }

	private synchronized boolean isClosed() { return (this.state == State.CLOSED); }
	
	private void cancelKey(SelectionKey key) throws IOException {
		key.cancel();
		key.channel().close();
	}
	
	public boolean run() {
		while (!this.isClosed()) {
			
			try { this.selector.select(); }
			catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
			
			Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						/* Va registrata un'UNICA chiave per non confondere messaggi in entrata da e in uscita verso il client */
						SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
						MessageBuffer buffer = new MessageBuffer(BYTE_BUF_CAP);
						clientKey.attach(buffer);
					} else {
						SocketChannel client = (SocketChannel) key.channel();
						MessageBuffer buffer = (MessageBuffer) key.attachment();
						if (key.isReadable()) {
							int r = buffer.readFromChannel(client);
							if (r == -1) {
								buffer.clear();
								key.interestOps(SelectionKey.OP_WRITE);
								byte[] ackBytes = "ACKNOWLEDGED".getBytes();
								buffer.readFromArray(ackBytes, 0, ackBytes.length);
							} else {
								byte[] rawData = new byte[BYTE_BUF_CAP];
								int len = buffer.writeToArray(rawData, 0, BYTE_BUF_CAP);
								System.out.print(new String(rawData, 0, len));
							}
						} else if (key.isWritable()) {
							if (buffer.hasRemaining()) buffer.writeToChannel(client);
							else {
								System.out.println();
								try { cancelKey(key); } catch (IOException ioex) { return false; }
							}
						}
					}
				} catch (IOException | NotYetConnectedException ex) {
					try { cancelKey(key); } catch (IOException cex) { return false; }
				}
			} /* iterator.hasNext() */
			
		} /* !this.isClosed() */
		return true;
	}
		
	public synchronized void close() throws Exception {
		this.selector.close();
		this.serverChannel.close();
		this.selector = null;
		this.serverChannel = null;
		this.state = State.CLOSED;
	}
}