package ricm.channels.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class Channel implements IChannel {

	Reader r;
	Writer w;
	IChannelListener l;
	boolean closed;
	
	public Channel (SelectionKey key) {
		w = new Writer(key);
		r = new Reader(key, this);
		closed = false;
	}
	
	@Override
	public void setListener(IChannelListener l) {
		this.l = l;
		r.setListener(l);
	}

	@Override
	public void send(byte[] bytes, int offset, int count) {
		w.sendMsg(bytes, offset, count);
	}

	@Override
	public void send(byte[] bytes) {
		w.sendMsg(bytes);
	}

	@Override
	public void close() {
		if(!closed)
			closed = true;
	}

	@Override
	public boolean closed() {
		return closed;
	}

	public void handleWrite(SelectionKey key) throws IOException {
		w.handleWrite(key);		
	}

	public void handleRead(SelectionKey key) throws IOException {
		r.handleRead(key);
	}

}
