/*
 * Copyright 2002-2005 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.jca.cci.CannotGetCciConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
 
/**
 * Helper class that provides static methods to obtain CCI Connections from
 * a ConnectionFactory, and to close Connections if necessary. Has special support
 * for Spring-managed connections, e.g. for use with CciLocalTransactionManager.
 *
 * <p>Used internally by CciTemplate, CCI operation objects and the
 * CciLocalTransactionManager. Can also be used directly in application code.
 *
 * @author Thierry Templier
 * @author Juergen Hoeller
 * @since 1.2
 * @see #getConnection
 * @see #releaseConnection
 * @see CciLocalTransactionManager
 * @see org.springframework.jca.cci.core.CciTemplate
 * @see org.springframework.jca.cci.object.MappingRecordOperation
 */
public abstract class ConnectionFactoryUtils {

	private static final Log logger = LogFactory.getLog(ConnectionFactoryUtils.class);

	/**
	 * Get a Connection from the given DataSource. Changes any CCI exception into
	 * the Spring hierarchy of unchecked generic data access exceptions, simplifying
	 * calling code and making any exception that is thrown more meaningful.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using CciLocalTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * @param cf ConnectionFactory to get Connection from
	 * @return a CCI Connection from the given ConnectionFactory
	 * @throws org.springframework.jca.cci.CannotGetCciConnectionException
	 * if the attempt to get a Connection failed
	 * @see org.springframework.transaction.support.TransactionSynchronizationManager
	 * @see org.springframework.jca.cci.connection.CciLocalTransactionManager
	 */
	public static Connection getConnection(ConnectionFactory cf) throws CannotGetCciConnectionException {
		try {
			return doGetConnection(cf);
		}
		catch (ResourceException ex) {
			throw new CannotGetCciConnectionException("Could not get CCI connection", ex);
		}
	}

	/**
	 * Actually get a CCI Connection for the given ConnectionFactory.
	 * Same as <code>getConnection</code>, but throwing the original ResourceException.
	 * <p>Is aware of a corresponding Connection bound to the current thread, for example
	 * when using DataSourceTransactionManager. Will bind a Connection to the thread
	 * if transaction synchronization is active (e.g. if in a JTA transaction).
	 * <p>Directly accessed by TransactionAwareDataSourceProxy.
	 * @param cf ConnectionFactory to get Connection from
	 * @return a CCI Connection from the given ConnectionFactory
	 * @throws ResourceException if thrown by CCI API methods
	 * @see #getConnection(ConnectionFactory)
	 * @see TransactionAwareConnectionFactoryProxy
	 */
	public static Connection doGetConnection(ConnectionFactory cf) throws ResourceException {
		Assert.notNull(cf, "No ConnectionFactory specified");

		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(cf);
		if (conHolder != null) {
			return conHolder.getConnection();
		}

		logger.debug("Opening CCI Connection");
		Connection con = cf.getConnection();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for CCI Connection");
			conHolder = new ConnectionHolder(con);
			conHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.registerSynchronization(new ConnectionSynchronization(conHolder, cf));
			TransactionSynchronizationManager.bindResource(cf, conHolder);
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
	public static void releaseConnection(Connection con, ConnectionFactory cf) {
		try {
			doReleaseConnection(con, cf);
		}
		catch (ResourceException ex) {
			logger.error("Could not close CCI Connection", ex);
		}
	}

	/**
	 * Actually close a JCA CCI Connection for the given DataSource.
	 * Same as <code>releaseConnection</code>, but throwing the original ResourceException.
	 * <p>Directly accessed by TransactionAwareConnectionFactoryProxy.
	 * @throws ResourceException if thrown by JCA CCI methods
	 * @see #releaseConnection
	 */
	public static void doReleaseConnection(Connection con, ConnectionFactory cf) throws ResourceException {
		if (con == null || TransactionSynchronizationManager.hasResource(cf)) {
			return;
		}
		con.close();
	}


	/**
	 * Callback for resource cleanup at the end of a non-native CCI transaction
	 * (e.g. when participating in a JTA transaction).
	 */
	private static class ConnectionSynchronization extends TransactionSynchronizationAdapter {

		private final ConnectionHolder connectionHolder;

		private final ConnectionFactory connectionFactory;

		private boolean holderActive = true;

		public ConnectionSynchronization(ConnectionHolder connectionHolder, ConnectionFactory connectionFactory) {
			this.connectionHolder = connectionHolder;
			this.connectionFactory = connectionFactory;
		}

		public void suspend() {
			if (this.holderActive) {
				TransactionSynchronizationManager.unbindResource(this.connectionFactory);
			}
		}

		public void resume() {
			if (this.holderActive) {
				TransactionSynchronizationManager.bindResource(this.connectionFactory, this.connectionHolder);
			}
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.connectionFactory);
			this.holderActive = false;
			releaseConnection(this.connectionHolder.getConnection(), this.connectionFactory);
		}
	}

}
