package ricm.channels.fileserver;

import ricm.channels.impl.local.*;

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

	static int port = 80;
	static Executor e;
	static Broker sb, cb;
	static FileServer s;
	static FileDownloader c;
	
	/*
	 * Initialize our message-oriented middleware,
	 * using the local implementation.
	 */
	private static void initMiddleware() {
		e = new Executor();
		sb = new Broker(e, "server");
		cb = new Broker(e, "client");
	}

	/*
	 * Create the file server application:
	 */

	private static void initFileServer() throws Exception {
		String folder = "applications";
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
		c.download("server", port, filename, true);

	}

	public static void main(String[] args) throws Exception {

		initMiddleware();

		initFileServer();
		
		initFileDownloader();

		/*
		 * Capture the main thread, entering 
		 * the event loop of the executor.
		 */
		e.loop();
		
		System.out.println("Bye.");
	}

}
