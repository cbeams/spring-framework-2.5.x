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

package org.springframework.transaction;

/**
 * Representation of the status of a transaction,
 * consisting of a transaction object and some status flags.
 *
 * <p>Transactional code can use this to retrieve status information,
 * and to programmatically request a rollback (instead of throwing
 * an exception that causes an implicit rollback).
 *
 * <p>Derives from the SavepointManager interface to provide access
 * to savepoint management facilities. Note that savepoint management
 * is just available if the actual transaction manager supports it.
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see PlatformTransactionManager
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus
 * @see #setRollbackOnly
 */
public interface TransactionStatus extends SavepointManager {

	/**
	 * Return if the transaction is new,
	 * else participating in an existing transaction.
	 */
	boolean isNewTransaction();

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback,
	 * proceeding with the normal applicaiton workflow though (i.e. no exception). 
	 * <p>For transactions managed by TransactionTemplate or TransactionInterceptor.
	 * An alternative way to trigger a rollback is throwing an application exception.
	 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn
	 */
	void setRollbackOnly();

	/**
	 * Return if the transaction has been set rollback-only.
	 */
	public boolean isRollbackOnly();

}
