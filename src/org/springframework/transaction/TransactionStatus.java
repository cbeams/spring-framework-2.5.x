package org.springframework.transaction;

/**
 * Representation of the status of a transaction,
 * consisting of a transaction object and some status flags.
 *
 * <p>Transactional code can use this to retrieve status information,
 * and to programmatically request a rollback (instead of throwing
 * an exception that causes an implicit rollback).
 *
 * @author Juergen Hoeller
 * @since 27.03.2003
 * @see PlatformTransactionManager
 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
 * @see org.springframework.transaction.interceptor.TransactionInterceptor#currentTransactionStatus
 * @see #setRollbackOnly
 * @version $Id: TransactionStatus.java,v 1.2 2003-11-28 10:07:50 johnsonr Exp $
 */
public class TransactionStatus {

	private Object transaction = null;

	private boolean newTransaction = false;

	private boolean rollbackOnly = false;
	
	private boolean debug;

	/**
	 * Create a new TransactionStatus instance.
	 * @param transaction underlying transaction object,
	 * e.g. a JTA UserTransaction
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 * @param debug should debug logging be enabled for the handling of this transaction?
	 * Caching it in here can prevent repeated calls to ask the logging system wheter
	 * debug logging should be enabled.
	 */
	public TransactionStatus(Object transaction, boolean newTransaction, boolean debug) {
		this.transaction = transaction;
		this.newTransaction = newTransaction;
		this.debug = debug;
	}
	
	/**
	 * Create a new TransactionStatus instance without debug logging.
	 * @param transaction underlying transaction object,
	 * e.g. a JTA UserTransaction
	 * @param newTransaction if the transaction is new,
	 * else participating in an existing transaction
	 */
	public TransactionStatus(Object transaction, boolean newTransaction) {
		this(transaction, newTransaction, false);
	}
	
	/**
	 * @return whether the progress of this transaction being debugged. This is used
	 * by AbstractPlatformTransactionManager as an optimization, to prevent repeated
	 * calls to logger.isDebugEnabled(). Not really intended for client code.
	 */
	public final boolean isDebugEnabled() {
		// TODO could consider opening this up so that it escalates logging level
		// on behalf of clients, as well as for the present efficiency concern.
		// Could also even provide a transaction log, to ensure that output all appeared
		// together
		return debug;
	}
	

	/**
	 * Return the underlying transaction object, e.g. a JTA UserTransaction.
	 */
	public Object getTransaction() {
		return transaction;
	}

	/**
	 * Return if the transaction is new,
	 * else participating in an existing transaction.
	 */
	public boolean isNewTransaction() {
		return (transaction != null && newTransaction);
	}

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback,
	 * proceeding with the normal applicaiton workflow though (i.e. no exception). 
	 * <p>For transactions managed by TransactionTemplate or TransactionInterceptor.
	 * An alternative way to trigger a rollback is throwing an application exception.
	 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn
	 */
	public void setRollbackOnly() {
		this.rollbackOnly = true;
	}

	/**
	 * Return if the transaction has been set rollback-only.
	 */
	public boolean isRollbackOnly() {
		return rollbackOnly;
	}

}
