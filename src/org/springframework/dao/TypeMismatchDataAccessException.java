/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Exception thrown on mismatch between Java type and database type:
 * for example on an attempt to set an object of the wrong type
 * in an RDBMS column.
 * @author Rod Johnson
 * @version $Id: TypeMismatchDataAccessException.java,v 1.2 2003-11-02 12:53:03 johnsonr Exp $
 */
public class TypeMismatchDataAccessException extends DataAccessException {

	/**
	 * Constructor for TypeMismatchDataAccessException.
	 * @param msg mesg
	 * @param ex root cause
	 */
	public TypeMismatchDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
