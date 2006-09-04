/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.support.nativejdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ReflectionUtils;

/**
 * Implementation of the NativeJdbcExtractor interface for WebSphere.
 *
 * <p>Returns the underlying native Connection to application code instead
 * of WebSphere's wrapper implementation; unwraps the Connection for
 * native statements. The returned JDBC classes can then safely be cast,
 * e.g. to <code>oracle.jdbc.OracleConnection</code>.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working
 * with a WebSphere DataSource: If a given object is not a WebSphere
 * Connection wrapper, it will be returned as-is.
 *
 * <p>Supports both WebSphere 5 and WebSphere 4. Currently tested with
 * IBM WebSphere 5.1.0, 5.0.2 and 4.0.6. Thanks to Dave Keller and Victor
 * for figuring out how to do the unwrapping on WebSphere 5 and 4!
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcConnection
 * @see com.ibm.ws.rsadapter.jdbc.WSJdbcUtil#getNativeConnection
 * @see com.ibm.ejs.cm.proxy.ConnectionProxy#getPhysicalConnection
 */
public class WebSphereNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String JDBC_ADAPTER_CONNECTION_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcConnection";

	private static final String JDBC_ADAPTER_UTIL_NAME_5 = "com.ibm.ws.rsadapter.jdbc.WSJdbcUtil";

	private static final String CONNECTION_PROXY_NAME_4 = "com.ibm.ejs.cm.proxy.ConnectionProxy";


	protected final Log logger = LogFactory.getLog(getClass());

	private Class webSphere5ConnectionClass;

	private Class webSphere4ConnectionClass;

	private Method webSphere5NativeConnectionMethod;

	private Method webSphere4PhysicalConnectionMethod;


	/**
	 * This constructor retrieves WebSphere JDBC adapter classes,
	 * so we can get the underlying vendor connection using reflection.
	 */
	public WebSphereNativeJdbcExtractor() {
		// Detect WebSphere 5 connection classes.
		try {
			logger.debug("Trying WebSphere 5 Connection: " + JDBC_ADAPTER_CONNECTION_NAME_5);
			this.webSphere5ConnectionClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_CONNECTION_NAME_5);
			Class jdbcAdapterUtilClass = getClass().getClassLoader().loadClass(JDBC_ADAPTER_UTIL_NAME_5);
			this.webSphere5NativeConnectionMethod =
					jdbcAdapterUtilClass.getMethod("getNativeConnection", new Class[] {this.webSphere5ConnectionClass});
		}
		catch (Exception ex) {
			logger.debug("Could not find WebSphere 5 connection pool classes", ex);
		}

		// Detect WebSphere 4 connection classes.
		// Might also be found on WebSphere 5, for version 4 DataSources.
		try {
			logger.debug("Trying WebSphere 4 Connection: " + CONNECTION_PROXY_NAME_4);
			this.webSphere4ConnectionClass = getClass().getClassLoader().loadClass(CONNECTION_PROXY_NAME_4);
			this.webSphere4PhysicalConnectionMethod =
					this.webSphere4ConnectionClass.getMethod("getPhysicalConnection", (Class[]) null);
		}
		catch (Exception ex) {
			logger.debug("Could not find WebSphere 4 connection pool classes", ex);
		}
	}
	

	/**
	 * Return <code>true</code>, as WebSphere returns wrapped Statements.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	/**
	 * Return <code>true</code>, as WebSphere returns wrapped PreparedStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return true;
	}

	/**
	 * Return <code>true</code>, as WebSphere returns wrapped CallableStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	/**
	 * Retrieve the Connection via WebSphere's <code>getNativeConnection</code> method.
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		// WebSphere 5 connection?
		if (this.webSphere5ConnectionClass != null &&
				this.webSphere5ConnectionClass.isAssignableFrom(con.getClass())) {
			// WebSphere 5's WSJdbcUtil.getNativeConnection(wsJdbcConnection)
			return (Connection) ReflectionUtils.invokeMethod(
					this.webSphere5NativeConnectionMethod, null, new Object[] {con});
		}

		// WebSphere 4 connection (or version 4 connection on WebSphere 5)?
		else if (this.webSphere4ConnectionClass != null &&
				this.webSphere4ConnectionClass.isAssignableFrom(con.getClass())) {
			// WebSphere 4's connectionProxy.getPhysicalConnection()
			return (Connection) ReflectionUtils.invokeMethod(this.webSphere4PhysicalConnectionMethod, con);
		}

		// No known WebSphere connection -> return as-is.
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Connection [" + con + "] is not a WebSphere 5/4 connection, returning as-is");
			}
			return con;
		}
	}

}
