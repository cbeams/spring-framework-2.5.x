package org.springframework.jdbc.support.lob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.FileCopyUtils;

/**
 * LobHandler implementation for Oracle databases. Uses proprietary API to
 * create oracle.sql.BLOB and oracle.sql.CLOB instances, as necessary when
 * working with Oracle's JDBC driver. Developed and tested on Oracle 9i.
 *
 * <p>While most databases are able to work with DefaultLobHandler, Oracle just
 * accepts Blob/Clob instances created via its own proprietary BLOB/CLOB API,
 * and additionally doesn't accept large streams for PreparedStatement's
 * corresponding setter methods. Therefore, you need to use a strategy like
 * this LobHandler implementation.
 *
 * <p>Needs to work on a native JDBC Connection, to be able to cast it to
 * oracle.jdbc.OracleConnection. If you pass in Connections from a connection
 * pool (the usual case in a J2EE environment), you need to set an appropriate
 * NativeJdbcExtractor to allow for automatical retrieval of the underlying
 * native JDBC Connection. LobHandler and NativeJdbcExtractor are separate
 * concerns, therefore they are represented by separate strategy interfaces.
 *
 * <p>Coded via reflection to avoid dependencies on Oracle classes.
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see #setNativeJdbcExtractor
 */
public class OracleLobHandler implements LobHandler {

	protected final Log logger = LogFactory.getLog(getClass());

	private NativeJdbcExtractor nativeJdbcExtractor;

