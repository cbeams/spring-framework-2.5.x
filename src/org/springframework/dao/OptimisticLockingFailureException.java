/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Exception thrown on an optimistic locking violation. This exception will
 * be thrown either by O/R mapping tools or by custom DAO implementations.
 * @author Rod Johnson
 * @version $Id: OptimisticLockingFailureException.java,v 1.4 2003-11-02 12:53:05 johnsonr Exp $
 */
public class OptimisticLockingFailureException extends DataAccessException {

	public OptimisticLockingFailureException(String msg) {
		super(msg);
	}

	public OptimisticLockingFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
