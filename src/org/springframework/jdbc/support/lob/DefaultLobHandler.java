package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of the LobHandler interface. Invokes the direct accessor
 * methods that java.sql.ResultSet and java.sql.PreparedStatement offer.
 *
 * <p>This LobHandler should work for any JDBC driver that is JDBC compliant
 * in terms of the spec's suggestions regarding simple BLOB and CLOB handling.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see java.sql.ResultSet#getBytes
 * @see java.sql.ResultSet#getBinaryStream
 * @see java.sql.ResultSet#getString
 * @see java.sql.ResultSet#getAsciiStream
 * @see java.sql.ResultSet#getCharacterStream
 * @see java.sql.PreparedStatement#setBytes
 * @see java.sql.PreparedStatement#setBinaryStream
 * @see java.sql.PreparedStatement#setString
 * @see java.sql.PreparedStatement#setAsciiStream
 * @see java.sql.PreparedStatement#setCharacterStream
 */
public class DefaultLobHandler implements LobHandler {

	protected final Log logger = LogFactory.getLog(getClass());

	public byte[] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as bytes");
		return rs.getBytes(columnIndex);
	}

	public InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as binary stream");
		return rs.getBinaryStream(columnIndex);
	}

	public String getClobAsString(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as string");
		return rs.getString(columnIndex);
	}

	public InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as ASCII stream");
		return rs.getAsciiStream(columnIndex);
	}

	public Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as character stream");
		return rs.getCharacterStream(columnIndex);
	}

	public LobCreator getLobCreator() {
		return new DefaultLobCreator();
	}


	protected class DefaultLobCreator implements LobCreator {

		public void setBlobAsBytes(PreparedStatement ps, int parameterIndex, byte[] content)
				throws SQLException {
			ps.setBytes(parameterIndex, content);
			logger.debug("Set bytes for BLOB with length " + content.length);
		}

		public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream binaryStream,
		                                  int contentLength)
				throws SQLException {
			ps.setBinaryStream(parameterIndex, binaryStream, contentLength);
			logger.debug("Set binary stream for BLOB with length " + contentLength);
		}

		public void setClobAsString(PreparedStatement ps, int parameterIndex, String content)
		    throws SQLException {
			ps.setString(parameterIndex, content);
			logger.debug("Set string for CLOB with length " + content.length());
		}

		public void setClobAsAsciiStream(PreparedStatement ps, int parameterIndex, InputStream asciiStream,
		                                 int contentLength)
		    throws SQLException {
			ps.setAsciiStream(parameterIndex, asciiStream, contentLength);
			logger.debug("Set ASCII stream for CLOB with length " + contentLength);
		}


		public void setClobAsCharacterStream(PreparedStatement ps, int parameterIndex, Reader characterStream,
		                                     int contentLength)
		    throws SQLException {
			ps.setCharacterStream(parameterIndex, characterStream, contentLength);
			logger.debug("Set character stream for CLOB with length " + contentLength);
		}

		public void close() {
			// nothing to do here
		}
	}

}
