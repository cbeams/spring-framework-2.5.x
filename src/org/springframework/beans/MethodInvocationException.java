package org.springframework.beans;

import java.beans.PropertyChangeEvent;

/**
 * Thrown when a method getter or setter throws an exception,
 * analogous to an InvocationTargetException.
 * @author Rod Johnson
 * @version $Revision: 1.4 $
 */
public class MethodInvocationException extends PropertyAccessException {

	/**
	 * Constructor to use when an exception results from a PropertyChangeEvent.
	 * @param ex Throwable raised by invoked method
	 * @param propertyChangeEvent PropertyChangeEvent that resulted in an exception
	 */
	public MethodInvocationException(Throwable ex, PropertyChangeEvent propertyChangeEvent) {
		super("Property '" + propertyChangeEvent.getPropertyName() + "' threw exception", propertyChangeEvent, ex);
	}

	/**
	 * Constructor to use when an exception results from a method invocation,
	 * and there is no PropertyChangeEvent.
	 * @param ex Throwable raised by invoked method
	 * @param methodName name of the method we were trying to invoke
	 */
	public MethodInvocationException(Throwable ex, String methodName) {
		super("Method '" + methodName + "' threw exception", null, ex);
	}

	public String getErrorCode() {
		return "methodInvocation";
	}

}
