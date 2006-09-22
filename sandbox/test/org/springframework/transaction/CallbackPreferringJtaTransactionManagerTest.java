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

package org.springframework.transaction;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author Juergen Hoeller
 * @since 19.09.2006
 */
public class CallbackPreferringJtaTransactionManagerTest extends JtaTransactionManagerTests {

	protected JtaTransactionManager newJtaTransactionManager(UserTransaction ut) {
		TestCallbackPreferringJtaTransactionManager tm = new TestCallbackPreferringJtaTransactionManager(
				new CallbackBasedTransactionServiceImpl(ut));
		tm.setUserTransaction(ut);
		tm.afterPropertiesSet();
		return tm;
	}


	private static class CallbackBasedTransactionServiceImpl implements
			TestCallbackPreferringJtaTransactionManager.CallbackBasedTransactionService {

		private final UserTransaction ut;

		private boolean transactionActive;

		private boolean rollbackOnly;

		public CallbackBasedTransactionServiceImpl(UserTransaction ut) {
			this.ut = ut;
			try {
				this.transactionActive = (ut.getStatus() != Status.STATUS_NO_TRANSACTION);
			}
			catch (SystemException ex) {
				throw new TransactionSystemException("JTA failure on getStatus", ex);
			}
		}

		public boolean isTransactionActive() {
			return this.transactionActive;
		}

		public void executeWithinTransaction(int timeout, boolean createNew, boolean suspendExistingIfNecessary,
				TestCallbackPreferringJtaTransactionManager.TransactionServiceRunnable runnable) {
			try {
				if (!this.transactionActive && createNew) {
					if (timeout >= 0) {
						ut.setTransactionTimeout(timeout);
					}
					ut.begin();
				}
				runnable.run(new TestCallbackPreferringJtaTransactionManager.TransactionStatusFacility() {
					public void setRollbackOnly() {
						rollbackOnly = true;
					}
					public void registerSynchronization(Synchronization synch) {
						synch.beforeCompletion();
						if (transactionActive) {
							synch.afterCompletion(Status.STATUS_UNKNOWN);
						}
						else {
							synch.afterCompletion(rollbackOnly ? Status.STATUS_ROLLEDBACK : Status.STATUS_COMMITTED);
						}
					}
				});
				if (this.transactionActive) {
					if (rollbackOnly) {
						if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
							ut.setRollbackOnly();
						}
					}
				}
				else if (createNew) {
					if (rollbackOnly) {
						if (ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
							ut.rollback();
						}
					}
					else {
						boolean globalRollbackOnly = (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK);
						ut.commit();
						if (globalRollbackOnly) {
							throw new UnexpectedRollbackException("");
						}
					}
				}
			}
			catch (Exception ex) {
				throw new TransactionSystemException("Failed", ex);
			}
		}
	}

}
