package org.springframework.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Utility methods for file and stream copying.
 * @author Juergen Hoeller
 * @since 06.10.2003
 */
public abstract class FileCopyUtils {

	public static final int BLOCK_SIZE = 1024;

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * @param in the stream to copy from
	 * @param out the stream to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(Reader in, Writer out) throws IOException {
		try {
			char[] buffer = new char[BLOCK_SIZE];
			int nrOfBytes = -1;
			while ((nrOfBytes = in.read(buffer)) != -1) {
				out.write(buffer, 0, nrOfBytes);
			}
			out.flush();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ignore) {
			}
			try {
				out.close();
			}
			catch (IOException ignore) {
			}
		}
	}

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * @param in the stream to copy from
	 * @param out the stream to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[BLOCK_SIZE];
			int nrOfBytes = -1;
			while ((nrOfBytes = in.read(buffer)) != -1) {
				out.write(buffer, 0, nrOfBytes);
			}
			out.flush();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ignore) {
			}
			try {
				out.close();
			}
			catch (IOException ignore) {
			}
		}
	}

	/**
	 * Copy the contents of the given input File to the given output File.
	 * @param in the file to copy from
	 * @param out the file to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(File in, File out) throws IOException {
		copy(new BufferedInputStream(new FileInputStream(in)), new BufferedOutputStream(new FileOutputStream(out)));
	}

	/**
	 * Copy the contents of the given byte array to the given output File.
	 * @param in the byte array to copy from
	 * @param out the file to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(byte[] in, File out) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(in);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(out));
		copy(inStream, outStream);
	}

	/**
	 * Copy the contents of the given InputStream into a new byte array.
	 * @param in the stream to copy from
	 * @return the new byte array that has been copied to
	 * @throws IOException in case of I/O errors
	 */
	public static byte[] copyToByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		copy(in, out);
		return out.toByteArray();
	}

	/**
	 * Copy the contents of the given input File into a new byte array.
	 * @param in the file to copy from
	 * @return the new byte array that has been copied to
	 * @throws IOException in case of I/O errors
	 */
	public static byte[] copyToByteArray(File in) throws IOException {
		return copyToByteArray(new BufferedInputStream(new FileInputStream(in)));
	}

}
