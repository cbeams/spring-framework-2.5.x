package org.springframework.transaction.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Abstract base class that allows for easy implementation of
 * concrete platform transaction managers.
 *
 * <p>Provides the following case handling:
 * <ul>
 * <li>determines if there is an existing transaction;
 * <li>applies the appropriate propagation behavior;
 * <li>determines programmatic rollback on commit;
 * <li>applies the appropriate modification on rollback
 * (actual rollback or setting rollback-only);
 * <li>triggers registered synchronization callbacks
 * (if transactionSynchronization is active).
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 28.03.2003
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean transactionSynchronization = false;

	private boolean rollbackOnCommitFailure = false;

	/**
	 * Set if this transaction manager should activate the thread-bound
	 * transaction synchronization support. The default can very between
	 * transaction manager implementations; this class specifies false.
	 * <p>Note that transaction synchronization isn't supported for
	 * multiple concurrent transactions by different transaction managers.
	 * Only one transaction manager is allowed to activate it at any time.
	 * @see TransactionSynchronizationManager
	 * @see TransactionSynchronization
	 */
	public void setTransactionSynchronization(boolean transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	/**
	 * Return if this transaction manager should activate the thread-bound
	 * transaction synchronization support.
	 */
	public boolean getTransactionSynchronization() {
		return transactionSynchronization;
	}

	/**
	 * Set if a rollback should be performed on failure of the commit call.
	 * Typically not necessary and thus to be avoided as it can override the
	 * commit exception with a subsequent rollback exception. Default is false.
	 */
	public void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	/**
	 * Return if a rollback should be performed on failure of the commit call.
	 */
	public boolean isRollbackOnCommitFailure() {
		return rollbackOnCommitFailure;
	}

	/**
	 * This implementation of getTransaction handles propagation behavior.
	 * Delegates to doGetTransaction, isExistingTransaction, doBegin.
	 * @see #doGetTransaction
	 * @see #isExistingTransaction
	 * @see #doBegin
	 */
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		Object transaction = doGetTransaction();
		logger.debug("Using transaction object [" + transaction + "]");

		if (isExistingTransaction(transaction)) {
			logger.debug("Participating in existing transaction");
			return new TransactionStatus(transaction, false);
		}

		if (definition == null) {
			// use defaults
			definition = new DefaultTransactionDefinition();
		}
		if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
			throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());
		}
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new NoTransactionException("Transaction propagation mandatory but no existing transaction context found");
		}

		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED) {
			// create new transaction
			doBegin(transaction, definition);
			if (this.transactionSynchronization) {
				TransactionSynchronizationManager.initSynchronization();
			}
			return new TransactionStatus(transaction, true);
		}
		else {
			// empty (-> "no") transaction
			return new TransactionStatus(null, false);
		}
	}

	/**
	 * This implementation of commit handles participating in existing
	 * transactions and programmatic rollback requests.
	 * Delegates to isRollbackOnly, doCommit and rollback.
	 * @see org.springframework.transaction.TransactionStatus#isRollbackOnly
	 * @see #isRollbackOnly
	 * @see #doCommit
	 * @see #rollback
	 */
	public void commit(TransactionStatus status) throws TransactionException {
		if (status.isRollbackOnly() ||
		    (status.getTransaction() != null && isRollbackOnly(status.getTransaction()))) {
			logger.debug("Transactional code has requested rollback");
			rollback(status);
		}
		else if (status.isNewTransaction()) {
			// initiate transaction commit
			try {
				logger.debug("Triggering beforeCommit synchronization");
				TransactionSynchronizationManager.triggerBeforeCommit();
				logger.debug("Triggering beforeCompletion synchronization");
				TransactionSynchronizationManager.triggerBeforeCompletion();
				logger.debug("Initiating transaction commit");
				doCommit(status);
				triggerAfterCompletion(TransactionSynchronization.STATUS_COMMITTED, null);
			}
			catch (UnexpectedRollbackException ex) {
				triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK, ex);
				throw ex;
			}
			catch (TransactionException ex) {
				if (this.rollbackOnCommitFailure) {
					doRollbackOnCommitException(status, ex);
					triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK, ex);
				}
				else {
					triggerAfterCompletion(TransactionSynchronization.STATUS_UNKNOWN, ex);
				}
				throw ex;
			}
			catch (RuntimeException ex) {
				doRollbackOnCommitException(status, ex);
				triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK, ex);
				throw ex;
			}
			catch (Error err) {
				doRollbackOnCommitException(status, err);
				triggerAfterCompletion(TransactionSynchronization.STATUS_UNKNOWN, err);
				throw err;
			}
			finally {
				cleanupAfterCompletion(status.getTransaction());
				TransactionSynchronizationManager.clearSynchronization();
			}
		}
	}

	/**
	 * This implementation of rollback handles participating in existing
	 * transactions. Delegates to doRollback and doSetRollbackOnly.
	 * @see #doRollback
	 * @see #doSetRollbackOnly
	 */
	public void rollback(TransactionStatus status) throws TransactionException {
		if (status.isNewTransaction()) {
			try {
				logger.debug("Triggering beforeCompletion synchronization");
				TransactionSynchronizationManager.triggerBeforeCompletion();
				logger.debug("Initiating transaction rollback");
				doRollback(status);
				triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK, null);
			}
			catch (TransactionException ex) {
				triggerAfterCompletion(TransactionSynchronization.STATUS_UNKNOWN, ex);
				throw ex;
			}
			finally {
				cleanupAfterCompletion(status.getTransaction());
				TransactionSynchronizationManager.clearSynchronization();
			}
		}
		else if (status.getTransaction() != null) {
			doSetRollbackOnly(status);
		}
		else {
			// no transaction available
			logger.warn("Should roll back transaction but cannot - no transaction available");
		}
	}

	/**
	 * Invoke doRollback, handling rollback exceptions properly.
	 * @param status object representing the transaction
	 * @param ex the thrown application exception or error
	 * @throws TransactionException in case of a rollback error
	 * @see #doRollback
	 */
	private void doRollbackOnCommitException(TransactionStatus status, Throwable ex) throws TransactionException {
		try {
			doRollback(status);
		}
		catch (TransactionException tex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			throw tex;
		}
	}

	/**
	 * Trigger afterCompletion callback, handling rollback exceptions properly.
	 * @param status completion status according to TransactionSynchronization constants
	 * @param ex the thrown application exception or error, or null
	 * @throws TransactionException in case of a rollback error
	 */
	private void triggerAfterCompletion(int status, Throwable ex) {
		logger.debug("Triggering afterCompletion synchronization");
		try {
			TransactionSynchronizationManager.triggerAfterCompletion(status);
		}
		catch (RuntimeException tsex) {
			if (ex != null) {
				logger.error("Rollback exception overridden by synchronization exception", ex);
			}
			throw tsex;
		}
		catch (Error tserr) {
			if (ex != null) {
				logger.error("Rollback exception overridden by synchronization exception", ex);
			}
			throw tserr;
		}
	}

	/**
	 * Return a current transaction object, i.e. a JTA UserTransaction.
	 * @return the current transaction object
	 * @throws CannotCreateTransactionException if transaction support is
	 * not available (e.g. no JTA UserTransaction retrievable from JNDI)
	 * @throws TransactionException in case of lookup or system errors
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * Check if the given transaction object indicates an existing,
	 * i.e. already begun, transaction.
	 * @param transaction transaction object returned by doGetTransaction()
	 * @return if there is an existing transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract boolean isExistingTransaction(Object transaction) throws TransactionException;

	/**
	 * Begin a new transaction with the given transaction definition.
	 * Does not have to care about applying the propagation behavior,
	 * as this has already been handled by this abstract manager.
	 * @param transaction transaction object returned by doGetTransaction()
	 * @param definition TransactionDefinition instance, describing
	 * propagation behavior, isolation level, timeout etc.
	 * @throws TransactionException in case of creation or system errors
	 */
	protected abstract void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException;

	/**
	 * Check if the given transaction object indicates a rollback-only,
	 * assumably from a nested transaction (else, the TransactionStatus
	 * of this transaction would have indicated rollback-only).
	 * @param transaction transaction object returned by doGetTransaction()
	 * @return if the transaction has to result in a rollback
	 * @throws TransactionException in case of creation or system errors
	 */
	protected boolean isRollbackOnly(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * Perform an actual commit on the given transaction.
	 * An implementation does not need to check the rollback-only flag.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of commit or system errors
	 */
	protected abstract void doCommit(TransactionStatus status) throws TransactionException;

	/**
	 * Perform an actual rollback on the given transaction.
	 * An implementation does not need to check the new transaction flag.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract void doRollback(TransactionStatus status) throws TransactionException;

	/**
	 * Set the given transaction rollback-only. Only called on rollback
	 * if the current transaction takes part in an existing one.
	 * @param status status representation of the transaction
	 * @throws TransactionException in case of system errors
	 */
	protected abstract void doSetRollbackOnly(TransactionStatus status) throws TransactionException;

	/**
	 * Cleanup resources after transaction completion.
	 * Called after doCommit and doRollback execution on any outcome.
	 * Should not throw any exceptions but just issue warnings on errors.
	 * @param transaction transaction object returned by doGetTransaction()
	 */
	protected void cleanupAfterCompletion(Object transaction) {
	}

}
