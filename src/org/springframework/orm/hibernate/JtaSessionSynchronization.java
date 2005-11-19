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

package org.springframework.orm.hibernate;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Callback for resource cleanup at the end of a non-Spring JTA transaction,
 * i.e. when plain JTA or EJB CMT is used without Spring's JtaTransactionManager.
 *
 * @author Juergen Hoeller
 * @see SessionFactoryUtils
 * @see javax.transaction.Transaction#registerSynchronization
 */
class JtaSessionSynchronization implements Synchronization {

	private final SpringSessionSynchronization springSessionSynchronization;

	private final TransactionManager jtaTransactionManager;

	private boolean beforeCompletionCalled = false;


	public JtaSessionSynchronization(
			SpringSessionSynchronization springSessionSynchronization, TransactionManager jtaTransactionManager) {

		this.springSessionSynchronization = springSessionSynchronization;
		this.jtaTransactionManager = jtaTransactionManager;
	}


	/**
	 * JTA beforeCompletion callback: just invoked on commit.
	 * <p>In case of an exception, the JTA transaction gets set to rollback-only.
	 * (Synchronization.beforeCompletion is not supposed to throw an exception.)
	 * @see SpringSessionSynchronization#beforeCommit
	 */
	public void beforeCompletion() {
		try {
			boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
			this.springSessionSynchronization.beforeCommit(readOnly);
		}
		catch (Throwable ex) {
			SessionFactoryUtils.logger.error("beforeCommit callback threw exception", ex);
			try {
				this.jtaTransactionManager.setRollbackOnly();
			}
			catch (SystemException ex2) {
				SessionFactoryUtils.logger.error("Could not set JTA transaction rollback-only", ex2);
			}
		}
		// Unbind the SessionHolder from the thread early, to avoid issues
		// with strict JTA implementations that issue warnings when doing JDBC
		// operations after transaction completion (e.g. Connection.getWarnings).
		this.beforeCompletionCalled = true;
		this.springSessionSynchronization.beforeCompletion();
	}

	/**
	 * JTA afterCompletion callback: invoked after commit/rollback.
	 * <p>Needs to invoke SpringSessionSynchronization's beforeCompletion
	 * at this late stage, as there's no corresponding callback with JTA.
	 * @see SpringSessionSynchronization#beforeCompletion
	 * @see SpringSessionSynchronization#afterCompletion
	 */
	public void afterCompletion(int status) {
		if (!this.beforeCompletionCalled) {
			// beforeCompletion not called before (probably because of JTA rollback).
			// Unbind the SessionHolder from the thread here.
			this.springSessionSynchronization.beforeCompletion();
		}
		// Reset the synchronizedWithTransaction flag,
		// and clear the Hibernate Session after a rollback (if necessary).
		switch (status) {
			case Status.STATUS_COMMITTED:
				this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
				break;
			case Status.STATUS_ROLLEDBACK:
				this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
				break;
			default:
				this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
		}
	}

}
