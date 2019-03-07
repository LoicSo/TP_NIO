package ricm.channels.fileserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ricm.channels.IBroker;
import ricm.channels.IBrokerListener;
import ricm.channels.IChannel;
import ricm.channels.IChannelListener;

/*
 * Basic FileServer implementation 
 * 	The file is entirely read in memory before sending it to client
 */

public class FileServer implements IBrokerListener, IChannelListener {

	IBroker engine;
	String folder;
	int port;

	final int CHUNK_SIZE = 512;
	File f;
	FileInputStream fis;
	int next_byte_read;

	void panic(String msg, Exception ex) {
		ex.printStackTrace(System.err);
		System.err.println("PANIC: " + msg);
		System.exit(-1);
	}

	public FileServer(IBroker engine, String folder, int port) throws Exception {
		this.port = port;
		this.engine = engine;
		this.folder = folder;

		next_byte_read = 0;

		if (!folder.endsWith(File.separator))
			this.folder = folder + File.separator;
		this.engine.setListener(this);
		if (!this.engine.accept(port)) {
			System.err.println("Refused accept on " + port);
			System.exit(-1);
		}
	}

	byte[] readFile() {

		int nread;
		int r;
		byte[] bytes;
		bytes = new byte[Math.min((int) f.length() - next_byte_read, CHUNK_SIZE)];
		
		for (nread = 0; nread < bytes.length;) {
			try {
				r = fis.read(bytes, nread, bytes.length - nread);
				nread += r;
			} catch (IOException e) {
				return null;
			}
		}
		
		next_byte_read += nread;
		
		return bytes;
	}

	private int openFile(String filename) {
		f = new File(folder + filename);
		if (!f.exists() || !f.isFile())
			return -2;

		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return -2;
		}

		return (int) f.length();
	}

	/**
	 * Callback invoked when a message has been received. The message is whole, all
	 * bytes have been accumulated.
	 * 
	 * Returns an error code if the request failed: -1: could not parse the request
	 * -2: file does not exist -3: unexpected error
	 * 
	 * @param channel
	 * @param bytes
	 */
	public void received(IChannel channel, byte[] request) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			try {
				InputStream is = new ByteArrayInputStream(request);
				DataInputStream dis = new DataInputStream(is);
				String filename;
				try {
					filename = dis.readUTF();
					System.out.println("FileServer - Receive request for downloading: " + filename);
				} catch (Exception ex) {
					dos.writeInt(-1); // could not parse the downloading request
					byte[] bytes = os.toByteArray();
					channel.send(bytes);	
					return;
				}

				int file_len = openFile(filename);
				
				dos.writeInt(file_len);
				byte[] bLen = os.toByteArray();
				channel.send(bLen);
				
				if(file_len > 0) {
					while (next_byte_read < file_len) {
						byte[] bChunk = readFile();
						channel.send(bChunk);
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace(System.err);
				dos.writeInt(-3);
				byte[] bytes = os.toByteArray();
				channel.send(bytes);	
				
			} finally {
				dos.close();		
			}
		} catch (Exception ex) {
			panic("unexpected exception", ex);
		}
	}

	@Override
	public void connected(IChannel c) {
		System.out.println("Unexpected connected");
		System.exit(-1);
	}

	@Override
	public void accepted(IChannel c) {
		System.out.println("Accepted");
		c.setListener(this);
	}

	@Override
	public void refused(String host, int port) {
		System.out.println("Refused " + host + ":" + port);
		System.exit(-1);
	}

	@Override
	public void closed(IChannel c, Exception e) {
		System.out.println("Client closed channel");
	}
}
