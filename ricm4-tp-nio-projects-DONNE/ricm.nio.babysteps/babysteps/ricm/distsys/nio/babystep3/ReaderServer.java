package ricm.distsys.nio.babystep3;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;

public class ReaderServer extends Reader {

	int numClient;
	
	public ReaderServer(SelectionKey key, Writer w, int numClient) {
		super(key, w);
		this.numClient = numClient;
	}

	@Override
	public void processMsg(byte[] data) throws IOException {
		
		String msg = new String(data, Charset.forName("UTF-8"));
		System.out.println("NioServer received from " + numClient + ": " + msg);
		
		w.sendMsg(data);
	}

}
