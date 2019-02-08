package ricm.channels.impl.local;

import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class Channel implements IChannel {

	Broker broker;
	Channel channel;
	IChannelListener listener;
	boolean closed;

	Channel(Broker b) {
		broker = b;
	}

	Channel(Broker b, Channel c) {
		broker = b;
		channel = c;
		c.channel = this;
	}

	@Override
	public void setListener(IChannelListener l) {
		listener = l;
	}

	@Override
	public void send(byte[] bytes, int offset, int count) {
		broker.executor.send(channel, bytes, offset, count);
	}

	@Override
	public void send(byte[] bytes) {
		broker.executor.send(channel, bytes, 0, bytes.length);
	}

	@Override
	public void close() {
		if (!closed) {
			closed = true;
			if (!channel.closed()) {
				channel.closed = true;
				channel.listener.closed(channel, null);
			}
		}
	}

	@Override
	public boolean closed() {
		return closed;
	}

}
