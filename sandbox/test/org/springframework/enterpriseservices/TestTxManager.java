/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

/**
 * PlatformTransactionManager implementation that allows us to
 * count the number of transactions created.
 * @author Rod Johnson
 * @version $Id: TestTxManager.java,v 1.1 2003-11-22 09:05:40 johnsonr Exp $
 */
public class TestTxManager extends AbstractPlatformTransactionManager {

	protected Object TX = new Object();
	
	public int committed = 0;
	public int rolledback = 0;
	public int inflight = 0;
	
	public TestTxManager() {
		
	}
	
	public void clear() {
		this.committed = this.rolledback = this.inflight = 0;
	}
	
	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
	 */
	protected Object doGetTransaction() throws CannotCreateTransactionException, TransactionException {
		return TX;
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
		++inflight;
	}
	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.TransactionStatus)
	 */
	protected void doCommit(TransactionStatus status) throws TransactionException {
		--inflight;
		logger.info("doCommit++++");
		++committed;
	}
	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.TransactionStatus)
	 */
	protected void doRollback(TransactionStatus status) throws TransactionException {
		--inflight;
		logger.info("doRollback----");
		++rolledback;
		
	}
	/**
	 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doSetRollbackOnly(org.springframework.transaction.TransactionStatus)
	 */
	protected void doSetRollbackOnly(TransactionStatus status) throws TransactionException {
		status.setRollbackOnly();
	}

}
