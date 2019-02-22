package ricm.channels.impl;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class Channel implements IChannel {

	Reader r;
	Writer w;
	IChannelListener l;
	
	public Channel (SelectionKey key) {
		w = new Writer(key);
		r = new Reader(key, w);
	}
	
	@Override
	public void setListener(IChannelListener l) {
		this.l = l;
	}

	@Override
	public void send(byte[] bytes, int offset, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(byte[] bytes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean closed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void handleWrite(SelectionKey key) throws IOException {
		w.handleWrite(key);		
	}

	public void handleRead(SelectionKey key) throws IOException {
		r.handleRead(key);
	}

}
