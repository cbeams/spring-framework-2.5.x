package org.springframework.transaction.support;

/**
 * Interface for callbacks after transaction completion.
 * Supported by AbstractPlatformTransactionManager.
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 */
public interface TransactionSynchronization {

	/**
	 * Completion status in case of proper commit
	 */
	int STATUS_COMMITTED = 0;

	/**
	 * Completion status in case of proper rollback
	 */
	int STATUS_ROLLED_BACK = 1;

	/**
	 * Status in case of heuristic mixed completion or system errors
	 */
	int STATUS_UNKNOWN = 2;

	/**
	 * Invoked before transaction commit.
	 * Can e.g. flush of transactional sessions to the database.
	 * <p>Note that exceptions will get propagated to the commit caller
	 * and cause a rollback of the transaction.
	 * @throws RuntimeException in case of errors
	 */
	void beforeCommit() throws RuntimeException;

	/**
	 * Invoked after transaction completion.
	 * Can e.g. perform proper resource cleanup.
	 * <p>Note that exceptions will get propagated to the commit or rollback
	 * caller, although they will not influence the outcome of the transaction.
	 * @param status completion status according to the STATUS_ constants
	 * @throws RuntimeException in case of errors
	 * @see #STATUS_COMMITTED
	 * @see #STATUS_ROLLED_BACK
	 * @see #STATUS_UNKNOWN
	 */
	void afterCompletion(int status) throws RuntimeException;

}
