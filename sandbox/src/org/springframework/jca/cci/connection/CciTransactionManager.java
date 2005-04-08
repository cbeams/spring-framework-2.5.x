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
import javax.resource.cci.LocalTransaction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionObject;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single CCI ConnectionFactory.
 * Binds a CCI connection from the specified ConnectionFactory to the thread,
 * potentially allowing for one thread connection per data source.
 *
 * <p>Application code is required to retrieve the CCI Connection via
 * <code>DataSourceUtils.getConnection(ConnectionFactory)</code> or
 * <code>DataSourceUtils.getConnection(ConnectionFactory,ConnectionSpec)</code>
 * instead of J2EE's standard <code>ConnectionFactory.getConnection()</code> or
 * <code>ConnectionFactory.getConnection(ConnectionSpec)</code>. This is
 * recommended anyway, as it throws unchecked org.springframework.dao exceptions
 * instead of checked RessourceException.
 * All framework classes like CciTemplate or MappingRecordQuery use this strategy
 * implicitly.
 * If not used with this transaction manager, the lookup strategy behaves exactly
 * like the common one - it can thus be used in any case.
 *
 * @author Thierry TEMPLIER
 */
public class CciTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private ConnectionFactory connectionFactory;


	/**
	 * Create a new CciTransactionManager instance.
	 * A ConnectionFactory has to be set to be able to use it.
	 * @see #setConnectionFactory(ConnectionFactory)
	 */
	public CciTransactionManager() {
		setNestedTransactionAllowed(true);
	}

	/**
	 * Create a new CciTransactionManager instance.
	 * @param connectionFactory CCI ConnectionFactory to manage transactions for
	 */
	public CciTransactionManager(ConnectionFactory connectionFactory) {
		this();
		this.connectionFactory = connectionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the CCI ConnectionFactory that this instance should manage transactions for.
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the CCI ConnectionFactory that this instance manages transactions for.
	 */
	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void afterPropertiesSet() {
		if (this.connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is required");
		}
	}


	protected Object doGetTransaction() {
		CciTransactionObject txObject = new CciTransactionObject();
		ConnectionHolder conHolder =
		    (ConnectionHolder) TransactionSynchronizationManager.getResource(this.connectionFactory);
		txObject.setConnectionHolder(conHolder);
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		CciTransactionObject txObject = (CciTransactionObject) transaction;
		// consider a pre-bound Connection as transaction
		return (txObject.getConnectionHolder() != null);
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		CciTransactionObject txObject = (CciTransactionObject) transaction;

		if (logger.isDebugEnabled()) {
			logger.debug("Opening new connection for CCI transaction");
		}
		Connection con = ConnectionFactoryUtils.getConnection(this.connectionFactory, false);

		txObject.setConnectionHolder(new ConnectionHolder(con));
		txObject.getConnectionHolder().setSynchronizedWithTransaction(true);

		try {
			LocalTransaction localTransaction=con.getLocalTransaction();
			localTransaction.begin();

			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(definition.getTimeout());
			}
			TransactionSynchronizationManager.bindResource(getConnectionFactory(), txObject.getConnectionHolder());
		}

		catch (ResourceException ex) {
			ConnectionFactoryUtils.closeConnectionIfNecessary(con, this.connectionFactory);
			throw new CannotCreateTransactionException("Could not configure CCI connection for transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		CciTransactionObject txObject = (CciTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(this.connectionFactory);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		ConnectionHolder conHolder = (ConnectionHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(this.connectionFactory, conHolder);
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		CciTransactionObject txObject = (CciTransactionObject) transaction;
		return txObject.getConnectionHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		CciTransactionObject txObject = (CciTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing CCI transaction on connection [" + con + "]");
		}
		try {
			LocalTransaction localTransaction=con.getLocalTransaction();
			localTransaction.commit();
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Could not commit CCI transaction", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		CciTransactionObject txObject = (CciTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back JDBC transaction on connection [" + con + "]");
		}
		try {
			LocalTransaction localTransaction=con.getLocalTransaction();
			localTransaction.rollback();
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Could not roll back CCI transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		CciTransactionObject txObject = (CciTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting CCI transaction [" + txObject.getConnectionHolder().getConnection() +
									 "] rollback-only");
		}
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		CciTransactionObject txObject = (CciTransactionObject) transaction;

		// remove the connection holder from the thread
		TransactionSynchronizationManager.unbindResource(this.connectionFactory);
		txObject.getConnectionHolder().clear();

		Connection con = txObject.getConnectionHolder().getConnection();

		if (logger.isDebugEnabled()) {
			logger.debug("Closing CCI connection [" + con + "] after transaction");
		}
		ConnectionFactoryUtils.closeConnectionIfNecessary(con, this.connectionFactory);
	}

}
