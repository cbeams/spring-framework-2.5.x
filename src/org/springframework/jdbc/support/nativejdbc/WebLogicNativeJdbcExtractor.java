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

package org.springframework.jdbc.support.nativejdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Implementation of the NativeJdbcExtractor interface for WebLogic Server.
 * Returns the underlying native Connection to application code instead
 * of WebLogic's wrapper implementation; unwraps the Connection for
 * native statements. The returned JDBC classes can then safely be cast,
 * e.g. to OracleConnection.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working
 * with a WebLogic DataSource: If a given object is not a WebLogic
 * Connection wrapper, it will be returned as-is.
 *
 * <p>Currently just tested with BEA WebLogic 8.1 SP2.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 31.05.2004
 * @see #getNativeConnection
 * @see weblogic.jdbc.extensions.WLConnection#getVendorConnection
 */
public class WebLogicNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String JDBC_EXTENSION_NAME = "weblogic.jdbc.extensions.WLConnection";

	private final Class jdbcExtensionClass;

	private final Method getVendorConnectionMethod;
	
	/**
	 * This constructor retrieves the WebLogic JDBC extension interface,
	 * so we can get the underlying vendor connection using reflection.
	 */
	public WebLogicNativeJdbcExtractor() {
		try {
			this.jdbcExtensionClass = getClass().getClassLoader().loadClass(JDBC_EXTENSION_NAME);
			this.getVendorConnectionMethod = this.jdbcExtensionClass.getMethod("getVendorConnection", null);
		}
		catch (Exception ex) {
			throw new InvalidDataAccessApiUsageException(
					"Couldn't initialize WebLogicNativeJdbcExtractor because WebLogic API classes are not available", ex);
		}
	}

	/**
	 * Return true, as WebLogic returns wrapped Statements.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	/**
	 * Return true, as WebLogic returns wrapped PreparedStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return true;
	}

	/**
	 * Return true, as WebLogic returns wrapped CallableStatements.
	 */
	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	/**
	 * Retrieve the Connection via WebLogic's <code>getVendorConnection</code> method.
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		if (this.jdbcExtensionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) this.getVendorConnectionMethod.invoke(con, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke WebLogic's getVendorConnection method", ex);
			}
		}
		return con;
	}

}
