/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.web.flow;

public interface TransactionSynchronizer {

	/**
	 * Is given request participating in the active transaction of the model?
	 * @param clear indicates whether or not the transaction should end after
	 *        checking it
	 * @return True when the request is participating in the active transaction
	 *         of the model, false otherwise
	 */
	public boolean inTransaction(boolean clear);

	/**
	 * Assert that given request is participating in the active transaction of
	 * the model.
	 * @param clear indicates whether or not the transaction should end after
	 *        checking it
	 * @throws IllegalStateException The request is not participating in the
	 *         active transaction of the model or there is no transaction active
	 *         in the model
	 */
	public void assertInTransaction(boolean clear) throws IllegalStateException;

	/**
	 * Start a new transaction on this context.
	 */
	public void beginTransaction();

	/**
	 * End the active transaction on this context.
	 */
	public void endTransaction();

}