package ricm.channels.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;

public class Broker implements IBroker, Runnable {
	
	String host;
	IBrokerListener l;
	Selector selector;
	
	public Broker(String host) {
		this.host = host;
		try {
			selector = SelectorProvider.provider().openSelector();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			loop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loop() throws IOException {
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
	
	private void handleWrite(SelectionKey key) throws IOException {
		Channel c = (Channel) key.attachment();
		c.handleWrite(key);
	}

	private void handleRead(SelectionKey key) throws IOException {
		Channel c = (Channel) key.attachment();
		c.handleRead(key);		
	}

	private void handleAccept(SelectionKey key) throws IOException {
		
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc = ssc.accept();
		sc.configureBlocking(false);
		
		SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ);
		Channel c = new Channel(scKey);
		scKey.attach(c);
		l.accepted(c);
	}
	
	private void handleConnect (SelectionKey key) throws IOException {
		
		SocketChannel sc = (SocketChannel) key.channel();
		sc.finishConnect();
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		
		Channel c = new Channel(key);
		key.attach(c);
		l.connected(c);
	}
	
	@Override
	public boolean accept(int port) {
		
		ServerSocketChannel ssc;
		try {
			ssc = ServerSocketChannel.open();
			ssc.configureBlocking(false);
			
			InetAddress hostAddr = InetAddress.getByName(host);
			InetSocketAddress isa = new InetSocketAddress(hostAddr, port);
			ssc.socket().bind(isa);
			ssc.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return true;
	}

	@Override
	public boolean connect(String host, int port) {
		
		SocketChannel sc;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_CONNECT);
			
			InetAddress addr = InetAddress.getByName(host);
			sc.connect(new InetSocketAddress(addr, port));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void setListener(IBrokerListener l) {
		this.l = l;
	}

}
