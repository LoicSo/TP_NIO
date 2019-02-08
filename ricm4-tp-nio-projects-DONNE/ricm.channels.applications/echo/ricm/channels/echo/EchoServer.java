package ricm.channels.echo;

import java.nio.charset.Charset;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;
import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class EchoServer  implements IBrokerListener, IChannelListener {
	IBroker broker;

	EchoServer(IBroker b) {
		broker = b;
		b.setListener(this);
		b.accept(8080);
	}

	@Override
	public void connected(IChannel c) {
	}

	@Override
	public void refused(String host, int port) {
	}

	@Override
	public void accepted(IChannel c) {
		c.setListener(this);
	}

	@Override
	public void received(IChannel c, byte[] bytes) {
		String s = new String(bytes, Charset.forName("UTF-8"));
		System.out.println("Server received: " + s);
		c.send(bytes);
	}

	@Override
	public void closed(IChannel c, Exception e) {
		System.out.println("Server: channel closed");
	}


}
