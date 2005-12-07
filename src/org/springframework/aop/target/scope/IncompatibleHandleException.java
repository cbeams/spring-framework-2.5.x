package org.springframework.aop.target.scope;

/**
 * Exception thrown when a user tries to reconnect
 * from an incompatible handle.
 * @author Rod Johnson
 * @since 2.0
 */
public class IncompatibleHandleException extends BadHandleException {

	public IncompatibleHandleException(Handle handle, String msg) {
		super("Handle [" + handle + "] is incompatible: " + msg);
	}

}
