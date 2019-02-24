package ricm.channels.fileserver;

import ricm.channels.impl.*;

/*
 * This is part of the samples to show how to use
 * our message-oriented middleware.
 * 
 * This is a local setup of two applications:
 * a file server and a file downloader.
 * 
 * Note that this is a local setup, within a
 * single Java Runtime Environment, using a single
 * middleware.
 */
public class LocalMain {

	static int port = 8080;
	static Broker sb, cb;
	static FileServer s;
	static FileDownloader c;
	
	/*
	 * Initialize our message-oriented middleware,
	 * using the local implementation.
	 */
	private static void initMiddleware() {
		sb = new Broker("localHost");
		cb = new Broker("client");
	}

	/*
	 * Create the file server application:
	 */

	private static void initFileServer() throws Exception {
		String folder = "echo";
		s = new FileServer(sb, folder, port);
	}

	/*
	 * Create the file download application
	 * and request the download of a file
	 */
	private static void initFileDownloader() throws Exception {
		
		c = new FileDownloader(cb);

		// Download a file
		String filename = "ricm/channels/echo/EchoClient.java";
		c.download("localHost", port, filename, true);

	}

	public static void main(String[] args) throws Exception {

		initMiddleware();

		initFileServer();
		
		initFileDownloader();
		
		Thread ts = new Thread(sb);
		Thread tc = new Thread(cb);
		ts.start();
		tc.start();
		
		System.out.println("Bye.");
	}

}
