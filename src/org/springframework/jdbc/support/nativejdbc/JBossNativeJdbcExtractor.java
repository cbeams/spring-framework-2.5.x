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

import org.jboss.resource.adapter.jdbc.WrappedCallableStatement;
import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.resource.adapter.jdbc.WrappedPreparedStatement;
import org.jboss.resource.adapter.jdbc.WrappedStatement;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Implementation of the NativeJdbcExtractor interface for the JBoss 3.2
 * connection pool. Returns the underlying native Connection, Statement,
 * etc to application code instead of JBoss' wrapper implementations. (Note
 * that JBoss started wrapping ResultSets as of v3.2.4, which is supported
 * by this implementation.) The returned JDBC classes can then safely be
 * cast, e.g. to OracleConnection.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * a JBoss connection pool: If a given object is not a JBoss wrapper,
 * it will be returned as-is.
 *
 * @author Juergen Hoeller
 * @since 03.01.2004
 */
public class JBossNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String ERROR_OBTAINING_RESULTSET_FROM_WRAPPER = "Error obtaining ResultSet from JBoss WrappedResultSet";
	
	public Connection getNativeConnection(Connection con) throws SQLException {
		if (con instanceof WrappedConnection) {
			return ((WrappedConnection) con).getUnderlyingConnection();
		}
		return con;
	}

	public Statement getNativeStatement(Statement stmt) throws SQLException {
		if (stmt instanceof WrappedStatement) {
			return ((WrappedStatement) stmt).getUnderlyingStatement();
		}
		return stmt;
	}

	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		if (ps instanceof WrappedPreparedStatement) {
			return (PreparedStatement) ((WrappedPreparedStatement) ps).getUnderlyingStatement();
		}
		return ps;
	}

	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		if (cs instanceof WrappedCallableStatement) {
			return (CallableStatement) ((WrappedCallableStatement) cs).getUnderlyingStatement();
		}
		return cs;
	}
	
//	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
//		if (rs instanceof WrappedResultSet) {
//			return ((WrappedResultSet)rs).getUnderlyingResultSet();
//		}
//		return rs;
//	}
	
	/**
	 * We access WrappedResultSet via reflection, since this class only appeared in JBoss 3.2.4
	 * and we want to ideal work with at least 3.2.2+
	 */
	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		
		if (rs.getClass().getName().equals("org.jboss.resource.adapter.jdbc.WrappedResultSet")) {
			Method m;
			try {
				m = rs.getClass().getMethod("getUnderlyingResultSet", null);
			}
			catch (NoSuchMethodException e) {
				return rs;
			}
			try {
				ResultSet realRs = (ResultSet) m.invoke(rs, null);
				return realRs;
			}
			catch (InvocationTargetException e) {
				throw new DataAccessResourceFailureException(ERROR_OBTAINING_RESULTSET_FROM_WRAPPER, e.getTargetException());
			}
			catch (Exception e) {
				// should never get here in real use
				throw new RuntimeException(ERROR_OBTAINING_RESULTSET_FROM_WRAPPER, e);
			}
		}
		return rs;
	}
	
}
