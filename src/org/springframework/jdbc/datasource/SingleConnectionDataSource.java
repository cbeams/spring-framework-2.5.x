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
import java.sql.Connection;
import java.sql.SQLException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.util.ObjectUtils;

/**
 * Implementation of SmartDataSource that wraps a single connection which is not
 * closed after use. Obviously, this is not multi-threading capable.
 *
 * <p>Note that at shutdown, someone should close the underlying connection via the
 * close() method. Client code will never call close on the connection handle if it
 * is SmartDataSource-aware (e.g. uses DataSourceUtils.closeConnectionIfNecessary).
 *
 * <p>If client code will call close in the assumption of a pooled connection,
 * like when using persistence tools, set suppressClose to true. This will return a
 * close-suppressing proxy instead of the physical connection. Be aware that you will
 * not be able to cast this to a native OracleConnection or the like anymore.
 *
 * <p>This is primarily a test class. For example, it enables easy testing of code
 * outside an application server, in conjunction with a simple JNDI environment.
 * In contrast to DriverManagerDataSource, it reuses the same connection all the time,
 * avoiding excessive creation of physical connections.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DataSourceUtils#closeConnectionIfNecessary
 */
public class SingleConnectionDataSource extends DriverManagerDataSource implements DisposableBean {

	private boolean suppressClose;

	/** Wrapped connection */
	private Connection target;

	/** Proxy connection */
	private Connection connection;


	/**
	 * Constructor for bean-style configuration.
	 */
	public SingleConnectionDataSource() {
	}

	/**
	 * Create a new SingleConnectionDataSource with the given standard
	 * DriverManager parameters.
	 * @param suppressClose if the returned connection should be a
	 * close-suppressing proxy or the physical connection.
	 */
	public SingleConnectionDataSource(
			String driverClassName, String url, String username, String password, boolean suppressClose)
			throws CannotGetJdbcConnectionException {
		super(driverClassName, url, username, password);
		this.suppressClose = suppressClose;
	}

	/**
	 * Create a new SingleConnectionDataSource with a given connection.
	 * @param target underlying target connection
	 * @param suppressClose if the connection should be wrapped with a* connection that
	 * suppresses close() calls (to allow for normal close() usage in applications that
	 * expect a pooled connection but do not know our SmartDataSource interface).
	 */
	public SingleConnectionDataSource(Connection target, boolean suppressClose) {
		if (target == null) {
			throw new IllegalArgumentException("Connection is null in SingleConnectionDataSource");
		}
		this.suppressClose = suppressClose;
		init(target);
	}

	/**
	 * Set if the returned connection should be a close-suppressing proxy
	 * or the physical connection.
	 */
	public void setSuppressClose(boolean suppressClose) {
		this.suppressClose = suppressClose;
	}

	/**
	 * Return if the returned connection will be a close-suppressing proxy
	 * or the physical connection.
	 */
	public boolean isSuppressClose() {
		return suppressClose;
	}


	/**
	 * This is a single connection: Do not close it when returning to the "pool".
	 */
	public boolean shouldClose(Connection con) {
		return false;
	}

	/**
	 * Initialize the underlying connection via DriverManager.
	 */
	protected void init() throws SQLException {
		init(getConnectionFromDriverManager());
	}

	/**
	 * Initialize the underlying connection.
	 * Wraps the connection with a close-suppressing proxy if necessary.
	 * @param target the JDBC Connection to use
	 */
	protected void init(Connection target) {
		this.target = target;
		this.connection = this.suppressClose ? getCloseSuppressingConnectionProxy(target) : target;
	}

	/**
	 * Close the underlying connection.
	 * The provider of this DataSource needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws SQLException {
		if (this.target != null) {
			this.target.close();
		}
	}


	public Connection getConnection() throws SQLException {
		synchronized (this) {
			if (this.connection == null) {
				// no underlying connection -> lazy init via DriverManager
				init();
			}
		}
		if (this.connection.isClosed()) {
			throw new SQLException(
					"Connection was closed in SingleConnectionDataSource. Check that user code checks " +
					"shouldClose() before closing connections, or set suppressClose to true");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Returning single connection: " + this.connection);
		}
		return this.connection;
	}

	/**
	 * Specifying a custom username and password doesn't make sense
	 * with a single connection. Returns the single connection if given
	 * the same username and password, though.
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		if (ObjectUtils.nullSafeEquals(username, getUsername()) &&
				ObjectUtils.nullSafeEquals(password, getPassword())) {
			return getConnection();
		}
		else {
			throw new SQLException("SingleConnectionDataSource does not support custom username and password");
		}
	}

	/**
	 * Wrap the given Connection with a proxy that delegates every method call to it
	 * but suppresses close calls.
	 * @param target the original Connection to wrap
	 * @return the wrapped Connection
	 */
	protected Connection getCloseSuppressingConnectionProxy(Connection target) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class[] {ConnectionProxy.class},
				new CloseSuppressingInvocationHandler(target));
	}


	/**
	 * Invocation handler that suppresses close calls on JDBC Connections.
	 */
	private static class CloseSuppressingInvocationHandler implements InvocationHandler {

		private static final String GET_TARGET_CONNECTION_METHOD_NAME = "getTargetConnection";

		private static final String CONNECTION_CLOSE_METHOD_NAME = "close";

		private final Connection target;

		private CloseSuppressingInvocationHandler(Connection target) {
			this.target = target;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals(GET_TARGET_CONNECTION_METHOD_NAME)) {
				return this.target;
			}
			if (method.getName().equals(CONNECTION_CLOSE_METHOD_NAME)) {
				// don't pass the call on
				return null;
			}
			try {
				return method.invoke(this.target, args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
