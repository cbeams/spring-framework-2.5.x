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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Implementation of the NativeJdbcExtractor interface for WebLogic Server.
 * Returns the underlying native Connection, Statement, ResultSet etc to application 
 * code instead of WebLogic's wrapper implementations.  The returned JDBC classes can 
 * then safely be cast, e.g. to OracleResultSet.
 *
 *
 * @author Thomas Risberg
 * @since 31.05.2004
 * @see org.springframework.jdbc.core.JdbcTemplate#setNativeJdbcExtractor
 */
public class WebLogicNativeJdbcExtractor implements NativeJdbcExtractor {

	private boolean nativeConnectionNecessaryForNativeStatements = false;

	private boolean nativeConnectionNecessaryForNativePreparedStatements = false;

	private boolean nativeConnectionNecessaryForNativeCallableStatements = false;
	
	private static final String WEBLOGIC_JDBC_EXTENSION_NAME = "weblogic.jdbc.extensions.WLConnection";
	
	private final Class jdbcExtension;
	
	/**
	 * This constructor retrieves the WebLogic jdbc extension interface so we can get the underlying 
	 * vendor connection using reflectoin.
	 * @throws ClassNotFoundException
	 */
	public WebLogicNativeJdbcExtractor() throws ClassNotFoundException {
		
		this.jdbcExtension = getClass().getClassLoader().loadClass(WEBLOGIC_JDBC_EXTENSION_NAME);
		
	}


	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native Statements. Default is false. If true, the Connection
	 * will be unwrapped first to create a Statement.
	 * <p>This makes sense if you need to work with native Statements from
	 * a pool that does not allow to extract the native JDBC objects from its
	 * wrappers but returns the native Connection on DatabaseMetaData.getConnection.
	 * <p>The standard SimpleNativeJdbcExtractor is unable to unwrap statements,
	 * so set this to true if your connection pool wraps Statements.
	 * @see java.sql.Connection#createStatement
	 * @see java.sql.DatabaseMetaData#getConnection
	 */
	public void setNativeConnectionNecessaryForNativeStatements(boolean nativeConnectionNecessaryForNativeStatements) {
		this.nativeConnectionNecessaryForNativeStatements = nativeConnectionNecessaryForNativeStatements;
	}

	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return nativeConnectionNecessaryForNativeStatements;
	}

	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native PreparedStatements. Default is false. If true,
	 * the Connection will be unwrapped first to create a PreparedStatement.
	 * <p>This makes sense if you need to work with native PreparedStatements from
	 * a pool that does not allow to extract the native JDBC objects from its
	 * wrappers but returns the native Connection on Statement.getConnection.
	 * <p>The standard SimpleNativeJdbcExtractor is unable to unwrap statements,
	 * so set this to true if your connection pool wraps PreparedStatements.
	 * @see java.sql.Connection#prepareStatement
	 * @see java.sql.DatabaseMetaData#getConnection
	 */
	public void setNativeConnectionNecessaryForNativePreparedStatements(boolean nativeConnectionNecessary) {
		this.nativeConnectionNecessaryForNativePreparedStatements = nativeConnectionNecessary;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return nativeConnectionNecessaryForNativePreparedStatements;
	}

	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native CallableStatements. Default is false. If true,
	 * the Connection will be unwrapped first to create a CallableStatement.
	 * <p>This makes sense if you need to work with native CallableStatements from
	 * a pool that does not allow to extract the native JDBC objects from its
	 * wrappers but returns the native Connection on Statement.getConnection.
	 * <p>The standard SimpleNativeJdbcExtractor is unable to unwrap statements,
	 * so set this to true if your connection pool wraps CallableStatements.
	 * @see java.sql.Connection#prepareCall
	 * @see java.sql.DatabaseMetaData#getConnection
	 */
	public void setNativeConnectionNecessaryForNativeCallableStatements(boolean nativeConnectionNecessary) {
		this.nativeConnectionNecessaryForNativeCallableStatements = nativeConnectionNecessary;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return nativeConnectionNecessaryForNativeCallableStatements;
	}


	/**
	 * Retrieve the Connection via the DatabaseMetaData object, which will
	 * result in the native JDBC Connection with many connection pools.
	 * @see java.sql.DatabaseMetaData#getConnection
	 */
	public Connection getNativeConnection(Connection con) throws SQLException {
		
		Connection vendorConnection = null;
		
		try {
			Method getVendorConn = jdbcExtension.getMethod("getVendorConnection", new Class[] {});
			Object o = getVendorConn.invoke(con, new Object[] {});
			vendorConnection = (Connection) o;
		}
		catch (IllegalAccessException iae) {
			throw new DataAccessResourceFailureException("Could not unwrap WebLogic connection wrapper", iae);
		}
		catch (InvocationTargetException ite) {
			throw new DataAccessResourceFailureException("Could not unwrap WebLogic connection wrapper", ite);
		}
		catch (NoSuchMethodException nsme) {
			throw new DataAccessResourceFailureException("Could not unwrap WebLogic connection wrapper", nsme);
		}
		
		if (vendorConnection == null)
			return con.getMetaData().getConnection();
		else
			return vendorConnection;
	}

	/**
	 * Retrieve the Connection via the DatabaseMetaData object of the
	 * Statement's Connection.
	 * @see #getNativeConnection
	 * @see java.sql.Statement#getConnection
	 */
	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return getNativeConnection(stmt.getConnection());
	}

	/**
	 * Not able to unwrap: return passed-in Statement.
	 */
	public Statement getNativeStatement(Statement stmt) {
		return stmt;
	}

	/**
	 * Not able to unwrap: return passed-in PreparedStatement.
	 */
	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) {
		return ps;
	}

	/**
	 * Not able to unwrap: return passed-in CallableStatement.
	 */
	public CallableStatement getNativeCallableStatement(CallableStatement cs) {
		return cs;
	}

	/**
	 * Not able to unwrap: return passed-in ResultSet.
	 */
	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
