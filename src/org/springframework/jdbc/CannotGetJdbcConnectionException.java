/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Fatal exception thrown when we can't connect to an RDBMS using JDBC.
 * @author Rod Johnson
 */
public class CannotGetJdbcConnectionException extends DataAccessResourceFailureException {

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param msg message
	 * @param ex SQLException root cause
	 */
	public CannotGetJdbcConnectionException(String msg, SQLException ex) {
		super(msg, ex);
	}

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param msg message
	 * @param ex ClassNotFoundException root cause
	 */
	public CannotGetJdbcConnectionException(String msg, ClassNotFoundException ex) {
		super(msg, ex);
	}

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param msg message
	 * @param ex NamingException root cause
	 */
	public CannotGetJdbcConnectionException(String msg, NamingException ex) {
		super(msg, ex);
	}

}
