/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jdbc;

import java.sql.SQLWarning;

import org.springframework.dao.UncategorizedDataAccessException;

/**
 * Exception thrown when we're not ignoring warnings.
 *
 * <p>If such an exception is thrown, the operation completed,
 * so we will need to explicitly roll it back if we're not happy
 * on looking at the warning. We might choose to ignore (or merely log)
 * the warning and throw the exception away.
 *
 * @author Rod Johnson
 */
public class SQLWarningException extends UncategorizedDataAccessException {

	/**
	 * Constructor for ConnectionFactoryException.
	 * @param msg message
	 * @param ex JDBC warning
	 */
	public SQLWarningException(String msg, SQLWarning ex) {
		super(msg, ex);
	}
	
	/**
	 * Return the SQLWarning.
	 */
	public SQLWarning SQLWarning() {
		return (SQLWarning) getCause();
	}

}
