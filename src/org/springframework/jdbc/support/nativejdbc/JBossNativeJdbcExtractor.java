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

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Implementation of the NativeJdbcExtractor interface for the JBoss 3.2
 * connection pool. Returns the underlying native Connection, Statement,
 * etc to application code instead of JBoss' wrapper implementations. The
 * returned JDBC classes can then safely be cast, e.g. to OracleConnection.
 *
 * <p>Note that JBoss started wrapping ResultSets as of 3.2.4, which is
 * supported by this implementation, while still being compatible with 3.2.x
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * a JBoss connection pool: If a given object is not a JBoss wrapper,
 * it will be returned as-is.
 *
 * @author Juergen Hoeller
 * @since 03.01.2004
 * @see org.jboss.resource.adapter.jdbc.WrappedConnection#getUnderlyingConnection
 * @see org.jboss.resource.adapter.jdbc.WrappedStatement#getUnderlyingStatement
 * @see org.jboss.resource.adapter.jdbc.WrappedResultSet#getUnderlyingResultSet
 */
public class JBossNativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private static final String WRAPPED_CONNECTION_NAME = "org.jboss.resource.adapter.jdbc.WrappedConnection";

	private static final String WRAPPED_STATEMENT_NAME = "org.jboss.resource.adapter.jdbc.WrappedStatement";

	private static final String WRAPPED_RESULT_SET_NAME = "org.jboss.resource.adapter.jdbc.WrappedResultSet";

	private Class wrappedConnectionClass;

	private Class wrappedStatementClass;

	private Method getUnderlyingConnectionMethod;

	private Method getUnderlyingStatementMethod;

	/**
	 * This constructor retrieves JBoss JDBC wrapper classes,
	 * so we can get the underlying vendor connection using reflection.
	 */
	public JBossNativeJdbcExtractor() {
		try {
			this.wrappedConnectionClass = getClass().getClassLoader().loadClass(WRAPPED_CONNECTION_NAME);
			this.wrappedStatementClass = getClass().getClassLoader().loadClass(WRAPPED_STATEMENT_NAME);
			this.getUnderlyingConnectionMethod = this.wrappedConnectionClass.getMethod("getUnderlyingConnection", null);
			this.getUnderlyingStatementMethod = this.wrappedStatementClass.getMethod("getUnderlyingStatement", null);
		}
		catch (Exception ex) {
			throw new InvalidDataAccessApiUsageException(
					"Couldn't initialize JBossNativeJdbcExtractor because JBoss API classes are not available", ex);
		}
	}

	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingConnection</code> method.
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		if (this.wrappedConnectionClass.isAssignableFrom(con.getClass())) {
			try {
				return (Connection) this.getUnderlyingConnectionMethod.invoke(con, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke JBoss' getUnderlyingConnection method", ex);
			}
		}
		return con;
	}

	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingStatement</code> method.
	 */
	public Statement getNativeStatement(Statement stmt) throws SQLException {
		if (this.wrappedStatementClass.isAssignableFrom(stmt.getClass())) {
			try {
				return (Statement) this.getUnderlyingStatementMethod.invoke(stmt, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke JBoss' getUnderlyingStatement method", ex);
			}
		}
		return stmt;
	}

	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingStatement</code> method.
	 */
	public PreparedStatement getNativePreparedStatement(PreparedStatement ps) throws SQLException {
		return (PreparedStatement) getNativeStatement(ps);
	}

	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingStatement</code> method.
	 */
	public CallableStatement getNativeCallableStatement(CallableStatement cs) throws SQLException {
		return (CallableStatement) getNativeStatement(cs);
	}
	
	/**
	 * Retrieve the Connection via JBoss' <code>getUnderlyingResultSet</code> method.
	 * <p>We access WrappedResultSet via direct reflection, since this class only
	 * appeared in JBoss 3.2.4 and we want to stay compatible with at least 3.2.2+.
	 */
	public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
		if (rs.getClass().getName().equals(WRAPPED_RESULT_SET_NAME)) {
			try {
				Method getUnderlyingResultSetMethod = rs.getClass().getMethod("getUnderlyingResultSet", null);
				return (ResultSet) getUnderlyingResultSetMethod.invoke(rs, null);
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not invoke JBoss' getUnderlyingResultSet method", ex);
			}
		}
		return rs;
	}
	
}
