/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author Rod Johnson
 * @version $Id: CountingTxManager.java,v 1.2 2004-01-20 10:41:10 jhoeller Exp $
 */
public class CountingTxManager extends AbstractPlatformTransactionManager {
	
	public int commits;
	public int rollbacks;
	public int inflight;
	private boolean rollbackOnly;

	protected Object doGetTransaction() throws TransactionException {
		return new Object();
	}

	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		++inflight;
	}

	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		++commits;
		--inflight;
	}

	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		++rollbacks;
		--inflight;
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
		rollbackOnly = true;
	}

}
