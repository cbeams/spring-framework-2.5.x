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
import java.sql.SQLException;

/**
 * Interface that abstracts potentially database-specific creation of large binary
 * fields and large text fields. Does not work with java.sql.Blob and java.sql.Clob
 * instances in the API, as some JDBC drivers do not support these types as such.
 *
 * <p>A LobCreator represents a session for creating BLOBs: It is <i>not</i>
 * thread-safe and needs to be instantiated for each statement execution or for
 * each transaction. Each LobCreator needs to be closed after completion.
 *
 * <p>According to the JDBC spec, one should be able to use setBytes/setBinaryStream
 * for any BLOB contents, analogously for CLOBs: Therefore, implementations should
 * use those direct setters if possible, for the sake of efficiency.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #close
 * @see LobHandler#getLobCreator
 * @see DefaultLobHandler.DefaultLobCreator
 * @see OracleLobHandler.OracleLobCreator
 * @see java.sql.PreparedStatement#setBlob
 * @see java.sql.PreparedStatement#setBinaryStream
 * @see java.sql.PreparedStatement#setString
 * @see java.sql.PreparedStatement#setAsciiStream
 * @see java.sql.PreparedStatement#setCharacterStream
 */
public interface LobCreator {

	/**
	 * Set the given content as bytes on the given statement, using the given
	 * parameter index. Might simply invoke PreparedStatement.setBytes
	 * or create a Blob instance for it, depending on the database and driver.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param content the content as byte array
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setBlobAsBytes(PreparedStatement ps, int parameterIndex, byte[] content)
	    throws SQLException;

	/**
	 * Set the given content as binary stream on the given statement, using the
	 * given parameter index. Might simply invoke PreparedStatement.setBinaryStream
	 * or create a Blob instance for it, depending on the database and driver.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param contentStream the content as InputStream
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream,
	                           int contentLength)
	    throws SQLException;

	/**
	 * Set the given content as String on the given statement, using the given
	 * parameter index. Might simply invoke PreparedStatement.setString
	 * or create a Clob instance for it, depending on the database and driver.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param content the content as byte array
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.PreparedStatement#setBytes
	 */
	void setClobAsString(PreparedStatement ps, int parameterIndex, String content)
	    throws SQLException;

	/**
	 * Set the given content as ASCII stream on the given statement, using the
	 * given parameter index. Might simply invoke PreparedStatement.setAsciiStream
	 * or create a Clob instance for it, depending on the database and driver.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param asciiStream the content as InputStream
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setClobAsAsciiStream(PreparedStatement ps, int parameterIndex, InputStream asciiStream,
	                          int contentLength)
	    throws SQLException;

	/**
	 * Set the given content as character stream on the given statement, using the
	 * given parameter index. Might simply invoke PreparedStatement.setCharacterStream
	 * or create a Clob instance for it, depending on the database and driver.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param characterStream the content as InputStream
	 * @throws SQLException if thrown by JDBC methods
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setClobAsCharacterStream(PreparedStatement ps, int parameterIndex, Reader characterStream,
	                              int contentLength)
	    throws SQLException;

	/**
	 * Close this LobCreator session and free its temporarily created BLOBs and CLOBs.
	 * Will not need to do anything if using PreparedStatement's standard methods,
	 * but might be necessary to free database resources if using proprietary means.
	 * <p><b>NOTE</b>: Needs to be invoked after the involved PreparedStatements have
	 * been executed respectively the affected O/R mapping sessions have been flushed.
	 * Else, the database resources for the temporary BLOBs might stay allocated.
	 */
	void close();

}
