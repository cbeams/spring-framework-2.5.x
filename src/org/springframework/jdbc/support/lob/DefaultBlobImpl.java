package org.springframework.jdbc.support.lob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * Spring's default implementation of the java.sql.Blob interface.
 * Should be accepted by any database that doesn't expect its own
 * specific Blob implementation.
 *
 * <p>Based on net.sf.hibernate.lob.BlobImpl from Hibernate 2.0.3.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see DefaultBlobCreator
 */
public class DefaultBlobImpl implements Blob {

	private final InputStream contentStream;

	private final int contentLength;

	/**
	 * Create a Blob for the given content.
	 * @param content the content as byte array
	 */
	public DefaultBlobImpl(byte[] content) {
		this.contentStream = new ByteArrayInputStream(content);
		this.contentLength = content.length;
	}

	/**
	 * Create a Blob for the given content.
	 * @param contentStream the content as InputStream
	 * @throws IOException if the stream could not be examined
	 */
	public DefaultBlobImpl(InputStream contentStream) throws IOException {
		this.contentStream = contentStream;
		this.contentLength = contentStream.available();
	}

	/**
	 * @see java.sql.Blob#getBinaryStream()
	 */
	public InputStream getBinaryStream() {
		return contentStream;
	}

	/**
	 * @see java.sql.Blob#length()
	 */
	public long length() {
		return contentLength;
	}

	/**
	 * @see java.sql.Blob#getBytes(long, int)
	 */
	public byte[] getBytes(long arg0, int arg1) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#setBytes(long, byte[])
	 */
	public int setBytes(long arg0, byte[] arg1) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#setBytes(long, byte[], int, int)
	 */
	public int setBytes(long arg0, byte[] arg1, int arg2, int arg3)
	throws SQLException {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#setBinaryStream(long)
	 */
	public OutputStream setBinaryStream(long arg0) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#truncate(long)
	 */
	public void truncate(long arg0) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#position(byte[], long)
	 */
	public long position(byte[] arg0, long arg1) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

	/**
	 * @see java.sql.Blob#position(Blob, long)
	 */
	public long position(Blob arg0, long arg1) {
		throw new UnsupportedOperationException("Blob may not be manipulated other than via constructors");
	}

}
