/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;


/**
 * Filter that restricts matching of a pointcut or introduction to
 * a given set of target classes.
 * <br>
 * Can be used as part of a pointcut, or for the entire 
 * targeting of an IntroductionAdvice.
 * @author Rod Johnson
 * @see org.springframework.aop.Pointcut
 * @version $Id: ClassFilter.java,v 1.2 2003-11-17 11:02:43 johnsonr Exp $
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
	
	/**
	 * Should the pointcut apply to the given interface or target class?
	 * @param clazz candidate target class
	 * @return whether the advice should apply to this candidate target class
	 */
	boolean matches(Class clazz);

}
