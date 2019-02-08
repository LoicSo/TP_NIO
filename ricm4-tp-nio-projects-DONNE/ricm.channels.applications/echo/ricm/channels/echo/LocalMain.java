package ricm.channels.echo;

import ricm.channels.impl.local.*;

public class LocalMain {

	static Executor e;
	static Broker sb,cb;
	static EchoServer s,c;
	
	
	/*
	 * Initialize our message-oriented middleware,
	 * using the local implementation.
	 */
	private static void initMiddleware() {
		e = new Executor();
		sb = new Broker(e, "server");
		cb = new Broker(e, "client");
	}


	public static void main(String[] args) {

		initMiddleware();
		
		/*
		 * Create the client and server applications:
		 */
		EchoServer s = new EchoServer(sb);
		EchoClient c = new EchoClient(cb);
		
		/*
		 * Capture the main thread, entering 
		 * the event loop of the executor.
		 */
		e.loop();
		
		System.out.println("Bye.");
	}

}
