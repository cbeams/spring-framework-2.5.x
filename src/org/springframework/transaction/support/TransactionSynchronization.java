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

/**
 * Interface for transaction synchronization callbacks.
 * Supported by AbstractPlatformTransactionManager.
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 */
public interface TransactionSynchronization {

	/** Completion status in case of proper commit */
	int STATUS_COMMITTED = 0;

	/** Completion status in case of proper rollback */
	int STATUS_ROLLED_BACK = 1;

	/** Completion status in case of heuristic mixed completion or system errors */
	int STATUS_UNKNOWN = 2;
	

	/**
	 * Suspend this synchronization. Supposed to unbind resources
	 * from TransactionSynchronizationManager if managing any.
	 * @see TransactionSynchronizationManager#unbindResource
	 */
	void suspend();

	/**
	 * Resume this synchronization. Supposed to rebind resources
	 * to TransactionSynchronizationManager if managing any.
	 * @see TransactionSynchronizationManager#bindResource
	 */
	void resume();

	/**
	 * Invoked before transaction commit (before "beforeCompletion").
	 * Can e.g. flush transactional sessions to the database.
	 * <p>Note that exceptions will get propagated to the commit caller
	 * and cause a rollback of the transaction.
	 * @param readOnly if the transaction is defined as read-only transaction 
	 * @throws RuntimeException in case of errors
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #beforeCompletion
	 */
	void beforeCommit(boolean readOnly);

	/**
	 * Invoked before transaction commit/rollback (after "beforeCommit", even
	 * if beforeCommit threw an exception). Can e.g. perform resource cleanup.
	 * <p>Note that exceptions will get propagated to the commit caller
	 * and cause a rollback of the transaction.
	 * @throws RuntimeException in case of errors
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #beforeCommit
	 * @see #afterCompletion
	 */
	void beforeCompletion();

	/**
	 * Invoked after transaction commit/rollback. Can e.g. perform resource
	 * cleanup, in this case after transaction completion.
	 * <p>Note that exceptions will get propagated to the commit or rollback
	 * caller, although they will not influence the outcome of the transaction.
	 * @param status completion status according to the STATUS_* constants
	 * @throws RuntimeException in case of errors
	 * (note: do not throw TransactionException subclasses here!)
	 * @see #STATUS_COMMITTED
	 * @see #STATUS_ROLLED_BACK
	 * @see #STATUS_UNKNOWN
	 * @see #beforeCompletion
	 */
	void afterCompletion(int status);

}
