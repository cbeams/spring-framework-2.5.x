package org.springframework.jdbc.support.nativejdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * <p>Supports both WebSphere 5 and WebSphere 4. Currently just tested with
 * IBM WebSphere 5.1.0 and 4.0.6. Thanks to Dave Keller and Victor for
 * figuring out how to do the unwrapping on WebSphere 5 respectively 4!
 *
 * @author Juergen Hoeller
 * @since 08.06.2004
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcConnection
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcUtil#getNativeConnection
 * @see com.ibm.ejs.cm.proxy.ConnectionProxy#getPhysicalConnection
 */
public class WebSphereNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String JDBC_ADAPTER_CONNECTION_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcConnection";

	private static final String JDBC_ADAPTER_UTIL_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcUtil";

	private static final String CONNECTION_PROXY_NAME_4 = "com.ibm.ejs.cm.proxy.ConnectionProxy";

	protected final Log logger = LogFactory.getLog(getClass());

	private Class webSphereConnectionClass;

	private Method getNativeConnectionMethod;

	private boolean webSphere5;

	/**
	 * This constructor retrieves WebSphere JDBC adapter classes,
	 * so we can get the underlying vendor connection using reflection.
	 */
	public WebSphereNativeJdbcExtractor() throws InvalidDataAccessApiUsageException {
		try {
			logger.debug("Trying WebSphere 5: " + JDBC_ADAPTER_CONNECTION_NAME_5);
			this.webSphereConnectionClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_CONNECTION_NAME_5);
			Class jdbcAdapterUtilClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_UTIL_NAME_5);
			this.getNativeConnectionMethod =
					jdbcAdapterUtilClass.getMethod("getNativeConnection", new Class[] {this.webSphereConnectionClass});
			this.webSphere5 = true;
		}
		catch (Exception ex) {
			logger.debug("Could not find WebSphere 5 connection pool classes", ex);
			try {
				logger.debug("Trying WebSphere 4: " + CONNECTION_PROXY_NAME_4);
				this.webSphereConnectionClass = getClass().getClassLoader().loadClass(CONNECTION_PROXY_NAME_4);
				this.getNativeConnectionMethod =
						this.webSphereConnectionClass.getMethod("getPhysicalConnection", null);
				this.webSphere5 = false;
			}
			catch (Exception ex2) {
				logger.debug("Could not find WebSphere 4 connection pool classes", ex2);
				throw new InvalidDataAccessApiUsageException(
						"Could neither find WebSphere 5 nor WebSphere 4 connection pool classes");
			}
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
		if (this.webSphereConnectionClass.isAssignableFrom(con.getClass())) {
			try {
				if (this.webSphere5) {
					// WebSphere 5's WSJdbcUtil.getNativeConnection(wsJdbcConnection)
					return (Connection) this.getNativeConnectionMethod.invoke(null, new Object[] {con});
				}
				else {
					// WebSphere 4's connectionProxy.getPhysicalConnection()
					return (Connection) this.getNativeConnectionMethod.invoke(con, null);
				}
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke WebSphere's getNativeConnection method", ex);
			}
		}
		return con;
	}

}
