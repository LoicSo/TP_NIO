package ricm.channels.impl.local;

import java.util.LinkedList;
import java.util.List;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;

public class Broker implements IBroker {

	String host;
	IBrokerListener l;
	List<Channel> channels;
	Executor executor;

	public Broker(Executor e, String host) {
		this.executor = e;
		this.host = host;
		channels = new LinkedList<Channel>();
	}

	@Override
	public void setListener(IBrokerListener l) {
		this.l = l;
	}

	@Override
	public boolean connect(String host, int port) {
		executor.registerConnect(this, host, port);
		return true;
	}

	@Override
	public boolean accept(int port) {
		executor.registerAccept(this, port);
		return true;
	}

}
