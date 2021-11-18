package assignments.lab09;

import java.nio.channels.*;
import java.net.*;
import java.util.*;
import util.common.MessageBuffer;
import java.io.IOException;

public class Server implements AutoCloseable {
	
	public static final int DEFAULT_PORT = 1919;
	
	private static final int BYTE_BUF_CAP = 1024; //1KB
	
	private static final String ECHO_STR = " echoed by server";
	
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
	
	private void closeConnection(SelectionKey key) throws IOException {
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
						SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
						MessageBuffer[] buffers = new MessageBuffer[] {new MessageBuffer(BYTE_BUF_CAP), new MessageBuffer(ECHO_STR.length())};
						buffers[1].readFromArray(ECHO_STR.getBytes(), 0, ECHO_STR.length());
						clientKey.attach(buffers);
					} else {
						SocketChannel client = (SocketChannel) key.channel();
						MessageBuffer[] buffers = (MessageBuffer[]) key.attachment();
						if (key.isReadable() && key.isWritable()) {
							if (buffers[0].hasRemaining()) buffers[0].writeToChannel(client);
							if (buffers[0].readFromChannel(client) == -1) key.interestOps(SelectionKey.OP_WRITE);
						} else if (key.isReadable()) {
							if (buffers[0].readFromChannel(client) == -1) key.interestOps(SelectionKey.OP_WRITE);
							else key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						} else if (key.isWritable()) {
							if (buffers[0].hasRemaining()) buffers[0].writeToChannel(client);
							else if (buffers[1].hasRemaining()) buffers[1].writeToChannel(client);
							else {
								try { closeConnection(key); } catch (IOException ioex) { return false; }
							}
						}
					}
				} catch (IOException | NotYetConnectedException ex) {
					try { closeConnection(key); } catch (IOException cex) { return false; }
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