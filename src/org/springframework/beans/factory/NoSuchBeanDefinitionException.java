/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Exception thrown when a BeanFactory is asked for a bean
 * instance name for which it cannot find a definition.
 * @author Rod Johnson
 * @version $Id: NoSuchBeanDefinitionException.java,v 1.3 2003-11-13 11:51:25 jhoeller Exp $
 */
public class NoSuchBeanDefinitionException extends BeansException {

	/** Name of the missing bean */
	private String beanName;

	/** Required bean type */
	private Class beanType;

	/**
	 * Create new <code>NoSuchBeanDefinitionException</code>.
	 * @param name the name of the missing bean
	 * @param message further, detailed message describing the problem.
	 */
	public NoSuchBeanDefinitionException(String name, String message) {
		super("No bean named '" + name + "' is defined: " + message, null);
		this.beanName = name;
	}

	/**
	 * Create new <code>NoSuchBeanDefinitionException</code>.
	 * @param type required type of bean
	 * @param message further, detailed message describing the problem.
	 */
	public NoSuchBeanDefinitionException(Class type, String message) {
		super("No unique bean of type [" + type.getName() + "] is defined: " + message, null);
		this.beanType = type;
	}

	/**
	 * Return the name of the missing bean.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Return the required type of bean.
	 */
	public Class getBeanType() {
		return beanType;
	}

}
