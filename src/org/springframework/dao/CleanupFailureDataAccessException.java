/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Exception thrown when we couldn't cleanup after a data
 * access operation, but the actual operation went OK.
 * For example, this exception or a subclass might be thrown if a JDBC Connection
 * couldn't be closed after it had been used successfully.
 * @author Rod Johnson
 */
public class CleanupFailureDataAccessException extends DataAccessException {

	/**
	 * Constructor for CleanupFailureDataAccessException.
	 * @param msg Message
	 * @param ex Root cause from the underlying data access API,
	 * such as JDBC
	 */
	public CleanupFailureDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
