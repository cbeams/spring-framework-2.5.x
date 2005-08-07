package org.springframework.aop.target.scope;

/**
 * Exception thrown when a user tries to reconnect
 * from an incompatible handle.
 * @author Rod Johnson
 * @since 1.3
 */
public class IncompatibleHandleException extends BadHandleException {

	public IncompatibleHandleException(Handle h, String targetBeanName) {
		super("Handle with name '" + h.getTargetBeanName() + " is incompatible: " +
				"Expected targetBeanName was '" + targetBeanName + "'");
	}

}
