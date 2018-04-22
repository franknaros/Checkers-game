import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.net.*;
import java.nio.ByteBuffer;
import java.io.IOException;

public class Checkers_EntryPoint {
	static DatagramSocket clientSocket;
	static Server s;
	static BufferedImage crownImage = null;
	static Checkers chk;
	static int PORT;
	static int PORT_C;
	static String IP;
	static Serializer Serial;
	static DatagramPacket sendPacket;
	static ByteBuffer buf;
	static String NAME;
	static DatagramSocket serverSocket;
	static InetAddress adresse;

	public static void main(String[] args) throws IOException {
		try {
			System.out.println("STARTER SERVEREN...");
			s = new Server();
			clientSocket = new DatagramSocket();
			s.start();

			crownImage = ImageIO.read(new File("crown.png"));
		} catch (Exception e) {

			e.printStackTrace();
		}
		chk = new Checkers();

	}
}
