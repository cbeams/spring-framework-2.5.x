/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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

		public void setBlobAsBytes(PreparedStatement ps, int paramIndex, byte[] content)
				throws SQLException {
			ps.setBytes(paramIndex, content);
			if (logger.isDebugEnabled()) {
				logger.debug(content != null ? "Set bytes for BLOB with length " + content.length :
						"Set BLOB to null");
			}
		}

		public void setBlobAsBinaryStream(
				PreparedStatement ps, int paramIndex, InputStream binaryStream, int contentLength)
				throws SQLException {
			ps.setBinaryStream(paramIndex, binaryStream, contentLength);
			if (logger.isDebugEnabled()) {
				logger.debug(binaryStream != null ? "Set binary stream for BLOB with length " + contentLength :
						"Set BLOB to null");
			}
		}

		public void setClobAsString(PreparedStatement ps, int paramIndex, String content)
		    throws SQLException {
			ps.setString(paramIndex, content);
			if (logger.isDebugEnabled()) {
				logger.debug(content != null ? "Set string for CLOB with length " + content.length() :
						"Set CLOB to null");
			}
		}

		public void setClobAsAsciiStream(
				PreparedStatement ps, int paramIndex, InputStream asciiStream, int contentLength)
		    throws SQLException {
			ps.setAsciiStream(paramIndex, asciiStream, contentLength);
			if (logger.isDebugEnabled()) {
				logger.debug(asciiStream != null ? "Set ASCII stream for CLOB with length " + contentLength :
						"Set CLOB to null");
			}
		}


		public void setClobAsCharacterStream(
				PreparedStatement ps, int paramIndex, Reader characterStream, int contentLength)
		    throws SQLException {
			ps.setCharacterStream(paramIndex, characterStream, contentLength);
			if (logger.isDebugEnabled()) {
				logger.debug(characterStream != null ? "Set character stream for CLOB with length " + contentLength :
						"Set CLOB to null");
			}
		}

		public void close() {
			// nothing to do here
		}
	}

}
