/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.benchmark.invokers;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author Rod Johnson
 */
public class DummyTransactionManager extends AbstractPlatformTransactionManager {

	protected Object doGetTransaction() throws TransactionException {
		return new Object();
	}

	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
	}

	protected Object doSuspend(Object transaction) throws TransactionException {
		return null;
	}

	protected void doResume(Object transaction, Object suspendedResources)
			throws TransactionException {
	}

	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		return false;
	}

	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
	}

	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
	}

	protected void doCleanupAfterCompletion(Object transaction) {
	}

}
