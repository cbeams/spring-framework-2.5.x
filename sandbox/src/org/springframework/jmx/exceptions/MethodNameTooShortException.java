/*
 * Created on Jul 22, 2004
 */
package org.springframework.jmx.exceptions;

import org.springframework.core.NestedRuntimeException;

/**
 * @author robh
 */
public class MethodNameTooShortException extends NestedRuntimeException {

	public MethodNameTooShortException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MethodNameTooShortException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}