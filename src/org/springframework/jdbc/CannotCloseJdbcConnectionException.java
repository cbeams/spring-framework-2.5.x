/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc;

import java.sql.SQLException;

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
	 * @param msg message
	 * @param ex SQLException root cause
	 */
	public CannotCloseJdbcConnectionException(String msg, SQLException ex) {
		super(msg, ex);
	}
	
}