	/**
	 * Set an appropriate NativeJdbcExtractor to be able to retrieve the underlying
	 * native oracle.jdbc.OracleConnection. This is necessary for any DataSource-based
	 * connection pool, as such a pool needs to return wrapped Connection handles.
	 * @see oracle.jdbc.OracleConnection
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor extractor) {
		this.nativeJdbcExtractor = extractor;
	}
	

	public byte[] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as bytes");
		Blob blob = rs.getBlob(columnIndex);
		return (blob != null ? blob.getBytes(1, (int) blob.length()) : new byte[0]);
	}

	public InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning BLOB as binary stream");
		Blob blob = rs.getBlob(columnIndex);
		return (blob != null ? blob.getBinaryStream() : new ByteArrayInputStream(new byte[0]));
	}

	public String getClobAsString(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as string");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getSubString(1, (int) clob.length()) : "");
	}

	public InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as ASCII stream");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getAsciiStream() : new ByteArrayInputStream(new byte[0]));
	}

	public Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException {
		logger.debug("Returning CLOB as character stream");
		Clob clob = rs.getClob(columnIndex);
		return (clob != null ? clob.getCharacterStream() : new StringReader(""));
	}

	public LobCreator getLobCreator() {
		return new OracleLobCreator();
	}


	protected class OracleLobCreator implements LobCreator {

		private static final int DURATION_SESSION = 10;

		private static final int MODE_READWRITE = 1;

		private final List createdLobs = new ArrayList();

		public void setBlobAsBytes(PreparedStatement ps, int parameterIndex, final byte[] content)
				throws SQLException {
			Blob blob = (Blob) createLob(ps, "oracle.sql.BLOB", new LobCallback() {
				public void populateLob(Object lob) throws Exception {
					Method methodToInvoke = lob.getClass().getMethod("getBinaryOutputStream", new Class[0]);
					((OutputStream) methodToInvoke.invoke(lob, null)).write(content);
				}
			});
			ps.setBlob(parameterIndex, blob);
			logger.debug("Set bytes for BLOB with length " + content.length);
		}

		public void setBlobAsBinaryStream(PreparedStatement ps, int parameterIndex,
		                                  final InputStream binaryStream, int contentLength)
				throws SQLException {
			Blob blob = (Blob) createLob(ps, "oracle.sql.BLOB", new LobCallback() {
				public void populateLob(Object lob) throws Exception {
					Method methodToInvoke = lob.getClass().getMethod("getBinaryOutputStream", null);
					FileCopyUtils.copy(binaryStream, ((OutputStream) methodToInvoke.invoke(lob, null)));
				}
			});
			ps.setBlob(parameterIndex, blob);
			logger.debug("Set binary stream for BLOB with length " + contentLength);
		}

		public void setClobAsString(PreparedStatement ps, int parameterIndex, final String content)
		    throws SQLException {
			Clob clob = (Clob) createLob(ps, "oracle.sql.CLOB", new LobCallback() {
				public void populateLob(Object lob) throws Exception {
					Method methodToInvoke = lob.getClass().getMethod("getCharacterOutputStream", null);
					Writer writer = ((Writer) methodToInvoke.invoke(lob, null));
					writer.write(content);
					writer.close();
				}
			});
			ps.setClob(parameterIndex, clob);
			logger.debug("Set string for CLOB with length " + content.length());
		}

		public void setClobAsAsciiStream(PreparedStatement ps, int parameterIndex,
		                                 final InputStream asciiStream, int contentLength)
		    throws SQLException {
			Clob clob = (Clob) createLob(ps, "oracle.sql.CLOB", new LobCallback() {
				public void populateLob(Object lob) throws Exception {
					Method methodToInvoke = lob.getClass().getMethod("getAsciiOutputStream", null);
					FileCopyUtils.copy(asciiStream, ((OutputStream) methodToInvoke.invoke(lob, null)));
				}
			});
			ps.setClob(parameterIndex, clob);
			logger.debug("Set ASCII stream for CLOB with length " + contentLength);
		}

		public void setClobAsCharacterStream(PreparedStatement ps, int parameterIndex,
		                                     final Reader characterStream, int contentLength)
		    throws SQLException {
			Clob clob = (Clob) createLob(ps, "oracle.sql.CLOB", new LobCallback() {
				public void populateLob(Object lob) throws Exception {
					Method methodToInvoke = lob.getClass().getMethod("getCharacterOutputStream", null);
					FileCopyUtils.copy(characterStream, ((Writer) methodToInvoke.invoke(lob, null)));
				}
			});
			ps.setClob(parameterIndex, clob);
			logger.debug("Set character stream for CLOB with length " + contentLength);
		}


		/**
		 * Create a LOB instance for the given PreparedStatement,
		 * populating it via the given callback.
		 */
		protected Object createLob(PreparedStatement ps, String lobClass, LobCallback callback) throws SQLException {
			try {
				Object lob = prepareLob(getOracleConnection(ps), lobClass);
				callback.populateLob(lob);
				lob.getClass().getMethod("close", null).invoke(lob, null);
				this.createdLobs.add(lob);
				logger.debug("Created new Oracle LOB");
				return lob;
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

		/**
		 * Retrieve the underlying OracleConnection, using a NativeJdbcExtractor if set.
		 */
		protected Connection getOracleConnection(PreparedStatement ps) throws SQLException, ClassNotFoundException {
			Connection conToUse = (nativeJdbcExtractor != null) ?
					nativeJdbcExtractor.getNativeConnectionFromStatement(ps) : ps.getConnection();
			Class oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection");
			if (!oracleConnectionClass.isAssignableFrom(conToUse.getClass())) {
				throw new InvalidDataAccessApiUsageException("OracleLobHandler needs to work on OracleConnection - " +
																										 "maybe set the nativeJdbcExtractor property?");
			}
			return conToUse;
		}

		/**
		 * Create and open an oracle.sql.BLOB/CLOB instance via reflection.
		 */
		protected Object prepareLob(Connection con, String lobClassName) throws Exception {
			/*
			BLOB blob = BLOB.createTemporary(con, false, BLOB.DURATION_SESSION);
			blob.open(BLOB.MODE_READWRITE);
			return blob;
			*/
			Class lobClass = Class.forName(lobClassName);
			Method createTemporary = lobClass.getMethod("createTemporary",
			                                            new Class[] {Connection.class, boolean.class, int.class});
			Object lob = createTemporary.invoke(null, new Object[] {con, Boolean.FALSE, new Integer(DURATION_SESSION)});
			Method open = lobClass.getMethod("open", new Class[] {int.class});
			open.invoke(lob, new Object[] {new Integer(MODE_READWRITE)});
			return lob;
		}

		/**
		 * Free all temporary BLOBs and CLOBs created by this creator.
		 */
		public void close() {
			try {
				for (Iterator it = this.createdLobs.iterator(); it.hasNext();) {
					/*
					BLOB blob = (BLOB) it.next();
					blob.freeTemporary();
					*/
					Object lob = it.next();
					Method freeTemporary = lob.getClass().getMethod("freeTemporary", new Class[0]);
					freeTemporary.invoke(lob, new Object[0]);
					it.remove();
				}
			}
			catch (InvocationTargetException ex) {
				logger.error("Could not free Oracle LOB", ex.getTargetException());
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not free Oracle LOB", ex);
			}
		}
	}


	/**
	 * Internal callback interface for use with createLob.
	 * @see OracleLobCreator#createLob
	 */
	protected static interface LobCallback {

		/**
		 * Populate the given BLOB or CLOB instance with content.
		 * @throws Exception any exception including InvocationTargetException
		 */
		void populateLob(Object lob) throws Exception;
	}

}
