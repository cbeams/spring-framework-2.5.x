package org.springframework.transaction.annotation;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author robh
 */
public class CallCountingPlatformTransactionManager implements PlatformTransactionManager {
	private int getTransactionCount;

	private int commitCount;

	private int rollbackCount;

	private boolean lastTransactionReadOnly;

	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		getTransactionCount++;
		this.lastTransactionReadOnly = definition.isReadOnly();
		return new DefaultTransactionStatus(new Object(), true, true, this.lastTransactionReadOnly, false, null);
	}

	public void commit(TransactionStatus status) throws TransactionException {
		commitCount++;
	}

	public void rollback(TransactionStatus status) throws TransactionException {
		rollbackCount++;
	}

	public int getGetTransactionCount() {
		return getTransactionCount;
	}

	public int getCommitCount() {
		return commitCount;
	}

	public int getRollbackCount() {
		return rollbackCount;
	}

	public boolean isLastTransactionReadOnly() {
		return lastTransactionReadOnly;
	}
}
