package org.springframework.jdbc.support.lob;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of the LobHandler interface. Its BlobHandler implementation
 * creates instances of DefaultBlobImpl in the createBlob methods, and invokes
 * PreparedStatement.setBytes/setBinaryStream in setBlobAsBytes/setBlobAsBinaryStream.
 *
 * <p>With some JDBC drivers, PreparedStatement's setBytes/setBinaryStream is
 * implemented more efficiently than setBlob. Therefore, this LobHandler
 * only creates java.sql.Blob instances when necessary, i.e. on createBlob invocations.
 *
 * <p>Note that PreparedStatement's setBlob implementation is broken with MySQL
 * Connector/J 3.0.9: Nevertheless, DefaultBlobCreator's setBlobAsBinaryStream
 * will work as it calls setBinaryStream instead of setBlob, as outlined above.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see DefaultBlobImpl
 * @see java.sql.PreparedStatement#setBytes
 * @see java.sql.PreparedStatement#setBinaryStream
 */
public class DefaultLobHandler implements LobHandler {

	public BlobCreator getBlobCreator() {
		return new DefaultBlobCreator();
	}


	protected static class DefaultBlobCreator implements BlobCreator {

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

		public void setBlobAsBytes(PreparedStatement ps, int parameterIndex, byte[] content)
				throws SQLException {
			ps.setBytes(parameterIndex, content);
			logger.debug("Set bytes for BLOB with length " + content.length);
		}

		public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream)
				throws SQLException, IOException{
			int contentLength = contentStream.available();
			ps.setBinaryStream(parameterIndex, contentStream, contentLength);
			logger.debug("Set binary stream for BLOB with length " + contentLength);
		}

		public void close() {
			// nothing to do here
		}
	}

}
