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

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.spi.LocalTransactionException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation that performs local transactions
 * on a single CCI ConnectionFactory. Binds a CCI connection from the specified
 * ConnectionFactory to the thread, potentially allowing for one thread
 * Connection per ConnectionFactory.
 *
 * <p>Application code is required to retrieve the CCI Connection via
 * <code>ConnectionFactoryUtils.getConnection(ConnectionFactory)</code>
 * instead of J2EE's standard <code>ConnectionFactory.getConnection()</code>.
 * This is recommended anyway, as it throws unchecked <code>org.springframework.dao</code>
 * exceptions instead of the checked JCA ResourceException. All framework classes
 * like CciTemplate or MappingRecordQuery use this strategy implicitly. If not used
 * with this transaction manager, the lookup strategy behaves exactly like the
 * common one - it can thus be used in any case.
 *
 * <p>Alternatively, you can also allow application code to work with the standard
 * J2EE lookup pattern <code>ConnectionFactory.getConnection()</code>, for example
 * for legacy code that is not aware of Spring at all. In that case, define a
 * TransactionAwareConnectionFactoryProxy for your target ConnectionFactory, and pass
 * that proxy ConnectionFactory to your DAOs, which will automatically participate
 * in Spring-managed transactions through it. Note that CciLocalTransactionManager
 * still needs to be wired with the target ConnectionFactory, driving transactions for it.
 *
 * @author Thierry Templier
 * @author Juergen Hoeller
 * @since 1.2
 * @see ConnectionFactoryUtils#getConnection(javax.resource.cci.ConnectionFactory)
 * @see ConnectionFactoryUtils#releaseConnection
 * @see TransactionAwareConnectionFactoryProxy
 * @see org.springframework.jca.cci.core.CciTemplate
 * @see org.springframework.jca.cci.object.MappingRecordOperation
 */
public class CciLocalTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private ConnectionFactory connectionFactory;


	/**
	 * Create a new CciLocalTransactionManager instance.
	 * A ConnectionFactory has to be set to be able to use it.
	 * @see #setConnectionFactory
	 */
	public CciLocalTransactionManager() {
	}

	/**
	 * Create a new CciLocalTransactionManager instance.
	 * @param connectionFactory CCI ConnectionFactory to manage local transactions for
	 */
	public CciLocalTransactionManager(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		afterPropertiesSet();
	}

	/**
	 * Set the CCI ConnectionFactory that this instance should manage local
	 * transactions for.
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * Return the CCI ConnectionFactory that this instance manages local
	 * transactions for.
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
		CciLocalTransactionObject txObject = new CciLocalTransactionObject();
		ConnectionHolder conHolder =
		    (ConnectionHolder) TransactionSynchronizationManager.getResource(getConnectionFactory());
		txObject.setConnectionHolder(conHolder);
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		// Consider a pre-bound connection as transaction.
		return (txObject.getConnectionHolder() != null);
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;

		Connection con = null;

		try {
			con = getConnectionFactory().getConnection();
			if (logger.isDebugEnabled()) {
				logger.debug("Acquired Connection [" + con + "] for local CCI transaction");
			}

			txObject.setConnectionHolder(new ConnectionHolder(con));
			txObject.getConnectionHolder().setSynchronizedWithTransaction(true);

			con.getLocalTransaction().begin();
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(definition.getTimeout());
			}
			TransactionSynchronizationManager.bindResource(getConnectionFactory(), txObject.getConnectionHolder());
		}

		catch (NotSupportedException ex) {
			ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());
			throw new CannotCreateTransactionException("CCI Connection does not support local transactions", ex);
		}
		catch (LocalTransactionException ex) {
			ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());
			throw new CannotCreateTransactionException("Could not begin local CCI transaction", ex);
		}
		catch (ResourceException ex) {
			ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());
			throw new TransactionSystemException("Unexpected failure on begin of CCI local transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		txObject.setConnectionHolder(null);
		return TransactionSynchronizationManager.unbindResource(getConnectionFactory());
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		ConnectionHolder conHolder = (ConnectionHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(getConnectionFactory(), conHolder);
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;
		return txObject.getConnectionHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing CCI local transaction on Connection [" + con + "]");
		}
		try {
			con.getLocalTransaction().commit();
		}
		catch (LocalTransactionException ex) {
			throw new TransactionSystemException("Could not commit CCI local transaction", ex);
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Unexpected failure on commit of CCI local transaction", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back CCI local transaction on Connection [" + con + "]");
		}
		try {
			con.getLocalTransaction().rollback();
		}
		catch (LocalTransactionException ex) {
			throw new TransactionSystemException("Could not roll back CCI local transaction", ex);
		}
		catch (ResourceException ex) {
			throw new TransactionSystemException("Unexpected failure on rollback of CCI local transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting CCI local transaction [" + txObject.getConnectionHolder().getConnection() +
					"] rollback-only");
		}
		txObject.getConnectionHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		CciLocalTransactionObject txObject = (CciLocalTransactionObject) transaction;

		// Remove the connection holder from the thread.
		TransactionSynchronizationManager.unbindResource(getConnectionFactory());
		txObject.getConnectionHolder().clear();

		Connection con = txObject.getConnectionHolder().getConnection();
		if (logger.isDebugEnabled()) {
			logger.debug("Releasing CCI Connection [" + con + "] after transaction");
		}
		ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());
	}


	/**
	 * CCI local transaction object, representing a ConnectionHolder.
	 * Used as transaction object by CciLocalTransactionManager.
	 * @see ConnectionHolder
	 */
	private static class CciLocalTransactionObject {

		private ConnectionHolder connectionHolder;

		public void setConnectionHolder(ConnectionHolder connectionHolder) {
			this.connectionHolder = connectionHolder;
		}

		public ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
