package org.springframework.beans;

import java.beans.PropertyChangeEvent;

/**
 * Superclass for exceptions related to a property access,
 * such as type mismatch or invocation target exception.
 * @author Rod Johnson
 * @version $Revision: 1.2 $
 */
public abstract class PropertyAccessException extends BeansException {

	private PropertyChangeEvent propertyChangeEvent;

	public PropertyAccessException(String msg, PropertyChangeEvent propertyChangeEvent, Throwable t) {
		super(msg, t);
		this.propertyChangeEvent = propertyChangeEvent;
	}

	/**
	 * Return the PropertyChangeEvent that resulted in the problem.
	 */
	public PropertyChangeEvent getPropertyChangeEvent() {
		return propertyChangeEvent;
	}

}
