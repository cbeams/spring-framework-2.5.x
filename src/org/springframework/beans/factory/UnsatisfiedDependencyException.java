/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

/**
 * Exception thrown when a bean depends on other beans or simple properties that were not
 * specified in the bean factory definition, although dependency checking was enabled.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since September 3, 2003
 * @version $Id: UnsatisfiedDependencyException.java,v 1.6 2004-03-01 09:47:11 jhoeller Exp $
 */
public class UnsatisfiedDependencyException extends BeanDefinitionStoreException {

	public UnsatisfiedDependencyException(String beanName, int ctorArgIndex, Class ctorArgType, String msg) {
		super("Bean with name '" + beanName + "' has an unsatisfied dependency expressed through " +
					"constructor argument with index " + ctorArgIndex + " of type [" + ctorArgType.getName() + "]" +
					(msg != null ? ": " + msg : ""));
	}

	public UnsatisfiedDependencyException(String beanName, String propertyName, String msg) {
		super("Bean with name '" + beanName + "' has an unsatisfied dependency expressed through property '" +
					propertyName + "': set this property value or disable dependency checking for this bean" +
					(msg != null ? ": " + msg : ""));
	}

}
