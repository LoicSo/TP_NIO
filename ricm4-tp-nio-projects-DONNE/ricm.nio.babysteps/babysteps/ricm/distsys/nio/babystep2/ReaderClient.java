package ricm.distsys.nio.babystep2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;

public class ReaderClient extends Reader {

	int nloops = 0;

	public ReaderClient(SelectionKey key, Writer w) {
		super(key, w);
	}

	@Override
	public void processMsg(byte[] data) throws IOException {

		// Let's print the message we received, assuming it is a string
		// in UTF-8 encoding, since it is the format of our first message
		// we sent to the server.
		String msg = new String(data, Charset.forName("UTF-8"));
		System.out.println("NioClient received msg[" + nloops + "]: " + msg);

		// Let's make sure we read the message we sent to the server
		byte[] md5 = md5(data);
		if (!md5check(digest, md5))
			System.out.println("Checksum Error!");

		nloops++;
		if (nloops < 100) {
			// send back the received message
			w.sendMsg(data);
		}
	}

}
