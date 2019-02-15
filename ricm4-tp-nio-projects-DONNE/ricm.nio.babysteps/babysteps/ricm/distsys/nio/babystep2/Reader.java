package ricm.distsys.nio.babystep2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Reader {

	private SelectionKey key;

	public Reader(SelectionKey key) {
		this.key = key;
	}

	// Est-ce que handleRead renvoie obligatoirement void ?
	public byte[] handleRead(SelectionKey key) throws IOException {

		// get the socket channel for the client who sent something
		SocketChannel sc = (SocketChannel) key.channel();

		ByteBuffer length = ByteBuffer.allocate(4);
		int count = 0;
		int n;
		while (count < length.capacity()) {
			n = sc.read(length);
			count += n;
		}
		
		length.rewind();
		int len = length.getInt();
		
		ByteBuffer inBuffer = ByteBuffer.allocate(len);
		count = 0;
		while(count < len) {
			n = sc.read(inBuffer);
			count += n;
		}
		
		// process the received data
		byte[] data = new byte[inBuffer.position()];
		inBuffer.rewind();
		inBuffer.get(data, 0, data.length);

		//processMsg(data);
		
		return data;
	}

	// A quoi sert processMsg ???
	public void processMsg(byte[] msg) {
		ByteBuffer buf;
		buf = ByteBuffer.wrap(msg, 0, msg.length);

		key.interestOps(SelectionKey.OP_WRITE);
		key.attach(buf);
	}
}
