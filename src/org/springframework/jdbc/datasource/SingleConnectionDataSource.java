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

	/** wrapped connection */
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
	public SingleConnectionDataSource(String driverClassName, String url, String username, String password,
	                                  boolean suppressClose) throws CannotGetJdbcConnectionException {
		super(driverClassName, url, username, password);
		this.suppressClose = suppressClose;
	}

	/**
	 * Create a new SingleConnectionDataSource with a given connection.
	 * @param source underlying source connection
	 * @param suppressClose if the connection should be wrapped with a* connection that
	 * suppresses close() calls (to allow for normal close() usage in applications that
	 * expect a pooled connection but do not know our SmartDataSource interface).
	 */
	public SingleConnectionDataSource(Connection source, boolean suppressClose) {
		if (source == null) {
			throw new IllegalArgumentException("Connection is null in SingleConnectionDataSource");
		}
		this.suppressClose = suppressClose;
		init(source);
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
	public boolean shouldClose(Connection conn) {
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
	 * @param source the JDBC Connection to use
	 */
	protected void init(Connection source) {
		this.connection = this.suppressClose ? DataSourceUtils.getCloseSuppressingConnectionProxy(source) : source;
	}

	public Connection getConnection() throws SQLException {
		synchronized (this) {
			if (this.connection == null) {
				// no underlying connection -> lazy init via DriverManager
				init();
			}
		}
		if (this.connection.isClosed()) {
			throw new SQLException("Connection was closed in SingleConnectionDataSource. " +
			                       "Check that user code checks shouldClose() before closing connections, " +
			                       "or set suppressClose to true");
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
	 * Close the underlying connection.
	 * The provider of this DataSource needs to care for proper shutdown.
	 * <p>As this bean implements DisposableBean, a bean factory will
	 * automatically invoke this on destruction of its cached singletons.
	 */
	public void destroy() throws SQLException {
		if (this.connection != null) {
			this.connection.close();
		}
	}

}
