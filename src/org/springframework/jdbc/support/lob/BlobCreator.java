package org.springframework.jdbc.support.lob;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface that abstracts potentially database-specific creation of java.sql.Blob
 * instances, for use with PreparedStatement.setBlob or O/R mapping fields of type Blob.
 *
 * <p>A BlobCreator represents a session for creating BLOBs: It is <i>not</i>
 * thread-safe and needs to be instantiated for each statement execution or for
 * each transaction. Each BlobCreator needs to be closed after completion.
 *
 * <p>It's advisable to use setBlobAsBytes/BinaryStream if just needing to set BLOB
 * contents to a field, instead of invoking createBlob plus PreparedStatement.setBlob.
 * According to the JDBC spec, one should be able to use setBytes/setBinaryStream
 * for any BLOB contents: Therefore, implementations should use those if possible,
 * for the sake of efficiency.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #close
 * @see LobHandler
 * @see DefaultLobHandler
 * @see OracleLobHandler
 * @see java.sql.PreparedStatement#setBlob
 * @see java.sql.PreparedStatement#setBinaryStream
 */
public interface BlobCreator {

	/**
	 * Create a Blob for the given content.
	 * May access the current Connection if necessary.
	 * @param con the current JDBC Connection
	 * @param content the content as byte array
	 * @return the new Blob instance
	 * @throws SQLException if thrown by JDBC methods
	 */
	Blob createBlob(Connection con, byte[] content)
	    throws SQLException;

	/**
	 * Create a Blob for the given content.
	 * May access the current Connection if necessary.
	 * @param con the current JDBC Connection
	 * @param contentStream the content as InputStream
	 * @return the new Blob instance
	 * @throws SQLException if thrown by JDBC methods
	 * @throws IOException if thrown by streaming methods
	 */
	Blob createBlob(Connection con, InputStream contentStream)
	    throws SQLException, IOException;

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
	 * @throws IOException if thrown by streaming methods
	 * @see java.sql.PreparedStatement#setBinaryStream
	 */
	void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream)
	    throws SQLException, IOException;

	/**
	 * Close this BlobCreator session and free its temporarily created BLOBs.
	 * Will not need to do anything if using PreparedStatement's standard methods,
	 * but might be necessary to free database resources if using proprietary means.
	 * <p><b>NOTE</b>: Needs to be invoked after the involved PreparedStatements have
	 * been executed respectively the affected O/R mapping sessions have been flushed.
	 * Else, the database resources for the temporary BLOBs might stay allocated.
	 * @throws SQLException if thrown by JDBC methods
	 */
	void close() throws SQLException;

}
