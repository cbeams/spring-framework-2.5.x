/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.datasource;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Fatal exception thrown when we can't connect to an RDBMS
 * using JDBC.
 * @author Rod Johnson
 */
public class CannotGetJdbcConnectionException extends DataAccessResourceFailureException {

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param s message
	 * @param ex root cause
	 */
	public CannotGetJdbcConnectionException(String s, Throwable ex) {
		super(s, ex);
	}

}
