package ricm.channels.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class Reader {

	private final int NBBYTELEN = 4;
	
	protected SelectionKey key;
	
	private enum State {LEN,MSG};
	private State state;
	
	private int lengthMsg;	// Longueur du message à lire
	private int count;		// Nombre d'octet lu
	
	ByteBuffer buffLength;	// Buffer pour la longueur du message
	ByteBuffer buffMsg;		// Buffer pour le message
	
	IChannel c;
	IChannelListener l;
	
	byte[] digest;			// Tableau pour le checksum
	
	public Reader(SelectionKey key, IChannel c) {
		this.key = key;
		state = State.LEN;
		count = 0;
		buffLength = ByteBuffer.allocate(NBBYTELEN);
		this.c = c;
	}
	
	public void setListener(IChannelListener l) {
		this.l = l;
	}

	public void handleRead(SelectionKey key) throws IOException {

		// get the socket channel for the client who sent something
		SocketChannel sc = (SocketChannel) key.channel();
		
		int n;
		switch(state) {
		case LEN:
			
			n = sc.read(buffLength);
			count += n;
			
			if(count == NBBYTELEN) {
				count = 0;
				
				buffLength.rewind();
				lengthMsg = buffLength.getInt();
				buffMsg = ByteBuffer.allocate(lengthMsg);
				buffLength.rewind();
				
				state = State.MSG;
			}
			
			key.interestOps(SelectionKey.OP_READ);
			break;
			
		case MSG:
			
			n = sc.read(buffMsg);
			count += n;
			
			if(count == lengthMsg) {
				count = 0;
				
				// process the received data
				byte[] data = new byte[buffMsg.position()];
				buffMsg.rewind();
				buffMsg.get(data, 0, data.length);
				
				state = State.LEN;
				
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				
				l.received(c, data);
			} else {
				key.interestOps(SelectionKey.OP_READ);
			}
			break;
		}
	}
}
