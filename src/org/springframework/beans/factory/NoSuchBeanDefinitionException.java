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
 * @version $Id: NoSuchBeanDefinitionException.java,v 1.2 2003-10-23 10:21:02 jhoeller Exp $
 */
public class NoSuchBeanDefinitionException extends BeansException {

	/** Name of the missing bean */
	private final String name;

	/**
	 * Creates new <code>NoSuchBeanDefinitionException</code>..
	 * @param name the name of the missing bean
	 * @param message further, detailed message describing the problem.
	 */
	public NoSuchBeanDefinitionException(String name, String message) {
		super("No bean named '" + name + "' is defined: " + message, null);
		this.name = name;
	}

	/**
	 * Return the name of the missing bean.
	 * @return the name of the missing bean
	 */
	public String getBeanName() {
		return name;
	}

}
