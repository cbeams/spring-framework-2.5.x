/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.benchmark.invokers;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * 
 * @author Rod Johnson
 * @version $Id: DummyTransactionManager.java,v 1.1 2003-12-02 22:31:06 johnsonr Exp $
 */
public class DummyTransactionManager extends AbstractPlatformTransactionManager {

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
	 */
	protected Object doGetTransaction() throws TransactionException {
		//System.err.println("New transaction");
		return new Object();
	}

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#isExistingTransaction(java.lang.Object)
	 */
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object, org.springframework.transaction.TransactionDefinition)
	 */
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		
	}

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.TransactionStatus)
	 */
	protected void doCommit(TransactionStatus status) throws TransactionException {
		
	}

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.TransactionStatus)
	 */
	protected void doRollback(TransactionStatus status) throws TransactionException {
		
	}

	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doSetRollbackOnly(org.springframework.transaction.TransactionStatus)
	 */
	protected void doSetRollbackOnly(TransactionStatus status) throws TransactionException {
		
	}

}
