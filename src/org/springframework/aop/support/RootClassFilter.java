/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;


/** 
 * Simple ClassFilter implementation that passes classes (and optionally subclasses)
 */
public class RootClassFilter implements ClassFilter {
	
	private Class clazz;
	
	// TODO inheritance
	
	public RootClassFilter(Class clazz) {
		this.clazz = clazz;
	}

	/**
	 * @see org.springframework.aop.pointcut.ClassMatcher#canMatch(java.lang.Class)
	 */
	public boolean matches(Class candidate) {
		return clazz.isAssignableFrom(candidate);
	}
	

}
