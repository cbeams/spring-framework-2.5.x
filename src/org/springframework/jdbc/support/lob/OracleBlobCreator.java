package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.FileCopyUtils;

/**
 * BlobCreator implementation for Oracle databases.
 * Uses proprietary API to create oracle.sql.BLOB instances,
 * as necessary when working with Oracle's JDBC driver.
 *
 * <p>Coded via reflection to avoid dependencies on Oracle jars.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 */
public class OracleBlobCreator implements BlobCreator {

	private static final int DURATION_SESSION = 10;

	private static final int MODE_READWRITE = 1;

	private NativeJdbcExtractor nativeJdbcExtractor;

	/**
	 * Set an appropriate NativeJdbcExtractor to be able to retrieve the
	 * underlying native oracle.jdbc.OracleConnection. This is necessary for
	 * any connection pool, as a pool needs to return wrapped Connection handles. 
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		this.nativeJdbcExtractor = extractor;
	}

	public Blob createBlob(Connection con, byte[] content) throws DataAccessException {
		try {
			Blob blob = prepareBlob(con);
			getBinaryOutputStream(blob).write(content);
			closeBlob(blob);
			return blob;
		}
		catch (Exception ex) {
			throw new InvalidDataAccessResourceUsageException("Could not create Oracle BLOB", ex);
		}
	}

	public Blob createBlob(Connection con, InputStream contentStream) throws DataAccessException {
		try {
			Blob blob = prepareBlob(con);
			FileCopyUtils.copy(contentStream, getBinaryOutputStream(blob));
			closeBlob(blob);
			return blob;
		}
		catch (Exception ex) {
			throw new InvalidDataAccessResourceUsageException("Could not create Oracle BLOB", ex);
		}
	}

	/**
	 * Create and open an oracle.sql.BLOB instance via reflection.
	 */
	protected Blob prepareBlob(Connection con) throws Exception {
		Connection conToUse = con;
		if (this.nativeJdbcExtractor != null) {
			conToUse = this.nativeJdbcExtractor.getNativeConnection(con);
		}
		Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");
		if (!oracleConnectionClass.isAssignableFrom(conToUse.getClass())) {
			throw new InvalidDataAccessApiUsageException("OracleBlobCreator needs to work on OracleConnection -- " +
			                                             "maybe set the nativeJdbcExtractor property?");
		}

		/*
		BLOB blob = BLOB.createTemporary(conToUse, false, BLOB.DURATION_SESSION);
		blob.open(BLOB.MODE_READWRITE);
		return blob;
		*/
		Class blobClass = Class.forName("oracle.sql.BLOB");
		Method createTemporary = blobClass.getMethod("createTemporary",
		                                             new Class[] {Connection.class, boolean.class, int.class});
		Object blob = createTemporary.invoke(null, new Object[] {conToUse, Boolean.FALSE, new Integer(DURATION_SESSION)});
		Method open = blobClass.getMethod("open", new Class[] {int.class});
		open.invoke(blob, new Object[] {new Integer(MODE_READWRITE)});
		return (Blob) blob;
	}

	/**
	 * Retrieve the binary output stream from the given oracle.sql.BLOB instance
	 * via reflection.
	 */
	protected OutputStream getBinaryOutputStream(Blob blob) throws Exception {
		/*
		blob.getBinaryOutputStream();
		*/
		Method getBinaryOutputStream = blob.getClass().getMethod("getBinaryOutputStream", null);
		return (OutputStream) getBinaryOutputStream.invoke(blob, null);
	}

	/**
	 * Close the given oracle.sql.BLOB instance via reflection.
	 */
	protected void closeBlob(Blob blob) throws Exception {
		/*
		blob.close();
		*/
		Method close = blob.getClass().getMethod("close", null);
		close.invoke(blob, null);
	}

}
