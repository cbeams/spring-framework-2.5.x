package org.springframework.transaction;

/**
 * Exception thrown when the existence or non-existence of a transaction
 * amounts to an illegal state according to the transaction propagation
 * behavior that applies.
 * @author Juergen Hoeller
 * @since 21.01.2004
 */
public class IllegalTransactionStateException extends CannotCreateTransactionException {

	public IllegalTransactionStateException(String msg) {
		super(msg);
	}

	public IllegalTransactionStateException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
