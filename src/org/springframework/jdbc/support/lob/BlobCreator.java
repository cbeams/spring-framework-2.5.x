package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;

import org.springframework.dao.DataAccessException;

/**
 * Interface that abstracts potentially database-specific creation
 * of java.sql.Blob instances, for use with PreparedStatement.setBlob
 * or O/R mapping fields of type Blob.
 *
 * <p>Most databases should be able to work with DefaultBlobCreator.
 * Unfortunately, Oracle just accepts Blob instances created via its own
 * proprietary BLOB API, therefore you need to use OracleBlobCreator there.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see java.sql.PreparedStatement#setBlob
 * @see DefaultBlobCreator
 * @see OracleBlobCreator
 */
public interface BlobCreator {

	/**
	 * Create a Blob for the given content.
	 * May access the current Connection if necessary.
	 * @param con the current JDBC Connection
	 * @param content the content as byte array
	 * @return the new Blob instance
	 * @throws DataAccessException in case of errors
	 */
	Blob createBlob(Connection con, byte[] content) throws DataAccessException;

	/**
	 * Create a Blob for the given content.
	 * May access the current Connection if necessary.
	 * @param con the current JDBC Connection
	 * @param contentStream the content as InputStream
	 * @return the new Blob instance
	 * @throws DataAccessException in case of errors
	 */
	Blob createBlob(Connection con, InputStream contentStream) throws DataAccessException;

}
