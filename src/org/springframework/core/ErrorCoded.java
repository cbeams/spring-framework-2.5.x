
package org.springframework.core;

/**
 * Interface that can be implemented by exceptions etc that are error coded.
 * The error code is a String, rather than a number, so it can be given
 * user-readable values, such as "object.failureDescription".
 * These codes will be resolved by a cMessageSource object.
 *
 * <p>This interface is necessary because both runtime and checked
 * exceptions are useful, and they cannot share a common,
 * framework-specific, superclass.
 *
 * @author Rod Johnson
 * @version $Id: ErrorCoded.java,v 1.1.1.1 2003-08-14 16:20:24 trisberg Exp $
 * @see org.springframework.context.MessageSource
 */
public interface ErrorCoded {
	
	/**
	 * Return the error code associated with this failure. 
	 * The GUI can render this anyway it pleases, allowing for Int8ln etc.
	 * @return a String error code associated with this failure,
	 * or null if not error-coded
	 */
	String getErrorCode();

}

