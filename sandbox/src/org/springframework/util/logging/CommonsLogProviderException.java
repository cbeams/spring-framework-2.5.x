/*
 * Created on Nov 6, 2004
 */
package org.springframework.util.logging;

import org.springframework.core.NestedRuntimeException;

/**
 * @author robh
 *
 */
public class CommonsLogProviderException extends NestedRuntimeException {

	/**
	 * @param msg
	 */
	public CommonsLogProviderException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param msg
	 * @param ex
	 */
	public CommonsLogProviderException(String msg, Throwable ex) {
		super(msg, ex);
		// TODO Auto-generated constructor stub
	}

}
