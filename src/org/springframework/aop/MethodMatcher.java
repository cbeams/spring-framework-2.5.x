/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * Part of a Pointcut.
 * 
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: MethodMatcher.java,v 1.1 2003-11-11 18:31:51 johnsonr Exp $
 */
public interface MethodMatcher {
	
	
	boolean matches(Method m, Class targetClass);
	
	/**
	 * Must a final call be made on the matches(Method, Class, Object[]) method at runtime
	 * even if the 2-arg matches method returns true?
	 * Can be invoked when an AOP proxy is created, and need not be invoked
	 * again before each method invocation,
	 * @return
	 * TODO could be pulled up into a Matcher superinterface to apply to
	 * fields also
	 */
	boolean isRuntime();
	
	/**
	 * Invoked only if the 2-arg matches method returns true for the given method
	 * and target class, and if the isRuntime() method returns true.
	 * Invoked at the time of method invocation.
	 * @param m
	 * @param targetClass
	 * @param args
	 * @return
	 */
	boolean matches(Method m, Class targetClass, Object[] args);
	
	
	/**
	 * Canonical instance that matches all methods
	 */
	public static final MethodMatcher TRUE = new MethodMatcher() {
		public boolean isRuntime() {
			return false;
		}

		public boolean matches(Method m, Class targetClass) {
			return true;
		}

		public boolean matches(Method m, Class targetClass, Object[] args) {
			// Should never be invoked as isRuntime returns false
			throw new UnsupportedOperationException();
		}
	};

}
