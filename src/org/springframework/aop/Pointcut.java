/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

/**
 * Core Spring pointcut abstraction. A pointcut is composed of ClassFilters and MethodMatchers.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations.
 * @author Rod Johnson
 * @version $Id: Pointcut.java,v 1.2 2003-11-21 22:45:09 jhoeller Exp $
 */
public interface Pointcut {

	ClassFilter getClassFilter();
	
	MethodMatcher getMethodMatcher();
	
	// could add getFieldMatcher() without breaking most existing code
	
	
	/**
	 * Canonical instance that matches everything.
	 */
	Pointcut TRUE = new Pointcut() {

		public ClassFilter getClassFilter() {
			return ClassFilter.TRUE;
		}

		public MethodMatcher getMethodMatcher() {
			return MethodMatcher.TRUE;
		}

		public String toString() {
			return "Pointcut.TRUE";
		}
	};
	

}
