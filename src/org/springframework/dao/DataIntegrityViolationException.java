/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.dao;

/**
 * Exception thrown when an attempt to insert or update data
 * results in violation of an integrity constraint. Note that this
 * is not purely a relational concept; unique primary keys are
 * required by most database types.
 * @author Rod Johnson
 * @version $Id: DataIntegrityViolationException.java,v 1.2 2003-11-02 12:53:06 johnsonr Exp $
 */
public class DataIntegrityViolationException extends DataAccessException {

	/**
	 * Constructor for DataIntegrityViolationException.
	 * @param msg mesg
	 * @param ex root cause
	 */
	public DataIntegrityViolationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
