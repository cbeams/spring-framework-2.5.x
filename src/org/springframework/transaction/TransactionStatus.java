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
 * @version $Id: TransactionStatus.java,v 1.4 2004-01-20 10:41:09 jhoeller Exp $
 */
public interface TransactionStatus {

	/**
	 * Return if the transaction is new,
	 * else participating in an existing transaction.
	 */
	boolean isNewTransaction();

	/**
	 * Set the transaction rollback-only. This instructs the transaction manager
	 * that the only possible outcome of the transaction may be a rollback,
	 * proceeding with the normal applicaiton workflow though (i.e. no exception). 
	 * <p>For transactions managed by TransactionTemplate or TransactionInterceptor.
	 * An alternative way to trigger a rollback is throwing an application exception.
	 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction
	 * @see org.springframework.transaction.interceptor.TransactionAttribute#rollbackOn
	 */
	void setRollbackOnly();

	/**
	 * Return if the transaction has been set rollback-only.
	 */
	public boolean isRollbackOnly();

}
