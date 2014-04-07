package Server;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.lwjgl.input.Keyboard;

import Packet.*;

public class Client {

	public Client() {
	}

	public Client(SelectionKey sok, Game gam) {
		game = gam;
		socket = sok;
		player = new Player();
		player.game = gam;
		player.pos.x = 100.0f;
		player.pos.y = 100.0f;
		game.addEntity(player);
	}

	public int id;

	public Queue<ByteBuffer> messages = new ConcurrentLinkedQueue<ByteBuffer>();
	public Game game;
	public Player player;
	public SelectionKey socket;
	public boolean[] keys = new boolean[256];

	public void onKeyPressed(int key) {
		keys[key] = true;

		if (!player.immobilized) {
			if (key == Keyboard.KEY_RIGHT) {
				player.setSpeed(0.0f, player.speed.y, player.speed.z);
				player.dir = 1.0f;

			}

			if (key == Keyboard.KEY_LEFT) {
				player.setSpeed(0.0f, player.speed.y, player.speed.z);
				player.dir = -1.0f;
			}

			if (key == Keyboard.KEY_UP) {
				player.setSpeed(player.speed.x, 0.0f, player.speed.z);

			}

			if (key == Keyboard.KEY_DOWN) {
				player.setSpeed(player.speed.x, 0.0f, player.speed.z);

			}

			if (key == Keyboard.KEY_C) {
				player.setSpeed(3.0f, 0.0f, 3.0f);
				player.immobilize();

			}

			if (key == Keyboard.KEY_X) {
				if (player.canAnim()) {
					if (player.pos.z > 0) {
						if (game.ball.owner == player)
							game.ball.possess(null);

						player.setAnim(4);

					}
				}
			}

			if (key == Keyboard.KEY_Z) {
				if (player.canAnim()) {
					player.direction.x = player.dir;
					player.direction.y = 0.0f;
					player.direction.z = 0.0f;
					
					player.direction.add(player.speed);
					player.setAnim(2);
				}
			}
			if (key == Keyboard.KEY_S) {
				if (player.canAnim()) {
					player.direction.x = player.dir;
					player.direction.y = 0.0f;
					player.direction.z = 0.0f;
					
					player.direction.add(player.speed);
					player.setAnim(3);
				}
			}
		}
	}

	public void onKeyReleased(int key) {
		keys[key] = false;

		if (key == Keyboard.KEY_RIGHT && !keys[Keyboard.KEY_LEFT]) {
			player.setSpeed(0.0f, player.speed.y, player.speed.z);

		}

		if (key == Keyboard.KEY_LEFT && !keys[Keyboard.KEY_RIGHT]) {
			player.setSpeed(0.0f, player.speed.y, player.speed.z);

		}

		if (key == Keyboard.KEY_UP && !keys[Keyboard.KEY_DOWN]) {
			player.setSpeed(player.speed.x, 0.0f, player.speed.z);

		}

		if (key == Keyboard.KEY_DOWN && !keys[Keyboard.KEY_UP]) {
			player.setSpeed(player.speed.x, 0.0f, player.speed.z);
		}

		if (key == Keyboard.KEY_SPACE)
			player.setSpeed(player.speed.x, player.speed.y, 2.5f);

	}

	public void onKey() {

		if (keys[Keyboard.KEY_LEFT]) {
			player.setSpeed(player.speed.x - 0.09f, player.speed.y,
					player.speed.z);
		}

		if (keys[Keyboard.KEY_RIGHT]) {

			player.setSpeed(player.speed.x + 0.09f, player.speed.y,
					player.speed.z);
		}

		if (keys[Keyboard.KEY_UP]) {

			player.setSpeed(player.speed.x, player.speed.y - 0.09f,
					player.speed.z);
		}

		if (keys[Keyboard.KEY_DOWN]) {

			player.setSpeed(player.speed.x, player.speed.y + 0.09f,
					player.speed.z);
		}
	}

	public void destroy() {
		player.dead = true;
	}

	public void update() {
		processMessages();
		onKey();
	}

	public void processMessages() {
		while (!messages.isEmpty()) {
			ByteBuffer buf = messages.poll();
			if (buf != null) {
				byte[] bytes = buf.array();
				Packets pk = Packets.fromByteArray(bytes);
				switch (pk.getType()) {
				case 4: {
					KeyPressPacket p = (KeyPressPacket) pk;
					onKeyPressed(p.key);
				}
					break;

				case 5: {
					KeyReleasePacket p = (KeyReleasePacket) pk;
					onKeyReleased(p.key);
				}
					break;

				default:
					break;
				}
			}
		}
	}
}
