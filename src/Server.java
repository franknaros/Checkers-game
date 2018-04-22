import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

class Server extends Thread {

	public static void server() throws Exception {
		Scanner input = new Scanner(System.in);

		System.out.print("SETT MOTSPILLERS IP : ");
		String o = input.next();

		Checkers.IP = o;
		Checkers.adresse = InetAddress.getByName(Checkers.IP);
		System.out.println(Checkers.adresse);

		System.out.print("SETT DIN PORT : ");
		String IN = input.next();
		Checkers.PORT = Integer.parseInt(IN);

		System.out.print("SETT MOTSPILLERS PORT : ");
		IN = input.next();
		Checkers.PORT_C = Integer.parseInt(IN);
		System.out.println("MOTSPILLERS PORT SETT : " + Checkers.PORT_C);

		Checkers.serverSocket = new DatagramSocket(Checkers.PORT);
		byte[] mottattData = new byte[1024];
		String gammel = null;

		while (true) {

			DatagramPacket receivePacket = new DatagramPacket(mottattData, mottattData.length);
			Checkers.serverSocket.receive(receivePacket);
			String setning = new String(receivePacket.getData());

			Serializer F = Serializer.hentData(receivePacket.getData());

			if (setning != gammel) {
				int x1, x2;
				try {
					x1 = F.kol;
					x2 = F.rad;

					Checkers_EntryPoint.chk.play(x2, x1);

				} catch (Exception e) {
					System.out.println(e);
				}

				gammel = setning;
			}
		}
	}

	public void run() {
		try {
			System.out.println("VENTER PÅ MOTSPILLER...");
			server();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

}
