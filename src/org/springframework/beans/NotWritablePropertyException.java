package org.springframework.beans;

/**
 * Exception thrown on an attempt to set the value of a property
 * that isn't writable, because there's no setter method.
 * @author Rod Johnson
 */
public class NotWritablePropertyException extends BeansException {

	/**
	 * Creates new NotWritablePropertyException.
	 */
	public NotWritablePropertyException(String propertyName, Class beanClass) {
		super("Property [" + propertyName + "] is not writable in bean class [" + beanClass.getName() + "]");
	}

}
