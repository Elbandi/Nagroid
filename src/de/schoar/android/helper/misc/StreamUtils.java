package de.schoar.android.helper.misc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamUtils {

	public static byte[] readstreamFully(InputStream is) {
		return readstreamFully(is, 1024);
	}

	public static byte[] readstreamFully(InputStream is, int blocksize) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[blocksize];
			while (true) {
				int len = is.read(buffer);
				if (len == -1) {
					break;
				}
				baos.write(buffer, 0, len);
			}
			return baos.toByteArray();
		} catch (Exception e) {
		}
		return new byte[0];
	}
}
