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

import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;

import org.springframework.transaction.TransactionDefinition;
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
		txObject.setPersistenceBrokerHolder(new PersistenceBrokerHolder(pb));
		pb.beginTransaction();
		TransactionSynchronizationManager.bindResource(getPbKey(), txObject.getPersistenceBrokerHolder());
	}

	protected Object doSuspend(Object transaction) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;
		txObject.setPersistenceBrokerHolder(null);
		return TransactionSynchronizationManager.unbindResource(getPbKey());
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		PersistenceBrokerHolder pmHolder = (PersistenceBrokerHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(getPbKey(), pmHolder);
	}

	protected boolean isRollbackOnly(Object transaction) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;
		return txObject.getPersistenceBrokerHolder().isRollbackOnly();
	}

	protected void doCommit(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		txObject.getPersistenceBrokerHolder().getPersistenceBroker().commitTransaction();
	}

	protected void doRollback(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		txObject.getPersistenceBrokerHolder().getPersistenceBroker().abortTransaction();
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) status.getTransaction();
		txObject.getPersistenceBrokerHolder().setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		PersistenceBrokerTransactionObject txObject = (PersistenceBrokerTransactionObject) transaction;
		TransactionSynchronizationManager.unbindResource(getPbKey());
		OjbFactoryUtils.closePersistenceBrokerIfNecessary(
		    txObject.getPersistenceBrokerHolder().getPersistenceBroker(), getPbKey());
	}

}
