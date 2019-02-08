package ricm.channels.echo;

import java.nio.charset.Charset;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;
import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class EchoClient implements IBrokerListener, IChannelListener {
	IBroker broker;
	int nrounds;

	EchoClient(IBroker b) {
		broker = b;
		b.setListener(this);
		b.connect("server", 8080);
	}

	@Override
	public void connected(IChannel c) {
		c.setListener(this);
		byte[] bytes = "Hello".getBytes(Charset.forName("UTF-8"));
		c.send(bytes);
	}

	@Override
	public void refused(String host, int port) {
	}

	@Override
	public void accepted(IChannel c) {
	}

	@Override
	public void received(IChannel c, byte[] bytes) {
		String s = new String(bytes, Charset.forName("UTF-8"));
		System.out.println("Client(round "+nrounds+"): " + s);
		if (++nrounds < 10)
			c.send(bytes);
		else {
			System.out.println("Client closing its channel");
			c.close();
		}
	}

	@Override
	public void closed(IChannel c, Exception e) {
		System.out.println("Client: channel closed");
	}

}
