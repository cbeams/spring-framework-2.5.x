/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.datasource;

import org.springframework.dao.CleanupFailureDataAccessException;

/**
 * Exception thrown when we successfully executed a SQL statement,
 * but then failed to close the JDBC connection.
 * This results in a java.sql.SQLException, but application code can choose
 * to catch the exception to avoid the transaction being rolled back.
 * @author Rod Johnson
 */
public class CannotCloseJdbcConnectionException extends CleanupFailureDataAccessException {

	/**
	 * Constructor for CannotCloseJdbcConnectionException.
	 * @param ex root cause from data access API in use
	 */
	public CannotCloseJdbcConnectionException(Throwable ex) {
		super("Could not close JDBC connection", ex);
	}

	/**
	 * Constructor for CannotCloseJdbcConnectionException.
	 * @param msg message
	 * @param ex root cause from data access API in use
	 */
	public CannotCloseJdbcConnectionException(String msg, Throwable ex) {
		super(msg, ex);
	}
	
}
