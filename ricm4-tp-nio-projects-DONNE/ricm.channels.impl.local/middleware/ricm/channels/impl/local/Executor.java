package ricm.channels.impl.local;

import java.util.Iterator;
import java.util.LinkedList;

public class Executor {

	static class Event {
		static final int CONNECT = 1;
		static final int ACCEPT = 2;
		static final int READ = 3;
		static final int WRITE = 4;
		Channel c;
		int kind;
		byte bytes[];
		
		Event(Channel c, int kind) {
			this.c = c;
			this.kind = kind;
		}
		Event(Channel c, int kind, byte[] bytes) {
			this.c = c;
			this.kind = kind;
			this.bytes = bytes;
		}
	}
	
	static class Request {
		Broker accept;
		Broker connect;
		String host;
		int port;
		Request(Broker b, int port) {
			this.accept = b;
			this.host = b.host;
			this.port = port;
		}
		Request(Broker b, String host, int port) {
			this.connect = b;
			this.host = host;
			this.port = port;
		}
	}
	
	static class AcceptRequest {
		Broker broker;
		int port;
	}
	
	LinkedList<Event> events;
	LinkedList<Request> reqs;
	
	public Executor() {
		events = new LinkedList<Event>();
		reqs = new LinkedList<Request>();
	}
	
	Request find(String host, int port) {
		Iterator<Request> iter = reqs.iterator();
		while (iter.hasNext()) {
			Request r = iter.next();
			if (r.port==port && r.host.equals(host))
				return r;
		}
		return null;
	}

	void registerAccept(Broker b, int port) {
		Request r = find(b.host,port);
		if (r==null) {
			r = new Request(b,port);
			reqs.add(r);
		} else {
			if (r.accept!=null)
				throw new IllegalStateException("accept doublon");
			r.accept = b;
			if (r.connect!=null) {
				rendezVous(r);
				reqs.remove(r);
			}
		}
	}
	void registerConnect(Broker b, String host, int port) {
		Request r = find(host,port);
		if (r==null) {
			r = new Request(b,host, port);
			reqs.add(r);
		} else {
			if (r.connect!=null)
				throw new IllegalStateException("connect doublon");
			r.connect= b;
			if (r.accept!=null) {
				rendezVous(r);
				reqs.remove(r);
			}
		}
	}

	void rendezVous(Request r) {
		Channel client,server;
		Event e;
		client = new Channel(r.connect);
		server = new Channel(r.accept);
		client.channel = server;
		server.channel = client;
		e = new Event(server, Event.ACCEPT);
		events.add(e);
		e = new Event(client, Event.CONNECT);
		events.add(e);
	}

	void send(Channel c, byte[] bytes, int offset, int count) {
		byte[] tmp =new byte[count];
		System.arraycopy(bytes, offset, tmp, 0, count);
		Event e = new Event(c, Event.READ, tmp);
		events.add(e);
	}
	

	public void loop() {
		while (events.size()!=0) {
			Event e = events.removeFirst();
			switch(e.kind) {
			case Event.ACCEPT:
				e.c.broker.l.accepted(e.c);
				break;
			case Event.CONNECT:
				e.c.broker.l.connected(e.c);
				break;
			case Event.READ:
				e.c.listener.received(e.c, e.bytes);
				break;
			}
		}
	}
	
}
