package org.springframework.jdbc.support.nativejdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Implementation of the NativeJdbcExtractor interface for WebSphere.
 * Returns the underlying native Connection to application code instead
 * of WebSphere's wrapper implementation; unwraps the Connection for
 * native statements. The returned JDBC classes can then safely be cast,
 * e.g. to OracleConnection.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working
 * with a WebSphere DataSource: If a given object is not a WebSphere
 * Connection wrapper, it will be returned as-is.
 *
 * <p>Currently just tested with IBM WebSphere 5.1.0. Thanks to
 * Dave Keller for figuring out how to do the unwrapping on WebSphere!
 *
 * @author Juergen Hoeller
 * @since 08.06.2004
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcConnection
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcUtil#getNativeConnection
 */
public class WebSphereNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String JDBC_ADAPTER_CONNECTION_NAME = "com.ibm.ws.rsadapter.jdbc.WSJdbcConnection";

	private static final String JDBC_ADAPTER_UTIL_NAME = "com.ibm.ws.rsadapter.jdbc.WSJdbcUtil";

	private final Class jdbcAdapterConnectionClass;

	private final Method getNativeConnectionMethod;

	/**
	 * This constructor retrieves WebSphere JDBC adapter classes,
	 * so we can get the underlying vendor connection using reflection.
	 */
	public WebSphereNativeJdbcExtractor() {
		try {
			this.jdbcAdapterConnectionClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_CONNECTION_NAME);
			Class jdbcAdapterUtilClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_UTIL_NAME);
			this.getNativeConnectionMethod =
					jdbcAdapterUtilClass.getMethod("getNativeConnection", new Class[] {this.jdbcAdapterConnectionClass});
		}
		catch (Exception ex) {
			throw new InvalidDataAccessApiUsageException(
					"Couldn't initialize WebSphereNativeJdbcExtractor because WebSphere API classes are not available", ex);
		}
	}

	/**
	 * Return true, as WebSphere returns wrapped Statements.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	/**
	 * Return true, as WebSphere returns wrapped PreparedStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return true;
	}

	/**
	 * Return true, as WebSphere returns wrapped CallableStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	/**
	 * Retrieve the Connection via WebSphere's <code>getNativeConnection</code> method.
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		if (this.jdbcAdapterConnectionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) this.getNativeConnectionMethod.invoke(null, new Object[] {con});
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke WebSphere's getNativeConnection method", ex);
			}
		}
		return con;
	}

}
