package org.springframework.transaction;

/**
 * Superclass for exceptions caused by inappropriate usage of 
 * a Spring transaction API.
 * @author Rod Johnson
 * @since 22-Mar-2003
 * @version $Revision: 1.1.1.1 $
 */
public class TransactionUsageException extends TransactionException {

	public TransactionUsageException(String msg) {
		super(msg);
	}

	public TransactionUsageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
