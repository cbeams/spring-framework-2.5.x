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

import org.springframework.dao.DataAccessResourceFailureException;

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
 * <p>Tested against Commons DBCP 1.1 and 1.2, but should also work with 1.0.
 * Before Commons DBCP 1.1, DelegatingCallableStatement and DelegatingResultSet
 * have not offered any means to access underlying delegates; consequently,
 * getNativeCallableStatement and getNativeResultSet will not work with 1.0.
 *
 * @author Juergen Hoeller
 * @since 25.08.2003
 */
public class CommonsDbcpNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String GET_INNERMOST_DELEGATE_METHOD_NAME = "getInnermostDelegate";

	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		if (con instanceof DelegatingConnection) {
			Connection nativeCon = ((DelegatingConnection) con).getInnermostDelegate();
			// For some reason, the innermost delegate can be null: not for a
			// Statement's Connection but for the Connection handle returned by the pool.
			// We'll fall back to the MetaData's Connection in this case, which is
			// a native unwrapped Connection with Commons DBCP 1.1 and 1.2.
			return (nativeCon != null ? nativeCon : con.getMetaData().getConnection());
		}
		return con;
	}

	public Statement getNativeStatement(Statement stmt) throws SQLException {
		if (stmt instanceof DelegatingStatement) {
			return ((DelegatingStatement) stmt).getInnermostDelegate();
		}
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		if (ps instanceof DelegatingPreparedStatement) {
			// We need to use reflection here, as the "getInnermostDelegate"
			// method signature varies between Commons DBCP 1.1 and 1.2.
			try {
				Method getInnermostDelegate = ps.getClass().getMethod(GET_INNERMOST_DELEGATE_METHOD_NAME, null);
				return (PreparedStatement) getInnermostDelegate.invoke(ps, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not retrieve innermost delegate", ex);
			}
		}
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		if (cs instanceof DelegatingCallableStatement) {
			// We need to use reflection here, as the "getInnermostDelegate"
			// method signature varies between Commons DBCP 1.1 and 1.2.
			try {
				Method getInnermostDelegate = cs.getClass().getMethod(GET_INNERMOST_DELEGATE_METHOD_NAME, null);
				return (CallableStatement) getInnermostDelegate.invoke(cs, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not retrieve innermost delegate", ex);
			}
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
