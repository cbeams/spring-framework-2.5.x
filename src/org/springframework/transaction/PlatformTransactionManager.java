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
 * This is the central interface in Spring's transaction support.
 * Applications can use this directly, but it is not primarily meant as API.
 * Typically, applications will work with either TransactionTemplate or the
 * AOP transaction interceptor.
 *
 * <p>For implementers, AbstractPlatformTransactionManager is a good starting
 * point. The default implementations are DataSourceTransactionManager and
 * JtaTransactionManager.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16-Mar-2003
 * @see org.springframework.transaction.support.TransactionTemplate
 * @see org.springframework.transaction.interceptor.TransactionInterceptor
 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public interface PlatformTransactionManager {

	/**
	 * Return a currently active transaction or create a new one.
	 * Note that parameters like isolation level or timeout will only be applied
	 * to new transactions, and thus be ignored when participating in active ones.
	 * Furthermore, they aren't supported by every transaction manager:
	 * A proper implementation should thrown an exception when custom values
	 * that it doesn't support are specified.
	 * @param definition TransactionDefinition instance (can be null for defaults),
	 * describing propagation behavior, isolation level, timeout etc.
	 * @return transaction status object representing the new or current transaction
	 * @throws TransactionException in case of lookup, creation, or system errors
	 */
	TransactionStatus getTransaction(TransactionDefinition definition)
	    throws TransactionException;

	/**
	 * Commit the given transaction, with regard to its status.
	 * If the transaction has been marked rollback-only programmatically,
	 * perform a rollback.
	 * If the transaction wasn't a new one, omit the commit
	 * to take part in the surrounding transaction properly.
	 * @param status object returned by the getTransaction() method.
	 * @throws TransactionException in case of commit or system errors
	 */
	void commit(TransactionStatus status) throws TransactionException;

	/**
	 * Roll back the given transaction, with regard to its status.
	 * If the transaction wasn't a new one, just set it rollback-only
	 * to take part in the surrounding transaction properly.
	 * @param status object returned by the getTransaction() method.
	 * @throws TransactionException in case of system errors
	 */
	void rollback(TransactionStatus status) throws TransactionException;

}
