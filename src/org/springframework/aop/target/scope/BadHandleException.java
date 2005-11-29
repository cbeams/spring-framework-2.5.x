package org.springframework.aop.target.scope;

import org.springframework.core.NestedRuntimeException;

/**
 * Superclass of exceptions thrown when a caller tries
 * to reconnect to a scoped object with an invalid handle.
 * @author Rod Johnson
 * @since 1.3
 */
public abstract class BadHandleException extends NestedRuntimeException {

	public BadHandleException(String msg) {
		super(msg);
	}
	
	public BadHandleException(String msg, Throwable t) {
		super(msg, t);
	}

}
