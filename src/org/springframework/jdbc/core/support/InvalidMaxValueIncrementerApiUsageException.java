/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core.support;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Exception thrown on incorrect usage of the API, such as failing
 * to provide incrementerName or columnName before usage.
 *
 * <p>This represents a problem in our Java data access framework,
 * not the underlying data store.
 *
 * @author Thomas Risberg
 * @version $Id: InvalidMaxValueIncrementerApiUsageException.java,v 1.3 2003-11-03 15:06:43 johnsonr Exp $
 */
public class InvalidMaxValueIncrementerApiUsageException extends InvalidDataAccessApiUsageException {

	/**
	 * Constructor for InvalidMaxValueIncrementerApiUsageException.
	 * @param msg message
	 */
	public InvalidMaxValueIncrementerApiUsageException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for InvalidMaxValueIncrementerApiUsageException.
	 * @param msg message
	 * @param ex root cause, from an underlying API such as JDBC
	 */
	public InvalidMaxValueIncrementerApiUsageException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
