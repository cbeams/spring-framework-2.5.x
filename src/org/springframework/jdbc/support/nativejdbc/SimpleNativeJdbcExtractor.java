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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.jdbc.support.JdbcUtils;

/**
 * Simple implementation of the NativeJdbcExtractor interface.
 * Assumes a pool that just wraps Connections but not Statements:
 * In this case, the underlying native Connection can be retrieved by simply
 * opening a Statement and invoking Statement.getConnection(). All other JDBC
 * objects will be returned as passed in.
 *
 * <p>Known to work with Resin 2.1 and 3.0, but should work with any pool that
 * does not wrap Statements. Note that the pool can still wrap PreparedStatements
 * etc: The only requirement of this extractor is that java.sql.Statement does
 * not get wrapped, returning the native Connection on getConnection().
 *
 * <p>Customize this extractor by setting the "nativeConnectionNecessaryForXxx"
 * flags accordingly: If PreparedStatements and/or CallableStatements are wrapped
 * by your pool, set the respective "nativeConnectionNecessaryForXxx" flags to
 * true. If none of the statement types is wrapped, the defaults are fine.
 *
 * @author Juergen Hoeller
 * @since 05.12.2003
 * @see java.sql.Statement#getConnection
 */
public class SimpleNativeJdbcExtractor implements NativeJdbcExtractor {

	private boolean nativeConnectionNecessaryForNativePreparedStatements = false;

	private boolean nativeConnectionNecessaryForNativeCallableStatements = false;


	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return false;
	}

	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native PreparedStatements. Default is false.
	 * <p>This makes sense if you need to work with native PreparedStatements from
	 * a pool that does not allow to extract the native JDBC objects from its
	 * wrappers but returns the native Connection on Statement.getConnection.
	 */
	public void setNativeConnectionNecessaryForNativePreparedStatements(boolean nativeConnectionNecessary) {
		this.nativeConnectionNecessaryForNativePreparedStatements = nativeConnectionNecessary;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return nativeConnectionNecessaryForNativePreparedStatements;
	}

	/**
	 * Set whether it is necessary to work on the native Connection to
	 * receive native CallableStatements. Default is false.
	 * <p>This makes sense if you need to work with native CallableStatements from
	 * a pool that does not allow to extract the native JDBC objects from its
	 * wrappers but returns the native Connection on Statement.getConnection.
	 */
	public void setNativeConnectionNecessaryForNativeCallableStatements(boolean nativeConnectionNecessary) {
		this.nativeConnectionNecessaryForNativeCallableStatements = nativeConnectionNecessary;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return nativeConnectionNecessaryForNativeCallableStatements;
	}


	public Connection getNativeConnection(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		try {
			// assuming a non-wrapped Statement:
			// getConnection() will return the native Connection
			return stmt.getConnection();
		}
		finally {
			JdbcUtils.closeStatement(stmt);
		}
	}

	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		if (this.nativeConnectionNecessaryForNativePreparedStatements && stmt instanceof PreparedStatement) {
			// PreparedStatements are wrapped:
			// retrieve the native Connection via a Statement
			return getNativeConnection(stmt.getConnection());
		}
		if (this.nativeConnectionNecessaryForNativeCallableStatements && stmt instanceof CallableStatement) {
			// CallableStatements are wrapped:
			// retrieve the native Connection via a Statement
			return getNativeConnection(stmt.getConnection());
		}
		return stmt.getConnection();
	}

	public Statement getNativeStatement(Statement stmt) {
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) {
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) {
		return cs;
	}

	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
