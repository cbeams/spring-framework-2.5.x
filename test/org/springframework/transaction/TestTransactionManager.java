package org.springframework.transaction;

import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author Juergen Hoeller
 * @since 29.04.2003
 */
class TestTransactionManager extends AbstractPlatformTransactionManager {

	private static final Object TRANSACTION = "transaction";

	private final boolean existingTransaction;

	private final boolean canCreateTransaction;

	protected boolean begin = false;

	protected boolean commit = false;

	protected boolean rollback = false;

	protected boolean rollbackOnly = false;

	protected TestTransactionManager(boolean existingTransaction, boolean canCreateTransaction) {
		this.existingTransaction = existingTransaction;
		this.canCreateTransaction = canCreateTransaction;
		setTransactionSynchronization(SYNCHRONIZATION_NEVER);
	}

	protected Object doGetTransaction() {
		return TRANSACTION;
	}

	protected boolean isExistingTransaction(Object transaction) {
		return existingTransaction;
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (!TRANSACTION.equals(transaction)) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		if (!this.canCreateTransaction) {
			throw new CannotCreateTransactionException("Cannot create transaction");
		}
		this.begin = true;
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

	protected void doCommit(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.commit = true;
	}

	protected void doRollback(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.rollback = true;
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		if (!TRANSACTION.equals(status.getTransaction())) {
			throw new IllegalArgumentException("Not the same transaction object");
		}
		this.rollbackOnly = true;
	}

	protected void doCleanupAfterCompletion(Object transaction) {
	}

}
