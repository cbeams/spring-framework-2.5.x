/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author Rod Johnson
 * @version $Id: CountingTxManager.java,v 1.3 2004-01-26 18:03:45 jhoeller Exp $
 */
public class CountingTxManager extends AbstractPlatformTransactionManager {
	
	public int commits;
	public int rollbacks;
	public int inflight;
	public boolean rollbackOnly;

	protected Object doGetTransaction() {
		return new Object();
	}

	protected boolean isExistingTransaction(Object transaction) {
		return false;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		++inflight;
	}

	protected Object doSuspend(Object transaction) {
		return null;
	}

	protected void doResume(Object transaction, Object suspendedResources)
	    throws TransactionException {
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		return false;
	}

	protected void doCommit(DefaultTransactionStatus status) {
		++commits;
		--inflight;
	}

	protected void doRollback(DefaultTransactionStatus status) {
		++rollbacks;
		--inflight;
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		rollbackOnly = true;
	}

	protected void doCleanupAfterCompletion(Object transaction) {
	}

}
