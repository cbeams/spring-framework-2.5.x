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

package org.springframework.transaction.support;

import org.springframework.transaction.TransactionStatus;

/**
 * Default implementation of the TransactionStatus interface,
 * used by AbstractPlatformTransactionManager.
 *
 * <p>Holds all status information that AbstractPlatformTransactionManager
 * needs internally, including a generic transaction object determined by
 * the concrete transaction manager implementation.
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see AbstractPlatformTransactionManager
 */
public class DefaultTransactionStatus implements TransactionStatus {

	private Object transaction;

	private boolean newTransaction;

	private boolean newSynchronization;

	private boolean readOnly;

	private boolean debug;

	private Object suspendedResources;

	private boolean rollbackOnly;

	/**
	 * Create a new TransactionStatus instance.
	 * @param transaction underlying transaction object,
	 * e.g. a JTA UserTransaction
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 * @param newSynchronization if a new transaction synchronization
	 * has been opened for the given transaction
	 * @param debug should debug logging be enabled for the handling of this transaction?
	 * Caching it in here can prevent repeated calls to ask the logging system whether
	 * debug logging should be enabled.
	 */
	public DefaultTransactionStatus(Object transaction, boolean newTransaction,
																	boolean newSynchronization, boolean readOnly,
	                                boolean debug, Object suspendedResources) {
		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.newSynchronization = newSynchronization;
		this.readOnly = readOnly;
		this.debug = debug;
		this.suspendedResources = suspendedResources;
	}

	/**
	 * Return the underlying transaction object, e.g. a JTA UserTransaction.
	 */
	public Object getTransaction() {
		return transaction;
	}

	public boolean isNewTransaction() {
		return (transaction != null && newTransaction);
	}

	/**
	 * Return if a new transaction synchronization has been opened
	 * for this transaction.
	 */
	public boolean isNewSynchronization() {
		return newSynchronization;
	}

	/**
	 * Return if this transaction is defined as read-only transaction.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Return whether the progress of this transaction is debugged. This is used
	 * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
	 * calls to logger.isDebug(). Not really intended for client code.
	 */
	public boolean isDebug() {
		// TODO could consider opening this up so that it escalates logging level
		// on behalf of clients, as well as for the present efficiency concern. Could
		// also even provide a transaction log, to ensure that output all appeared together.
		return debug;
	}

	public Object getSuspendedResources() {
		return suspendedResources;
	}

	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

}
