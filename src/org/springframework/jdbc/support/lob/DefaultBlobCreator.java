package org.springframework.jdbc.support.lob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of the BlobCreator interface.
 * Creates instances of DefaultBlobImpl in createBlob, and invokes
 * PreparedStatement.setBinaryStream in setBlobAsBinaryStream.
 *
 * <p>With some JDBC drivers, setBinaryStream is implemented more efficiently than
 * setBlob. Therefore, DefaultBlobCreator uses PreparedStatement.setBinaryStream
 * in its setBlobAsBinaryStream implementation, only creating java.sql.Blob
 * instances when necessary, i.e. on createBlob invocations.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see DefaultBlobImpl
 * @see java.sql.PreparedStatement#setBinaryStream
 */
public class DefaultBlobCreator implements BlobCreator {

	protected final Log logger = LogFactory.getLog(getClass());

	public Blob createBlob(Connection con, byte[] content) {
		DefaultBlobImpl blob = new DefaultBlobImpl(content);
		logger.debug("Created new DefaultBlobImpl with length " + blob.length());
		return blob;
	}

	public Blob createBlob(Connection con, InputStream contentStream) throws IOException {
		DefaultBlobImpl blob = new DefaultBlobImpl(contentStream);
		logger.debug("Created new DefaultBlobImpl with length " + blob.length());
		return blob;
	}

	public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, byte[] content)
	    throws SQLException {
		ps.setBinaryStream(parameterIndex, new ByteArrayInputStream(content), content.length);
		logger.debug("Set binary stream for BLOB with length " + content.length);
	}

	public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream)
	    throws SQLException, IOException{
		int contentLength = contentStream.available();
		ps.setBinaryStream(parameterIndex, contentStream, contentLength);
		logger.debug("Set binary stream for BLOB with length " + contentLength);
	}

}
