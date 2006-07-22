/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.transaction.jta;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Adapter that implements the JTA Synchronization interface
 * delegating to an underlying Spring TransactionSynchronization.
 *
 * <p>Useful for synchronizing Spring resource management code
 * with plain JTA transactions, despite the original code being
 * built for Spring transaction synchronization.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see javax.transaction.Transaction#registerSynchronization
 * @see org.springframework.transaction.support.TransactionSynchronization
 */
public class SpringJtaSynchronizationAdapter implements Synchronization {

	protected static final Log logger = LogFactory.getLog(SpringJtaSynchronizationAdapter.class);

	private final TransactionSynchronization springSynchronization;

	private UserTransaction jtaTransaction;

	private boolean beforeCompletionCalled = false;


	/**
	 * Create a new SpringJtaSynchronizationAdapter for the given Spring
	 * TransactionSynchronization and JTA TransactionManager.
	 * @param springSynchronization the Spring TransactionSynchronization to delegate to
	 * @param jtaUserTransaction the JTA UserTransaction to use for rollback-only
	 * setting in case of an exception thrown in <code>beforeCompletion</code>
	 * (can be omitted if the JTA provider itself marks the transaction rollback-only
	 * in such a scenario, which is required by the JTA specification as of JTA 1.1)
	 */
	public SpringJtaSynchronizationAdapter(
			TransactionSynchronization springSynchronization, UserTransaction jtaUserTransaction) {

		Assert.notNull(springSynchronization, "TransactionSynchronization must not be null");
		this.springSynchronization = springSynchronization;
		this.jtaTransaction = jtaUserTransaction;
	}

	/**
	 * Create a new SpringJtaSynchronizationAdapter for the given Spring
	 * TransactionSynchronization and JTA TransactionManager.
	 * @param springSynchronization the Spring TransactionSynchronization to delegate to
	 * @param jtaTransactionManager the JTA TransactionManager to use for rollback-only
	 * setting in case of an exception thrown in <code>beforeCompletion</code>
	 * (can be omitted if the JTA provider itself marks the transaction rollback-only
	 * in such a scenario, which is required by the JTA specification as of JTA 1.1)
	 */
	public SpringJtaSynchronizationAdapter(
			TransactionSynchronization springSynchronization, TransactionManager jtaTransactionManager) {

		this(springSynchronization,
				(jtaTransactionManager != null ? new UserTransactionAdapter(jtaTransactionManager) : null));
	}


	/**
	 * JTA beforeCompletion callback: just invoked on commit.
	 * <p>In case of an exception, the JTA transaction gets set to rollback-only.
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit
	 */
	public void beforeCompletion() {
		try {
			boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			this.springSynchronization.beforeCommit(readOnly);
		}
		catch (RuntimeException ex) {
			setRollbackOnlyIfPossible();
			throw ex;
		}
		catch (Error err) {
			setRollbackOnlyIfPossible();
			throw err;
		}
		finally {
			// Unbind the SessionHolder from the thread early, to avoid issues
			// with strict JTA implementations that issue warnings when doing JDBC
			// operations after transaction completion (e.g. Connection.getWarnings).
			this.beforeCompletionCalled = true;
			this.springSynchronization.beforeCompletion();
		}
	}

	/**
	 * Set the underlying JTA transaction to rollback-only.
	 */
	private void setRollbackOnlyIfPossible() {
		if (this.jtaTransaction != null) {
			try {
				this.jtaTransaction.setRollbackOnly();
			}
			catch (SystemException ex) {
				logger.error("Could not set JTA transaction rollback-only", ex);
			}
		}
	}

	/**
	 * JTA afterCompletion callback: invoked after commit/rollback.
	 * <p>Needs to invoke SpringSessionSynchronization's beforeCompletion
	 * at this late stage, as there's no corresponding callback with JTA.
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion
	 * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion
	 */
	public void afterCompletion(int status) {
		if (!this.beforeCompletionCalled) {
			// beforeCompletion not called before (probably because of JTA rollback).
			// Perform the cleanup here.
			this.springSynchronization.beforeCompletion();
		}
		// Call afterCompletion with the appropriate status indication.
		switch (status) {
			case Status.STATUS_COMMITTED:
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
				break;
			case Status.STATUS_ROLLEDBACK:
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
				break;
			default:
				this.springSynchronization.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
		}
	}

}
