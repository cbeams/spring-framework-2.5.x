package org.springframework.web.bind;

import javax.servlet.ServletException;

/**
 * Fatal binding exception, thrown when we want to
 * treat binding exceptions as unrecoverable.
 * @author Rod Johnson
 */
public class ServletRequestBindingException extends ServletException {

	public ServletRequestBindingException(String msg) {
		super(msg);
	}

	public ServletRequestBindingException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
