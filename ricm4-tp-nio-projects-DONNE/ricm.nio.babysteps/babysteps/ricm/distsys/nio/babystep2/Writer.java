package ricm.distsys.nio.babystep2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Writer {

	SelectionKey key;

	public Writer(SelectionKey key) {
		this.key = key;
	}

	// Que mettre dans sendMsg ???
	public void sendMsg(byte[] msg) {

	}

	public void handleWrite(SelectionKey key) throws IOException {
		// get the socket channel for the client to whom we
		// need to send something
		SocketChannel sc = (SocketChannel) key.channel();

		// get back the buffer that we must send
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		
		int length = buffer.capacity();
		ByteBuffer bLen = ByteBuffer.allocate(4);
		bLen.putInt(length);
		bLen.rewind();
		
		int count = 0;
		int n;
		
		// Write écrit 0 octets !!!
		while(count < bLen.capacity()) {
			n = sc.write(bLen);
			count += n;
		}
		
		count = 0;
		while(count < length) {
			n = sc.write(buffer);
			count += n;
		}
		
		key.interestOps(SelectionKey.OP_READ);
	}
}
