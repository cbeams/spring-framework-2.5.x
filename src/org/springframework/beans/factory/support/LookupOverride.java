/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * Represents an override of a method that looks up
 * an object in the same IoC context. 
 * <p>
 * Methods eligible for lookup override must not have arguments.
 * @author Rod Johnson
 * @version $Id: LookupOverride.java,v 1.1 2004-06-23 21:14:05 johnsonr Exp $
 */
public class LookupOverride extends MethodOverride {
	
	private final String beanName;

	/**
	 * Construct a new LookupOverride
	 * @param methodName name of the method to override. This
	 * method must have no arguments.
	 * @param beanName name of the bean in the current BeanFactory
	 * or ApplicationContext that the overriden method should return.
	 */
	public LookupOverride(String methodName, String beanName) {
		super(methodName);
		this.beanName = beanName;
	}
	
	/**
	 * @return the name of the bean that should be returned
	 * by this method
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Doesn't allow for overloading, so matching method name
	 * is fine
	 * @see org.springframework.beans.factory.support.MethodOverride#matches(java.lang.reflect.Method)
	 */
	public boolean matches(Method m) {
		return m.getName().equals(getMethodName());
	}

	public String toString() {
		return "LookupOverride for method '" + getMethodName() + "'; will return bean '" + beanName + "'";
	}
}
