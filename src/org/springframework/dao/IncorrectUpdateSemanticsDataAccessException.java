/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Data access exception thrown when something unintended appears to have
 * happened with an update, but the transaction hasn't already been rolled back.
 * Thrown, for example, when we wanted to update 1 row in an RDBMS but actually
 * updated 3.
 * @author Rod Johnson
 * @version $Id: IncorrectUpdateSemanticsDataAccessException.java,v 1.3 2004-02-11 01:06:33 jhoeller Exp $
 */
public abstract class IncorrectUpdateSemanticsDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param msg message
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param msg message
	 * @param ex root cause from the underlying API, such as JDBC
	 */
	public IncorrectUpdateSemanticsDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
	/**
	 * Return whether data was updated.
	 * @return whether data was updated (as opposed to being incorrectly
	 * updated). If this method returns true, there's nothing to roll back.
	 */
	public abstract boolean getDataWasUpdated();

}
