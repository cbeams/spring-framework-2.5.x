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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Helper class that simplifies programmatic transaction demarcation
 * and transaction exception handling.
 *
 * <p>The central method is "execute", supporting transactional code implementing
 * the TransactionCallback interface. It handles the transaction lifecycle and
 * possible exceptions such that neither the TransactionCallback implementation
 * nor the calling code needs to explicitly handle transactions.
 *
 * <p>Typical usage: Allows for writing low-level application services that use
 * (JNDI) resources but are not transaction-aware themselves. Instead, they
 * can implicitly take part in (JTA) transactions handled by higher-level
 * application services utilizing this class, making calls to the low-level
 * services via an inner-class callback object.
 *
 * <p>Can be used within a service implementation via direct instantiation with
 * a transaction manager reference, or get prepared in an application context
 * and given to services as bean reference. Note: The transaction manager should
 * always be configured as bean in the application context, in the first case
 * given to the service directly, in the second case to the prepared template.
 *
 * <p>Supports setting the propagation behavior and the isolation level by name,
 * for convenient configuration in context definitions.
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see org.springframework.transaction.support.TransactionCallback
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public class TransactionTemplate extends DefaultTransactionDefinition implements InitializingBean{

	protected final Log logger = LogFactory.getLog(getClass());

	private PlatformTransactionManager transactionManager = null;

	/**
	 * Create a new TransactionTemplate instance.
	 * Mainly targetted at configuration by a bean factory.
	 * Note: Transaction manager property needs to be set before any execute calls.
	 * @see #setTransactionManager
	 */
	public TransactionTemplate() {
	}

	/**
	 * Create a new TransactionTemplate instance.
	 * @param transactionManager transaction management strategy to be used
	 */
	public TransactionTemplate(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Set the transaction management strategy to be used.
	 */
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	/**
	 * Return the transaction management strategy to be used.
	 */
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("transactionManager is required");
		}
	}

	/**
	 * Execute the action specified by the given callback object within a transaction.
	 * <p>Allows for returning a result object created within the transaction, i.e.
	 * a domain object or a collection of domain objects. A RuntimeException thrown
	 * by the callback is treated as application exception that enforces a rollback.
	 * An exception gets propagated to the caller of the template.
	 * @param action callback object that specifies the transactional action
	 * @return a result object returned by the callback, or null
	 * @throws TransactionException in case of initialization, rollback, or system errors
	 */
	public Object execute(TransactionCallback action) throws TransactionException {
		TransactionStatus status = this.transactionManager.getTransaction(this);
		Object result = null;
		try {
			result = action.doInTransaction(status);
		}
		catch (RuntimeException ex) {
			// transactional code threw application exception -> rollback
			rollbackOnException(status, ex);
			throw ex;
		}
		catch (Error err) {
			// transactional code threw error -> rollback
			rollbackOnException(status, err);
			throw err;
		}
		this.transactionManager.commit(status);
		return result;
	}

	/**
	 * Perform a rollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 */
	private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initiating transaction rollback on application exception", ex);
		}
		try {
			this.transactionManager.rollback(status);
		}
		catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
		catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}

}
