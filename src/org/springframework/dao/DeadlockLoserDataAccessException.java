/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.dao;

/**
 * Generic exception thrown when the current process was
 * a deadlock loser, and its transaction rolled back.
 * @author Rod Johnson
 * @version $Id: DeadlockLoserDataAccessException.java,v 1.2 2003-11-02 12:53:06 johnsonr Exp $
 */
public class DeadlockLoserDataAccessException extends DataAccessException {

	/**
	 * Constructor for DeadlockLoserDataAccessException.
	 * @param msg mesg
	 * @param ex root cause
	 */
	public DeadlockLoserDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
