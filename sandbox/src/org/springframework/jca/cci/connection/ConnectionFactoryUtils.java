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

package org.springframework.jca.cci.connection;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jca.cci.CannotGetCciConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
 
/**
 * Helper class that provides static methods to obtain connections from
 * JNDI and close connections if necessary. Has support for thread-bound
 * connections, e.g. for use with CciTransactionManager.
 *
 * @author Thierry TEMPLIER
 */
public abstract class ConnectionFactoryUtils {

	private static final Log logger = LogFactory.getLog(ConnectionFactoryUtils.class);

	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param cf ConnectionFactory to get Connection from
	 * @return a JDBC Connection from this DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see CciTransactionManager
	 */
	public static Connection getConnection(ConnectionFactory cf) throws CannotGetCciConnectionException {
		return getConnection(cf, true);
	}

	public static Connection getConnection(ConnectionFactory cf,ConnectionSpec spec)
			throws CannotGetCciConnectionException {
		return getConnection(cf, spec, true);
	}

	public static Connection getConnection(ConnectionFactory cf,boolean allowSynchronization)
			throws CannotGetCciConnectionException {
		return getConnection(cf,null,allowSynchronization);
	}

	/**
	 * Get a Connection from the given DataSource. Changes any SQL exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param cf ConnectionFactory to get Connection from
	 * @param allowSynchronization if a new JDBC Connection is supposed to be
	 * registered with transaction synchronization (if synchronization is active).
	 * This will always be true for typical data access code.
	 * @return a JDBC Connection from this DataSource
	 * @throws org.springframework.jdbc.CannotGetJdbcConnectionException
	 * if the attempt to get a Connection failed
	 * @see #doGetConnection
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see CciTransactionManager
	 */
	public static Connection getConnection(ConnectionFactory cf, ConnectionSpec spec, boolean allowSynchronization)
	    throws CannotGetCciConnectionException {
		try {
			return doGetConnection(cf, spec, allowSynchronization);
		}
		catch (ResourceException ex) {
			throw new CannotGetCciConnectionException("Could not get CCI connection", ex);
		}
	}

	/**
	 * Actually get a JCA CCI Connection for the given DataSource.
	 * Same as getConnection, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @throws ResourceException if thrown by JCA CCI methods
	 */
	protected static Connection doGetConnection(ConnectionFactory cf, ConnectionSpec spec, boolean allowSynchronization)
			throws ResourceException {
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(cf);
		if (conHolder != null) {
			return conHolder.getConnection();
		}
		Connection con = null;
		if( spec!=null ) {
			con = cf.getConnection(spec);
		} else { 
			con = cf.getConnection();
		}
		if (allowSynchronization && TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for CCI connection");
			conHolder = new ConnectionHolder(con);
			TransactionSynchronizationManager.bindResource(cf, conHolder);
			TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(conHolder, cf));
		}
		return con;
	}

	/**
	 * Close the given Connection if necessary, i.e. if it is not bound to the thread
	 * and it is not created by a SmartDataSource returning shouldClose=false.
	 * @param con Connection to close if necessary
	 * (if this is null, the call will be ignored)
	 * @param cf ConnectionFactory that the Connection came from
	 */
	public static void closeConnectionIfNecessary(Connection con, ConnectionFactory cf) {
		try {
			doCloseConnectionIfNecessary(con, cf);
		}
		catch (ResourceException ex) {
			logger.error("Could not close CCI connection", ex);
		}
	}

	/**
	 * Actually close a JCA CCI Connection for the given DataSource.
	 * Same as closeConnectionIfNecessary, but throwing the original SQLException.
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @throws ResourceException if thrown by JCA CCI methods
	 * @see #closeConnectionIfNecessary
	 */
	protected static void doCloseConnectionIfNecessary(Connection con, ConnectionFactory cf) throws ResourceException {
		if (con == null || TransactionSynchronizationManager.hasResource(cf)) {
			return;
		}
		con.close();
	}


	/**
	 * Callback for resource cleanup at the end of a non-native-JDBC transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final ConnectionFactory connectionFactory;

		private ConnectionSynchronization(ConnectionHolder connectionHolder, ConnectionFactory connectionFactory) {
			this.connectionHolder = connectionHolder;
			this.connectionFactory = connectionFactory;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.connectionFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.connectionFactory, this.connectionHolder);
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.connectionFactory);
			closeConnectionIfNecessary(this.connectionHolder.getConnection(), this.connectionFactory);
		}
	}

}
