/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Used for testing only (for example, when we must replace the
 * behaviour of a PlatformTransactionManager bean we don't have access to).
 * Allows behaviour of an entire class to change with static delegate change.
 * Not multithreaded.
 * @author Rod Johnson
 * @since 26-Apr-2003
 * @version $Revision: 1.1.1.1 $
 */
public class PlatformTransactionManagerFacade implements PlatformTransactionManager {
	
	/**
	 * This member can be changed to change behaviour class-wide.
	 */
	public static PlatformTransactionManager delegate;

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#getTransaction(org.springframework.transaction.TransactionDefinition)
	 */
	public TransactionStatus getTransaction(TransactionDefinition definition) {
		return delegate.getTransaction(definition);
	}

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#commit(org.springframework.transaction.TransactionStatus)
	 */
	public void commit(TransactionStatus status) {
		delegate.commit(status);
	}

	/**
	 * @see org.springframework.transaction.PlatformTransactionManager#rollback(org.springframework.transaction.TransactionStatus)
	 */
	public void rollback(TransactionStatus status) {
		delegate.rollback(status);
	}

}
