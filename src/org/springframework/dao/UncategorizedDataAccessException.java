/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;


/**
 * Normal superclass when we can't distinguish anything
 * more specific than "something went wrong with the
 * underlying resource": for example, a SQLException from JDBC we
 * can't pinpoint more precisely.
 * @author Rod Johnson
 * @version $Id: UncategorizedDataAccessException.java,v 1.2 2003-11-02 12:53:04 johnsonr Exp $
 */
public abstract class UncategorizedDataAccessException extends DataAccessException {

	/**
	 * Constructor for UncategorizedDataAccessException.
	 * @param msg description of failure
	 * @param ex exception thrown by underlying data access API
	 */
	public UncategorizedDataAccessException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
