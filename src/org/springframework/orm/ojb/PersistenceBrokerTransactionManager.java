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

package org.springframework.orm.ojb;

import java.sql.Connection;

import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.TransactionAbortedException;
import org.apache.ojb.broker.accesslayer.LookupException;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single OJB persistence broker key.
 * Binds an OJB PersistenceBroker from the specified key to the thread, potentially
 * allowing for one thread PersistenceBroker per key. OjbFactoryUtils and
 * PersistenceBrokerTemplate are aware of thread-bound persistence brokers and
 * participate in such transactions automatically. Using either is required for
 * OJB access code supporting this transaction management mechanism.
 *
 * <p>This implementation is appropriate for applications that solely use OJB for
 * transactional data access. JTA respectively JtaTransactionManager is necessary
 * for accessing multiple transactional resources, in combination with transactional
 * DataSources as connection pools (to be specified in OJB's configuration).
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see #setJcdAlias
 * @see #setPbKey
 * @see OjbFactoryUtils#getPersistenceBroker
 * @see OjbFactoryUtils#closePersistenceBrokerIfNecessary
 * @see PersistenceBrokerTemplate#execute
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public class PersistenceBrokerTransactionManager extends AbstractOjbTransactionManager {

	/**
	 * Create a new PersistenceBrokerTransactionManager,
	 * sing the default connection configured for OJB.
	 */
	public PersistenceBrokerTransactionManager() {
	}

	/**
	 * Create a new PersistenceBrokerTransactionManager.
	 * @param jcdAlias the JDBC Connection Descriptor alias
	 * of the PersistenceBroker configuration to use
	 */
	public PersistenceBrokerTransactionManager(String jcdAlias) {
		setJcdAlias(jcdAlias);
	}

	/**
	 * Create a new PersistenceBrokerTransactionManager.
	 * @param pbKey the PBKey of the PersistenceBroker configuration to use
	 */
	public PersistenceBrokerTransactionManager(PBKey pbKey) {
		setPbKey(pbKey);
	}


	protected Object doGetTransaction() {
		PersistenceBrokerTransactionObject txObject = new PersistenceBrokerTransactionObject();
		PersistenceBrokerHolder pbHolder =
		    (PersistenceBrokerHolder) TransactionSynchronizationManager.getResource(getPbKey());
		txObject.setPersistenceBrokerHolder(pbHolder);
		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		return TransactionSynchronizationManager.hasResource(getPbKey());
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;

		PersistenceBroker pb = OjbFactoryUtils.getPersistenceBroker(getPbKey());
		if (logger.isDebugEnabled()) {
			logger.debug("Opened new persistence broker [" + pb + "] for OJB transaction");
		}

		txObject.setPersistenceBrokerHolder(new PersistenceBrokerHolder(pb));

		try {
			Connection con = pb.serviceConnectionManager().getConnection();
			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousIsolationLevel(previousIsolationLevel);

			pb.beginTransaction();

			// register the OJB PersistenceBroker's JDBC Connection for the DataSource, if set
			if (getDataSource() != null) {
				ConnectionHolder conHolder = new ConnectionHolder(con);
				if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
					conHolder.setTimeoutInSeconds(definition.getTimeout());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Exposing OJB transaction as JDBC transaction [" + conHolder.getConnection() + "]");
				}
				TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
				txObject.setConnectionHolder(conHolder);
			}

			TransactionSynchronizationManager.bindResource(getPbKey(), txObject.getPersistenceBrokerHolder());
		}

		catch (Exception ex) {
			OjbFactoryUtils.closePersistenceBrokerIfNecessary(pb, getPbKey());
			throw new CannotCreateTransactionException("Could not create OJB transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;
		txObject.setPersistenceBrokerHolder(null);
		PersistenceBrokerHolder pbHolder =
				(PersistenceBrokerHolder) TransactionSynchronizationManager.unbindResource(getPbKey());
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(pbHolder, connectionHolder);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		if (TransactionSynchronizationManager.hasResource(getPbKey())) {
			// from non-transactional code running in active transaction synchronization
			// -> can be safely removed, will be closed on transaction completion
			TransactionSynchronizationManager.unbindResource(getPbKey());
		}
		TransactionSynchronizationManager.bindResource(getPbKey(), resourcesHolder.getPersistenceBrokerHolder());
		if (getDataSource() != null) {
			TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
		}
	}

	protected boolean isRollbackOnly(Object transaction) {
		return ((PersistenceBrokerTransactionObject) transaction).getPersistenceBrokerHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing OJB transaction on persistence broker [" +
									 txObject.getPersistenceBrokerHolder().getPersistenceBroker() + "]");
		}
		try {
			txObject.getPersistenceBrokerHolder().getPersistenceBroker().commitTransaction();
		}
		catch (TransactionAbortedException ex) {
			// assumably from commit call to underlying JDBC connection
			throw new TransactionSystemException("Could not commit OJB transaction", ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back OJB transaction on persistence broker [" +
									 txObject.getPersistenceBrokerHolder().getPersistenceBroker() + "]");
		}
		txObject.getPersistenceBrokerHolder().getPersistenceBroker().abortTransaction();
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting OJB transaction on persistence broker [" +
									 txObject.getPersistenceBrokerHolder().getPersistenceBroker() + "] rollback-only");
		}
		txObject.getPersistenceBrokerHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;

		// remove the persistence broker from the thread
		TransactionSynchronizationManager.unbindResource(getPbKey());
		txObject.getPersistenceBrokerHolder().clear();

		// remove the JDBC connection holder from the thread, if set
		if (getDataSource() != null) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
		}

		PersistenceBroker pb = txObject.getPersistenceBrokerHolder().getPersistenceBroker();
		try {
			Connection con = pb.serviceConnectionManager().getConnection();
			DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
		}
		catch (LookupException ex) {
			logger.info("Could not look up JDBC connection of OJB persistence broker", ex);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Closing OJB persistence broker [" + pb + "] after transaction");
		}
		OjbFactoryUtils.closePersistenceBrokerIfNecessary(pb, getPbKey());
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by doSuspend and doResume.
	 * @see #doSuspend
	 * @see #doResume
	 */
	private static class SuspendedResourcesHolder {

		private final PersistenceBrokerHolder persistenceBrokerHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(PersistenceBrokerHolder pbHolder, ConnectionHolder conHolder) {
			this.persistenceBrokerHolder = pbHolder;
			this.connectionHolder = conHolder;
		}

		private PersistenceBrokerHolder getPersistenceBrokerHolder() {
			return persistenceBrokerHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
