/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

/**
 * Exception thrown when a bean depends on other beans or
 * simple properties that were not specified in the bean factory definition,
 * although dependency checking was enabled
 * @author Rod Johnson
 * @since September 3, 2003
 * @see org.springframework.beans.factory.FactoryBean
 * @version $Id: UnsatisfiedDependencyException.java,v 1.2 2003-09-06 11:21:38 johnsonr Exp $
 */
public class UnsatisfiedDependencyException extends BeanDefinitionStoreException {

	/**
	 * @param name
	 * @param propertyName
	 * @param string
	 */
	public UnsatisfiedDependencyException(String beanName, String propertyName, String message) {
		super("Bean with name '" + beanName + "' has an unsatisfied dependency expressed through property '" + 
				propertyName + "': set this property value or disable dependency checking for this bean:" +				"detail message=[" + message + "]", null);
	}

	public UnsatisfiedDependencyException(String beanName, String propertyName) {
		super("Bean with name '" + beanName + "' has an unsatisfied dependency expressed through property '" + 
			propertyName + "': set this property value or disable dependency checking for this bean", null);
	}

}
