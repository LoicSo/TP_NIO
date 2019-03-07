package ricm.channels.fileserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;
import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

public class FileDownloader implements IBrokerListener, IChannelListener {

	final int CHUNK_SIZE = 512;

	enum State {
		LEN, MSG
	};

	private State state;
	int count = 0;
	int nbytes;
	String txt = "";

	String server;
	int port;
	String filename;
	IBroker engine;
	boolean isText;

	public FileDownloader(IBroker engine) {
		this.engine = engine;
		this.engine.setListener(this);
		state = State.LEN;
	}

	public void download(String hostname, int port, String filename, boolean isText) throws Exception {
		this.filename = filename;
		this.isText = isText;
		if (!this.engine.connect(hostname, port)) {
			System.err.println("Refused connect on " + port);
			System.exit(-1);
		}
	}

	@Override
	public void connected(IChannel c) {
		System.out.println("Connected");
		System.out.println("  downloading " + filename);
		c.setListener(this);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		try {
			dos.writeUTF(filename);
			dos.close();
			byte[] bytes = os.toByteArray();
			c.send(bytes);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	@Override
	public void accepted(IChannel c) {
		System.out.println("Unexpected accepted connection");
		System.exit(-1);
	}

	@Override
	public void refused(String host, int port) {
		System.out.println("Refused " + host + ":" + port);
		System.exit(-1);
	}

	@Override
	public void received(IChannel c, byte[] reply) {
		System.out.println("===============================================================");
		System.out.println("Received: " + reply.length + " bytes");
		System.out.println("===============================================================");

		InputStream is = new ByteArrayInputStream(reply);
		DataInputStream dis = new DataInputStream(is);

		switch (state) {
		case LEN:
			try {
				nbytes = dis.readInt();
				if (nbytes <= 0) {
					System.out.println("Server returns an error code: " + nbytes);
					System.exit(-1);
				} else {
					System.out.println("Download " + nbytes + " bytes");
					state = State.MSG;
				}
			} catch (Exception e) {
				System.out.println(" failed parsing the received message");
				e.printStackTrace();
			}
			break;

		case MSG:

			try {
				count += reply.length;

				if (isText) {
					txt += new String(reply, "UTF-8");
				}

				if (count == nbytes) {
					System.out.println(txt);
					state = State.LEN;

					System.out.println("===============================================================");
					System.out.println("\n\nBye");
					c.close();
					System.exit(0);
				}

			} catch (IOException e) {
				System.out.println(" failed parsing the received message");
				e.printStackTrace();
			}
			break;
		}

	}

	@Override
	public void closed(IChannel c, Exception e) {
		System.out.println("Unexpected closed channel");
		if (e != null)
			e.printStackTrace();
		System.exit(-1);
	}

}
