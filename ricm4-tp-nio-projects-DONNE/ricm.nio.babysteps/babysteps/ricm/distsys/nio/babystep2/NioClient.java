package ricm.distsys.nio.babystep2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * NIO elementary client RICM4 TP F. Boyer
 */

public class NioClient {

	// The channel used to communicate with the server
	private SocketChannel sc;
	private SelectionKey scKey;

	// Java NIO selector
	private Selector selector;

	// The message to send to the server
	byte[] first;
	
	int nloops;
	
	private Reader r;
	private Writer w;

	/**
	 * NIO client initialization
	 * 
	 * @param serverName: the server name
	 * @param port: the server port
	 * @param msg: the message to send to the server
	 * @throws IOException
	 */
	public NioClient(String serverName, int port, byte[] payload) throws IOException {

		this.first = payload;

		// create a new selector
		selector = SelectorProvider.provider().openSelector();

		// create a new non-blocking server socket channel
		sc = SocketChannel.open();
		sc.configureBlocking(false);

		// register an connect interested in order to get a
		// connect event, when the connection will be established
		scKey = sc.register(selector, SelectionKey.OP_CONNECT);

		w = new Writer(scKey);
		r = new ReaderClient(scKey, w);
		
		// request a connection to the given server and port
		InetAddress addr;
		addr = InetAddress.getByName(serverName);
		sc.connect(new InetSocketAddress(addr, port));
	}

	/**
	 * The client forever-loop on the NIO selector - wait for events on registered
	 * channels - possible events are ACCEPT, CONNECT, READ, WRITE
	 */
	public void loop() throws IOException {
		System.out.println("NioClient running");
		while (true) {
			selector.select();

			// get the keys for which an event occurred
			Iterator<?> selectedKeys = this.selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = (SelectionKey) selectedKeys.next();
				// process key's events
				if (key.isValid()) {
					if (key.isAcceptable())
						handleAccept(key);
					if (key.isReadable())
						handleRead(key);
					if (key.isWritable())
						handleWrite(key);
					if (key.isConnectable())
						handleConnect(key);
				}
				// remove the key from the selected-key set
				selectedKeys.remove();
			}
		}
	}

	/**
	 * Accept a connection and make it non-blocking
	 * 
	 * @param the key of the channel on which a connection is requested
	 */
	private void handleAccept(SelectionKey key) throws IOException {
		throw new Error("Unexpected accept");
	}

	/**
	 * Finish to establish a connection
	 * 
	 * @param the key of the channel on which a connection is requested
	 */
	private void handleConnect(SelectionKey key) throws IOException {
		assert (this.scKey == key);
		assert (sc == key.channel());
		sc.finishConnect();
		key.interestOps(SelectionKey.OP_READ);

		// when connected, send a message to the server
		byte[] digest = Reader.md5(first);
		r.setDigest(digest);
		w.sendMsg(first);
	}

	/**
	 * Handle incoming data event
	 * 
	 * @param the key of the channel on which the incoming data waits to be received
	 */
	private void handleRead(SelectionKey key) throws IOException {
		assert (this.scKey == key);
		assert (sc == key.channel());
		
		r.handleRead(key);
	}

	/**
	 * Handle outgoing data event
	 * 
	 * @param the key of the channel on which data can be sent
	 */
	private void handleWrite(SelectionKey key) throws IOException {
		assert (this.scKey == key);
		assert (sc == key.channel());
		
		w.handleWrite(key);
	}

	public static void main(String args[]) throws IOException {
		int serverPort = NioServer.DEFAULT_SERVER_PORT;
		String serverAddress = "localhost";
		String msg = "Hello There1... Hello There2... Hello There3... Hello There4... Hello There5... Hello There6... Hello There7... Hello There8... Hello There9... Hello There10...";
		String arg;

		for (int i = 0; i < args.length; i++) {
			arg = args[i];

			if (arg.equals("-m")) {
				msg = args[++i];
			} else if (arg.equals("-p")) {
				serverPort = new Integer(args[++i]).intValue();
			} else if (arg.equals("-a")) {
				serverAddress = args[++i];
			}
		}
		byte[] bytes = msg.getBytes(Charset.forName("UTF-8"));
		NioClient nc;
		nc = new NioClient(serverAddress, serverPort, bytes);
		nc.loop();
	}


}
