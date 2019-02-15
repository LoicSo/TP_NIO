package ricm.distsys.nio.babystep2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;

public class ReaderServer extends Reader {

	public ReaderServer(SelectionKey key, Writer w) {
		super(key, w);
	}

	@Override
	public void processMsg(byte[] data) throws IOException {
		
		String msg = new String(data, Charset.forName("UTF-8"));
		System.out.println("NioServer received: " + msg);
		
		w.sendMsg(data);
	}

}
