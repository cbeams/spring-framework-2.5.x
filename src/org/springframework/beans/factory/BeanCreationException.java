/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown when a BeanFactory encounters an error when
 * attempting to create a bean from a bean definition.
 * @author Juergen Hoeller
 */
public class BeanCreationException extends FatalBeanException {

	public BeanCreationException(String msg) {
		super(msg);
	}

	public BeanCreationException(String msg, Throwable ex) {
		super(msg, ex);
	}

	public BeanCreationException(String resourceDescription, String beanName, String msg) {
		this(resourceDescription, beanName, msg, null);
	}

	public BeanCreationException(String resourceDescription, String beanName, String msg, Throwable ex) {
		super("Error creating bean with name '" + beanName + "' defined in " + resourceDescription + ": " + msg, ex);
	}

}
