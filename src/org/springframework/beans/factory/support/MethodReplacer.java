/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * Interface to be implemented by classes than can override
 * any method on an IoC-managed object.
 * @author Rod Johnson
 * @version $Id: MethodReplacer.java,v 1.1 2004-06-28 11:44:18 johnsonr Exp $
 */
public interface MethodReplacer {
	
	/**
	 * Reimplement the given method.
	 * @param o instance we're reimplementing the method for
	 * @param m method to reimplement
	 * @param args arguments
	 * @return return value for the method
	 */
	Object reimplement(Object o, Method m, Object[] args) throws Throwable;

}
