package ricm.channels.echo;

import ricm.channels.nio.*;

public class NIOMain {

	static Broker sb,cb;
	static EchoServer s,c;
	
	
	/*
	 * Initialize our message-oriented middleware,
	 * using the local implementation.
	 */
	private static void initMiddleware() {
		sb = new Broker("localHost");
		cb = new Broker("client");
	}


	public static void main(String[] args) {

		initMiddleware();
		
		/*
		 * Create the client and server applications:
		 */
		EchoServer s = new EchoServer(sb);
		EchoClient c = new EchoClient(cb);
		
		Thread ts = new Thread(sb);
		Thread tc = new Thread(cb);
		ts.start();
		tc.start();
		
		System.out.println("Bye.");
	}

}
