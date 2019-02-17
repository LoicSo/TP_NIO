package ricm.distsys.nio.babystep3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Writer {

	private final int NBBYTELEN = 4;
	
	SelectionKey key;
	
	private enum State {LEN, MSG};
	private State state;
	
	private int lengthMsg;	// Longueur du message à écrire
	private int count;		// Nombre d'octet écrit
	
	ByteBuffer buffLength;	// Buffer pour la longueur du message
	ByteBuffer buffMsg;		// Buffer pour le message

	public Writer(SelectionKey key) {
		this.key = key;
		state = State.LEN;
		count = 0;
		buffLength = ByteBuffer.allocate(NBBYTELEN);
	}

	public void sendMsg(byte[] msg) {
		
		buffMsg = ByteBuffer.wrap(msg, 0, msg.length);
		
		lengthMsg = buffMsg.capacity();
		buffLength.rewind();
		buffLength.putInt(lengthMsg);
		buffLength.rewind();
		
		key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}

	public void handleWrite(SelectionKey key) throws IOException {
		// get the socket channel for the client to whom we
		// need to send something
		SocketChannel sc = (SocketChannel) key.channel();

		int n;
		switch(state) {
		case LEN:
			n = sc.write(buffLength);
			count += n;
			
			if(count == NBBYTELEN) {
				count = 0;
				
				state = State.MSG;
			}
			key.interestOps(SelectionKey.OP_WRITE);
			break;
			
		case MSG:
			n = sc.write(buffMsg);
			count += n;
			
			if(count == lengthMsg) {
				count = 0;
				
				state = State.LEN;
				key.interestOps(SelectionKey.OP_READ);
			} else {
				key.interestOps(SelectionKey.OP_WRITE);
			}
			break;
		}
	}
}
