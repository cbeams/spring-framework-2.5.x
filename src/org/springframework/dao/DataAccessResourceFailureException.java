/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Data access exception thrown when a resource fails completely:
 * for example, if we can't connect to a database using JDBC.
 * @author Rod Johnson
 * @version $Id: DataAccessResourceFailureException.java,v 1.4 2003-11-23 22:41:43 jhoeller Exp $
 */
public class DataAccessResourceFailureException extends DataAccessException {

	/**
	 * Constructor for ResourceFailureDataAccessException.
	 * @param msg message
	 */
	public DataAccessResourceFailureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for ResourceFailureDataAccessException.
	 * @param msg message
	 * @param ex root cause from data access API in use
	 */
	public DataAccessResourceFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
