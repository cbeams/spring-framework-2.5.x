/*
 * Created on Jun 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.springframework.jdbc.support;

import org.springframework.dao.DataAccessException;

/**
 * @author trisberg
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CustomErrorCodeException extends DataAccessException {

	/**
	 * @param msg
	 */
	public CustomErrorCodeException(String msg) {
		super(msg);
	}

	/**
	 * @param msg
	 * @param ex
	 */
	public CustomErrorCodeException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
