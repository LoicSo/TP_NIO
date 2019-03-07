package ricm.channels.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class Writer {

	private final int NBBYTELEN = 4;

	SelectionKey key;

	private enum State {
		LEN, MSG
	};

	private State state;

	private int lengthMsg; // Longueur du message à écrire
	private int count; // Nombre d'octet écrit

	ByteBuffer buffLength; // Buffer pour la longueur du message
	ByteBuffer buffMsg; // Buffer pour le message

	LinkedList<ByteBuffer> listMsg; // Liste des messages à envoyer

	public Writer(SelectionKey key) {
		this.key = key;
		state = State.LEN;
		count = 0;
		buffLength = ByteBuffer.allocate(NBBYTELEN);
		listMsg = new LinkedList<ByteBuffer>();
	}

	public void sendMsg(byte[] msg) {

		ByteBuffer message = ByteBuffer.wrap(msg, 0, msg.length);

		if (listMsg.isEmpty()) {
			buffMsg = message;

			lengthMsg = buffMsg.capacity();
			buffLength.rewind();
			buffLength.putInt(lengthMsg);
			buffLength.rewind();
		}
		listMsg.add(message);

		key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}

	public void sendMsg(byte[] msg, int offset, int count) {

		ByteBuffer message = ByteBuffer.wrap(msg, offset, count);

		if (listMsg.isEmpty()) {
			buffMsg = message;

			lengthMsg = buffMsg.capacity();
			buffLength.rewind();
			buffLength.putInt(lengthMsg);
			buffLength.rewind();
		}
		listMsg.add(message);

		key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}

	public void handleWrite(SelectionKey key) throws IOException {
		// get the socket channel for the client to whom we
		// need to send something

		if (lengthMsg > 0) {

			SocketChannel sc = (SocketChannel) key.channel();

			int n;
			switch (state) {
			case LEN:
				n = sc.write(buffLength);
				count += n;

				if (count == NBBYTELEN) {
					count = 0;

					state = State.MSG;
				}
				key.interestOps(SelectionKey.OP_WRITE);
				break;

			case MSG:
				n = sc.write(buffMsg);
				count += n;

				if (count == lengthMsg) {
					count = 0;
					lengthMsg = 0;

					state = State.LEN;

					listMsg.remove();
					if (!listMsg.isEmpty()) {
						buffMsg = listMsg.get(0);

						lengthMsg = buffMsg.capacity();
						buffLength.rewind();
						buffLength.putInt(lengthMsg);
						buffLength.rewind();

						key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					} else {
						key.interestOps(SelectionKey.OP_READ);
					}
				} else {
					key.interestOps(SelectionKey.OP_WRITE);
				}
				break;
			}
		}
	}
}
