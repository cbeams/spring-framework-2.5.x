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

import org.springframework.jdbc.datasource.ConnectionProxy;

/**
 * Abstract adapter class for the NativeJdbcExtractor interface,
 * for simplified implementation of basic extractors.
 * Returns the passed-in JDBC objects on all methods.
 *
 * <p><code>getNativeConnection</code> checks for a ConnectionProxy chain,
 * for example from a TransactionAwareDataSourceProxy, before delegating to
 * <code>doGetNativeConnection</code> for actual unwrapping. You can override
 * either of the two for a specific connection pool, but the latter is
 * recommended to participate in ConnectionProxy unwrapping.
 *
 * <p>The <code>getNativeConnectionFromStatement</code> method is implemented
 * to simply delegate to <code>getNativeConnection</code> with the Statement's
 * Connection. This is what most extractor implementations will stick to,
 * unless there's a more efficient version for a specific pool.
 *
 * @author Juergen Hoeller
 * @since 02.06.2004
 * @see #getNativeConnection
 * @see #getNativeConnectionFromStatement
 */
public abstract class NativeJdbcExtractorAdapter implements NativeJdbcExtractor {

	/**
	 * Return false by default.
	 */
	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return false;
	}

	/**
	 * Return false by default.
	 */
	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return false;
	}

	/**
	 * Return false by default.
	 */
	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return false;
	}

	/**
	 * Check for a ConnectionProxy chain, then delegate to doGetNativeConnection.
	 * <p>ConnectionProxy is used by Spring's TransactionAwareDataSourceProxy
	 * and SingleConnectionDataSource. The target connection behind it is typically
	 * one from a local connection pool, to be unwrapped by the doGetNativeConnection
	 * implementation of a concrete subclass.
	 * @see #doGetNativeConnection
	 * @see org.springframework.jdbc.datasource.ConnectionProxy
	 * @see org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
	 * @see org.springframework.jdbc.datasource.SingleConnectionDataSource
	 */
	public Connection getNativeConnection(Connection con) throws SQLException {
		Connection conToUse = con;
		while (conToUse instanceof ConnectionProxy) {
			conToUse = ((ConnectionProxy) conToUse).getTargetConnection();
		}
		return doGetNativeConnection(conToUse);
	}

	/**
	 * Not able to unwrap: return passed-in Connection.
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		return con;
	}

	/**
	 * Retrieve the Connection via the Statement's Connection.
	 * @see #getNativeConnection
	 * @see Statement#getConnection
	 */
	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return getNativeConnection(stmt.getConnection());
	}

	/**
	 * Not able to unwrap: return passed-in Statement.
	 */
	public Statement getNativeStatement(Statement stmt) throws SQLException {
		return stmt;
	}

	/**
	 * Not able to unwrap: return passed-in PreparedStatement.
	 */
	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		return ps;
	}

	/**
	 * Not able to unwrap: return passed-in CallableStatement.
	 */
	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		return cs;
	}

	/**
	 * Not able to unwrap: return passed-in ResultSet.
	 */
	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		return rs;
	}

}
