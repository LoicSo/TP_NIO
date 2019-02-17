package ricm.distsys.nio.babystep3;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;

public abstract class Reader {

	private final int NBBYTELEN = 4;
	
	protected SelectionKey key;
	
	private enum State {LEN,MSG};
	private State state;
	
	private int lengthMsg;	// Longueur du message à lire
	private int count;		// Nombre d'octet lu
	
	ByteBuffer buffLength;	// Buffer pour la longueur du message
	ByteBuffer buffMsg;		// Buffer pour le message
	
	Writer w;
	
	byte[] digest;			// Tableau pour le checksum
	
	public Reader(SelectionKey key, Writer w) {
		this.key = key;
		state = State.LEN;
		count = 0;
		buffLength = ByteBuffer.allocate(NBBYTELEN);
		this.w = w;
	}

	public void handleRead(SelectionKey key) throws IOException {

		// get the socket channel for the client who sent something
		SocketChannel sc = (SocketChannel) key.channel();
		
		int n;
		switch(state) {
		case LEN:
			
			n = sc.read(buffLength);
			count += n;
			
			if(count == NBBYTELEN) {
				count = 0;
				
				buffLength.rewind();
				lengthMsg = buffLength.getInt();
				buffMsg = ByteBuffer.allocate(lengthMsg);
				buffLength.rewind();
				
				state = State.MSG;
			}
			
			key.interestOps(SelectionKey.OP_READ);
			break;
			
		case MSG:
			
			n = sc.read(buffMsg);
			count += n;
			
			if(count == lengthMsg) {
				count = 0;
				
				// process the received data
				byte[] data = new byte[buffMsg.position()];
				buffMsg.rewind();
				buffMsg.get(data, 0, data.length);
				
				state = State.LEN;
				
				processMsg(data);
			} else {
				key.interestOps(SelectionKey.OP_READ);
			}
			break;
		}
	}

	public abstract void processMsg(byte[] msg) throws IOException;
	
	/*
	 * Wikipedia: The MD5 message-digest algorithm is a widely used hash function
	 * producing a 128-bit hash value. Although MD5 was initially designed to be
	 * used as a cryptographic hash function, it has been found to suffer from
	 * extensive vulnerabilities. It can still be used as a checksum to verify data
	 * integrity, but only against unintentional corruption. It remains suitable for
	 * other non-cryptographic purposes, for example for determining the partition
	 * for a particular key in a partitioned database.
	 */
	public static byte[] md5(byte[] bytes) throws IOException {
		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(bytes, 0, bytes.length);
			digest = md.digest();
		} catch (Exception ex) {
			throw new IOException(ex);
		}
		return digest;
	}

	public static boolean md5check(byte[] d1, byte[] d2) {
		if (d1.length != d2.length)
			return false;
		for (int i = 0; i < d1.length; i++)
			if (d1[i] != d2[i])
				return false;
		return true;
	}

	public static void echo(PrintStream ps, byte[] digest) {
		for (int i = 0; i < digest.length; i++)
			ps.print(digest[i] + ", ");
		ps.println();
	}

	public void setDigest(byte[] digest) {
		this.digest = digest;		
	}

}
