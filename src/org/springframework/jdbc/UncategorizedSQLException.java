/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc;

import java.sql.SQLException;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Exception thrown when we can't classify a SQLException into 
 * one of our generic data access exceptions.
 * @author Rod Johnson
 * @version $Id: UncategorizedSQLException.java,v 1.1 2003-12-05 17:02:36 jhoeller Exp $
 */
public class UncategorizedSQLException extends UncategorizedDataAccessException {
	
	/** SQL that led to the problem */
	private final String sql;

	/**
	 * Constructor for ConnectionFactoryException.
	 * @param msg message
	 * @param sql SQL we were tring to execute
	 * @param ex SQLException
	 */
	public UncategorizedSQLException(String msg, String sql, SQLException ex) {
		super(msg, ex);
		this.sql = sql;
	}
	
	/**
	 * Return the underlying SQLException.
	 */
	public SQLException getSQLException() {
		return (SQLException) getRootCause();
	}
	
	/**
	 * Return the SQL that led to the problem.
	 */
	public String getSql() {
		return sql;
	}

}
