/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.SQLException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Exception thrown when we can't classify a SQLException into 
 * one of our generic data access exceptions
 * @see org.springframework.dao
 * @author Rod Johnson
 * @version $Id: UncategorizedSQLException.java,v 1.1.1.1 2003-08-14 16:20:29 trisberg Exp $
 */
public class UncategorizedSQLException extends UncategorizedDataAccessException {
	
	/** SQL that led to the problem */
	private final String sql;

	/**
	 * Constructor for ConnectionFactoryException.
	 * @param mesg message
	 * @param sql SQL we were tring to execute
	 * @param ex SQLException
	 */
	public UncategorizedSQLException(String mesg, String sql, SQLException ex) {
		super(mesg, ex);
		this.sql = sql;
	}
	
	/**
	 * Return the underlying SQLException
	 * @return the underlying SQLException
	 */
	public SQLException getSQLException() {
		return (SQLException) getRootCause();
	}
	
	/**
	 * Return the SQL that led to the problem
	 * @return the SQL that led to the problem
	 */
	public String getSql() {
		return sql;
	}

}
