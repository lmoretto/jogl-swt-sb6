package jogl.swt.utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IOUtils {
	public static void readFully(InputStream in, ByteBuffer out) throws IOException {
		if (!out.hasRemaining()) {
			return;
		}
		
		int oldpos = out.position();
		try {
			if (out.hasArray()) {
				while (out.hasRemaining()) {
					int r = in.read(out.array(), out.arrayOffset()+out.position(), out.remaining());
					if (r < 0) throw new EOFException();
					out.position(out.position() + r);
				}
			} else {
				byte[] temp = new byte[Math.min(out.remaining(), 8192)];
				while (out.hasRemaining()) {
					int r = in.read(temp, 0, Math.min(temp.length, out.remaining()));
					if (r < 0) throw new EOFException();
					out.put(temp, 0, r);
				}
			}
		} finally {
			out.position(oldpos);
		}
	}
	
	public static byte[] readBytes(InputStream is) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		byte[] tmp = new byte[100];
		
		int read = 0;
		try {
			while ((read = is.read(tmp)) >= 0) {
				out.write(tmp, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return out.toByteArray();
	}
	

	private IOUtils() {
	}
}
