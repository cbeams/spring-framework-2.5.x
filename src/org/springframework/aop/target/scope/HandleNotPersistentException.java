package org.springframework.aop.target.scope;

/**
 * Exception thrown when a user tries to reconnect
 * from a null handle or a handle that is not persistent.
 * @author Rod Johnson
 * @since 1.3
 */
public class HandleNotPersistentException extends BadHandleException {

	public HandleNotPersistentException(Handle h) {
		super("Handle with name '" + h.getTargetBeanName() + " is not persistent");
	}
	
	/**
	 * Constructor for use when handle is null
	 */
	public HandleNotPersistentException() {
		super("Cannot reconnect from a null handle");
	}

}
