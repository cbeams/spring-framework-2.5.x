/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Can be used as part of a pointcut, or for the entire 
 * targeting of an IntroductionAdvice.
 */
public interface ClassFilter {
	
	/**
	 * Canonical instance of a ClassFilter that matches all classes
	 */
	public final static ClassFilter TRUE = new ClassFilter() {
		public boolean matches(Class clazz) {
			return true;
		}
	};
	
	boolean matches(Class clazz);

}
