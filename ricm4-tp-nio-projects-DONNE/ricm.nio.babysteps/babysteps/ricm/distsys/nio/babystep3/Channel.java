package ricm.distsys.nio.babystep3;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class Channel {
	
	Reader r;
	Writer w;
	
	public Channel(Reader r, Writer w) {
		this.r = r;
		this.w = w;
	}

	public void sendMsg(byte[] msg) {
		w.sendMsg(msg);
	}

	public void handleRead(SelectionKey key) throws IOException {
		r.handleRead(key);
	}

	public void handleWrite(SelectionKey key) throws IOException {
		w.handleWrite(key);
	}

}
