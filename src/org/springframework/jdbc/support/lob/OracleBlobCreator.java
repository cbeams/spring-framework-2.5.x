package org.springframework.jdbc.support.lob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.FileCopyUtils;

/**
 * BlobCreator implementation for Oracle databases.
 * Uses proprietary API to create oracle.sql.BLOB instances for both createBlob
 * and setBlobAsBinaryStream, as necessary when working with Oracle's JDBC driver.
 *
 * <p>While most databases are able to work with DefaultBlobCreator, Oracle just
 * accepts java.sql.Blob instances created via its own proprietary BLOB API, and
 * additionally doesn't accept large streams for PreparedStatement.setBinaryStream.
 * Therefore, you need to use a strategy like this BlobCreator implementation.
 *
 * <p>Needs to work on a native JDBC Connection, to be able to cast it to
 * oracle.jdbc.OracleConnection. If you pass in Connections from a connection
 * pool (the usual case in a J2EE environment), you need to set an appropriate
 * NativeJdbcExtractor to allow for automatical retrieval of the underlying
 * native JDBC Connection. BlobCreator and NativeJdbcExtractor are separate
 * concerns, therefore they are represented by separate strategy interfaces.
 *
 * <p>Coded via reflection to avoid dependencies on Oracle classes.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #setNativeJdbcExtractor
 */
public class OracleBlobCreator implements BlobCreator {

	private static final int DURATION_SESSION = 10;

	private static final int MODE_READWRITE = 1;

	protected final Log logger = LogFactory.getLog(getClass());

	private NativeJdbcExtractor nativeJdbcExtractor;

	/**
	 * Set an appropriate NativeJdbcExtractor to be able to retrieve the underlying
	 * native oracle.jdbc.OracleConnection. This is necessary for any "real"
	 * connection pool, as a pool needs to return wrapped Connection handles.
	 * @see oracle.jdbc.OracleConnection
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		this.nativeJdbcExtractor = extractor;
	}


	public Blob createBlob(Connection con, byte[] content) throws SQLException {
		Connection conToUse = (this.nativeJdbcExtractor != null) ?
		    this.nativeJdbcExtractor.getNativeConnection(con) : con;
		return doCreateBlob(conToUse, content);
	}

	public Blob createBlob(Connection con, InputStream contentStream) throws SQLException, IOException {
		Connection conToUse = (this.nativeJdbcExtractor != null) ?
		    this.nativeJdbcExtractor.getNativeConnection(con) : con;
		return doCreateBlob(conToUse, contentStream);
	}

	public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, byte[] content)
	    throws SQLException {
		Connection conToUse = (this.nativeJdbcExtractor != null) ?
		    this.nativeJdbcExtractor.getNativeConnectionFromStatement(ps) : ps.getConnection();
		ps.setBlob(parameterIndex, doCreateBlob(conToUse, content));
	}

	public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex, InputStream contentStream)
	    throws SQLException, IOException {
		Connection conToUse = (this.nativeJdbcExtractor != null) ?
		    this.nativeJdbcExtractor.getNativeConnectionFromStatement(ps) : ps.getConnection();
		ps.setBlob(parameterIndex, doCreateBlob(conToUse, contentStream));
	}


	protected Blob doCreateBlob(Connection con, byte[] content) throws SQLException {
		try {
			Blob blob = prepareBlob(con);
			getBinaryOutputStream(blob).write(content);
			closeBlob(blob);
			logger.debug("Created new Oracle BLOB with length " + blob.length());
			return blob;
		}
		catch (SQLException ex) {
			throw ex;
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof SQLException) {
				throw (SQLException) ex.getTargetException();
			}
			else {
				throw new DataAccessResourceFailureException("Could not create Oracle BLOB", ex.getTargetException());
			}
		}
		catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not create Oracle BLOB", ex);
		}
	}

	protected Blob doCreateBlob(Connection con, InputStream contentStream) throws SQLException, IOException {
		try {
			Blob blob = prepareBlob(con);
			FileCopyUtils.copy(contentStream, getBinaryOutputStream(blob));
			closeBlob(blob);
			logger.debug("Created new Oracle BLOB with length " + blob.length());
			return blob;
		}
		catch (SQLException ex) {
			throw ex;
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof SQLException) {
				throw (SQLException) ex.getTargetException();
			}
			else if (ex.getTargetException() instanceof IOException) {
				throw (IOException) ex.getTargetException();
			}
			else {
				throw new DataAccessResourceFailureException("Could not create Oracle BLOB", ex.getTargetException());
			}
		}
		catch (Exception ex) {
			throw new DataAccessResourceFailureException("Could not create Oracle BLOB", ex);
		}
	}

	/**
	 * Create and open an oracle.sql.BLOB instance via reflection.
	 */
	protected Blob prepareBlob(Connection con) throws Exception {
		Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");
		if (!oracleConnectionClass.isAssignableFrom(con.getClass())) {
			throw new InvalidDataAccessApiUsageException("OracleBlobCreator needs to work on OracleConnection - " +
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
		Object blob = createTemporary.invoke(null, new Object[] {con, Boolean.FALSE, new Integer(DURATION_SESSION)});
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
