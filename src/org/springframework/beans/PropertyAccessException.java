package org.springframework.beans;

import java.beans.PropertyChangeEvent;

import org.springframework.core.ErrorCoded;

/**
 * Superclass for exceptions related to a property access,
 * such as type mismatch or invocation target exception.
 * @author Rod Johnson
 * @version $Revision: 1.3 $
 */
public abstract class PropertyAccessException extends BeansException implements ErrorCoded {

	private PropertyChangeEvent propertyChangeEvent;

	public PropertyAccessException(String msg, PropertyChangeEvent propertyChangeEvent) {
		super(msg);
		this.propertyChangeEvent = propertyChangeEvent;
	}

	public PropertyAccessException(String msg, PropertyChangeEvent propertyChangeEvent, Throwable ex) {
		super(msg, ex);
		this.propertyChangeEvent = propertyChangeEvent;
	}

	/**
	 * Return the PropertyChangeEvent that resulted in the problem.
	 */
	public PropertyChangeEvent getPropertyChangeEvent() {
		return propertyChangeEvent;
	}

}
