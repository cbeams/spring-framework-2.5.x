/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.mvc;

import javax.servlet.ServletException;

/**
 * Exception thrown when a Controller requires a session for the
 * current method. This exception is normally raised by framework
 * code, but may sometimes be handled by application code.
 * @author Rod Johnson
 */
public class SessionRequiredException extends ServletException {

	public SessionRequiredException(String msg) {
		super(msg);
	}

	public SessionRequiredException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
