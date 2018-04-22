import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class Serializer implements Serializable {
	private static final long serialVersionUID = 1L;
	int kol, rad;

	Serializer(int kol, int rad) throws IOException {
		this.kol = kol;
		this.rad = rad;
	}

	public static byte[] SetData(int rad, int kol) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		try {
			os = new ObjectOutputStream(outputStream);
			os.writeObject(new Serializer(kol, rad));
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] data = outputStream.toByteArray();
		return data;

	}

	public static Serializer hentData(byte[] b) {
		Serializer RES = null;

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			ObjectInputStream is = new ObjectInputStream(in);
			RES = (Serializer) is.readObject();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return RES;
	}
}
