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
 * <p>It's advisable to use setBlobAsBinaryStream if just needing to set BLOB contents
 * to a field, instead of invoking createBlob plus PreparedStatement.setBlob. According
 * to the JDBC spec, one should be able to use setBinaryStream for any BLOB contents;
 * therefore, implementations will use it if possible, for the sake of efficiency. 
 *
 * <p>Most databases should be able to work with DefaultBlobCreator. Unfortunately,
 * Oracle just accepts Blob instances created via its own proprietary BLOB API, and
 * additionally doesn't accept large streams for PreparedStatement.setBinaryStream.
 * Therefore, you need to use OracleBlobCreator there, which uses Oracle's BLOB API
 * for both createBlob and setBlobAsBinaryStream.
 *
 * <p>Note that PreparedStatement's setBlob implementation is broken with MySQL
 * Connector/J 3.0.9: Nevertheless, DefaultBlobCreator's setBlobAsBinaryStream
 * will work as it calls setBinaryStream instead of setBlob, as outlined above.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see DefaultBlobCreator
 * @see OracleBlobCreator
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
	 * Set the given content as binary stream on the given statement,
	 * using the given parameter index.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param content the content as byte array
	 * @throws SQLException if thrown by JDBC methods
	 */
	void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, byte[] content)
	    throws SQLException;

	/**
	 * Set the given content as binary stream on the given statement,
	 * using the given parameter index.
	 * @param ps the PreparedStatement to the set the content on
	 * @param parameterIndex the parameter index to use
	 * @param contentStream the content as InputStream
	 * @throws SQLException if thrown by JDBC methods
	 * @throws IOException if thrown by streaming methods
	 */
	void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream)
	    throws SQLException, IOException;

}
