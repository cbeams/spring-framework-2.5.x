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

package org.springframework.jdbc.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Generic proxy for a target DataSource, wrapping Connections, Statements
 * and ResultSets with proxies that delegate to <code>handleConnectionInvocation</code>,
 * <code>handleStatementInvocation</code> and <code>handleResultSetInvocation</code>,
 * respectively.
 *
 * <p>Needs to be subclasses for adding special behavior for specific JDBC operations,
 * for example ResultSet's <code>getString</code> methods. Allows to apply special
 * behavior with specific handle callbacks, without having to implement custom proxies
 * for the entire chain of JDBC resource objects.
 *
 * <p>See StringTrimmerDataSourceProxy for an example of a concrete proxy that
 * trims all String results.
 *
 * @author Juergen Hoeller
 * @since 29.01.2005
 */
public class GenericDataSourceProxy extends DelegatingDataSource {

	/**
	 * Return a generic Connection handle, delegating to the target
	 * DataSource but allowing to intercept JDBC method calls.
	 * <p>The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * @return a transactional Connection if any, a new one else
	 * @see #getConnectionProxy
	 * @see ConnectionProxy#getTargetConnection
	 */
	public Connection getConnection() throws SQLException {
		Connection targetConnection = getTargetDataSource().getConnection();
		return getConnectionProxy(targetConnection);
	}

	/**
	 * Return a generic Connection handle, delegating to the target
	 * DataSource but allowing to intercept JDBC method calls.
	 * <p>The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * @return a transactional Connection if any, a new one else
	 * @see #getConnectionProxy
	 * @see ConnectionProxy#getTargetConnection
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		Connection targetConnection = getTargetDataSource().getConnection(username, password);
		return getConnectionProxy(targetConnection);
	}


	/**
	 * Return a generic Connection handle, delegating to the
	 * <code>handleConnectionInvocation</code> method.
	 * <p>The returned Connection handle implements the ConnectionProxy interface,
	 * allowing to retrieve the underlying target Connection.
	 * @param target the target Connection
	 * @return the wrapped Connection
	 * @see #handleConnectionInvocation
	 * @see ConnectionProxy#getTargetConnection
	 */
	protected Connection getConnectionProxy(Connection target) throws SQLException {
		return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] {ConnectionProxy.class}, new GenericConnectionInvocationHandler(target));
	}

	/**
	 * Return a generic Statement handle, delegating to the
	 * <code>handleStatementInvocation</code> method.
	 * Handles Statement, PreparedStatement and CallableStatement.
	 * @param target the target Statement
	 * @return the wrapped Statement
	 * @see #handleStatementInvocation
	 */
	protected Statement getStatementProxy(Statement target) throws SQLException {
		Class statementInterface = Statement.class;
		if (target instanceof CallableStatement) {
			statementInterface = CallableStatement.class;
		}
		else if (target instanceof PreparedStatement) {
			statementInterface = PreparedStatement.class;
		}
		return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] {statementInterface}, new GenericStatementInvocationHandler(target));
	}

	/**
	 * Return a generic ResultSet handle, delegating to the
	 * <code>handleResultSetInvocation</code> method.
	 * @param target the target ResultSet
	 * @return the wrapped ResultSet
	 * @see #handleResultSetInvocation
	 */
	protected ResultSet getResultSetProxy(ResultSet target) throws SQLException {
		return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class[] {ResultSet.class}, new GenericResultSetInvocationHandler(target));
	}


	/**
	 * Handle an invocation of the given method on the given Connection.
	 * <p>Default implementation delegates all calls to the corresponding
	 * target method. Returned Statements will be wrapped with a proxy,
	 * allowing to intercept method calls on Statements.
	 * <p>Can be overridden in subclasses, applying special behavior
	 * for specific Connection invocations.
	 * @param target the target Connection to invoke on
	 * @param method the method to invoke
	 * @param args the arguments for the method
	 * @return the return value of the invocation
	 * @throws Throwable if thrown by the target method
	 * @see #getStatementProxy
	 */
	protected Object handleConnectionInvocation(Connection target, Method method, Object[] args) throws Throwable {
		try {
			Object retVal = method.invoke(target, args);
			if (retVal instanceof Statement) {
				return getStatementProxy((Statement) retVal);
			}
			return retVal;
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	/**
	 * Handle an invocation of the given method on the given Statement.
	 * <p>Default implementation delegates all calls to the corresponding
	 * target method. Returned ResultSets will be wrapped with a proxy,
	 * allowing to intercept method calls on ResultSets.
	 * <p>Can be overridden in subclasses, applying special behavior
	 * for specific Statement invocations.
	 * @param target the target Statement to invoke on
	 * @param method the method to invoke
	 * @param args the arguments for the method
	 * @return the return value of the invocation
	 * @throws Throwable if thrown by the target method
	 * @see #getResultSetProxy
	 */
	protected Object handleStatementInvocation(Statement target, Method method, Object[] args) throws Throwable {
		try {
			Object retVal = method.invoke(target, args);
			if (retVal instanceof ResultSet) {
				return getResultSetProxy((ResultSet) retVal);
			}
			return retVal;
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

	/**
	 * Handle an invocation of the given method on the given ResultSet.
	 * <p>Default implementation delegates all calls to the corresponding
	 * target method.
	 * <p>Can be overridden in subclasses, applying special behavior
	 * for specific Statement invocations.
	 * @param target the target ResultSet to invoke on
	 * @param method the method to invoke
	 * @param args the arguments for the method
	 * @return the return value of the invocation
	 * @throws Throwable if thrown by the target method
	 * @see #getResultSetProxy
	 */
	protected Object handleResultSetInvocation(ResultSet target, Method method, Object[] args) throws Throwable {
		try {
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	/**
	 * Generic invocation handler for Connections,
	 * delegating to <code>handleConnectionInvocation</code> method.
	 * <p>Returns the target Connection on ConnectionProxy's
	 * <code>getTargetConnection</code>.
	 */
	private class GenericConnectionInvocationHandler implements InvocationHandler {

		private final Connection target;

		public GenericConnectionInvocationHandler(Connection target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			// Handle getTargetConnection method: return underlying connection.
			if (method.getName().equals("getTargetConnection")) {
				return this.target;
			}

			return handleConnectionInvocation(this.target, method, args);
		}
	}


	/**
	 * Generic invocation handler for Statements,
	 * delegating to <code>handleStatementInvocation</code> method.
	 */
	private class GenericStatementInvocationHandler implements InvocationHandler {

		private final Statement target;

		public GenericStatementInvocationHandler(Statement target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return handleStatementInvocation(this.target, method, args);
		}
	}


	/**
	 * Generic invocation handler for ResultSets,
	 * delegating to <code>handleResultSetInvocation</code> method.
	 */
	private class GenericResultSetInvocationHandler implements InvocationHandler {

		private final ResultSet target;

		public GenericResultSetInvocationHandler(ResultSet target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return handleResultSetInvocation(this.target, method, args);
		}
	}

}
