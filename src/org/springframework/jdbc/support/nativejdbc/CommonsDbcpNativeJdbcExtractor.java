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

import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingResultSet;
import org.apache.commons.dbcp.DelegatingStatement;

/**
 * Implementation of the NativeJdbcExtractor interface for the Jakarta Commons
 * DBCP connection pool. Returns the underlying native Connection, Statement,
 * ResultSet etc to application code instead of DBCP's wrapper implementations.
 * The returned JDBC classes can then safely be cast, e.g. to OracleResultSet.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with a
 * Commons DBCP DataSource: If a given object is not a Commons DBCP wrapper,
 * it will be returned as-is.
 *
 * <p>Note: Before Commons DBCP 1.1, DelegatingCallableStatement and
 * DelegatingResultSet have not offered any means to access underlying delegates.
 * Therefore, getNativeCallableStatement and getNativeResultSet will just work
 * with DBCP 1.1. But getNativeResultSet will not be invoked by JdbcTemplate for a
 * wrapped ResultSet anyway, because getNativeStatement/getNativePreparedStatement
 * will already have returned the underlying delegate before.
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 * @see org.springframework.jdbc.core.JdbcTemplate#setNativeJdbcExtractor
 */
public class CommonsDbcpNativeJdbcExtractor implements NativeJdbcExtractor {

	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return false;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return false;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return false;
	}

	public Connection getNativeConnection(Connection con) {
		if (con instanceof DelegatingConnection) {
			return ((DelegatingConnection) con).getInnermostDelegate();
		}
		return con;
	}

	public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
		return getNativeConnection(stmt.getConnection());
	}

	public Statement getNativeStatement(Statement stmt) {
		if (stmt instanceof DelegatingStatement) {
			return ((DelegatingStatement) stmt).getInnermostDelegate();
		}
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) {
		if (ps instanceof DelegatingPreparedStatement) {
			return ((DelegatingPreparedStatement) ps).getInnermostDelegate();
		}
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) {
		if (cs instanceof DelegatingCallableStatement) {
			return ((DelegatingCallableStatement) cs).getInnermostDelegate();
		}
		return cs;
	}

	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		if (rs instanceof DelegatingResultSet) {
			return ((DelegatingResultSet) rs).getInnermostDelegate();
		}
		return rs;
	}

}
