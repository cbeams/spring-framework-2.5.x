/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.datasource;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Fatal exception thrown when we can't connect to an RDBMS using JDBC.
 * @author Rod Johnson
 */
public class CannotGetJdbcConnectionException extends DataAccessResourceFailureException {

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param ex root cause from data access API in use
	 */
	public CannotGetJdbcConnectionException(Throwable ex) {
		super("Could not get JDBC connection", ex);
	}

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param msg message
	 * @param ex root cause from data access API in use
	 */
	public CannotGetJdbcConnectionException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
