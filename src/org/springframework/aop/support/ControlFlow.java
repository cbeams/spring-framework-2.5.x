/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.support;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ControlFlow.java,v 1.2 2003-12-15 14:39:29 johnsonr Exp $
 */
public interface ControlFlow {
	
	boolean under(Class clazz);
	/**
	 * Matches whole method name
	 * @param clazz
	 * @param methodName
	 * @return
	 */
	boolean under(Class clazz, String methodName);
	
	boolean underToken(String token);
}