/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * Part of a Pointcut. Checks whether the target method is eligible
 * for advice. <br>
 * A MethodMatcher may be evaluated <b>statically</b> or at <b>runtime</b> (dynamically).
 * Static matching involves method and (possibly) method attributes. Dynamic matching
 * also makes arguments for a particular call available, and any effects of running
 * previous advice applying to the joinpoint.
 * <br>
 * If an implementation returns false in its isRuntime() method, evaluation can be performed
 * statically, and the result will be the same for all invocations of this method,
 * whatever their arguments. If the isRuntime() method returns false, the 3-arg matches()
 * method will never be invoked.
 * <br>
 * If an implementation returns true in its 2-arg matches() method, and
 * its isRuntime() method returns true, the 3-argument matches()
 * method will be invoked <i>immediately before each potential execution of the related advice</i>,
 * to decide whether the advice should run. All previous advice, such as earlier interceptors
 * in an interceptor chain, will have run, so any state changes they have produced in parameters
 * or ThreadLocal state, will be available at the time of evaluation.
 * <br>
 * 
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: MethodMatcher.java,v 1.2 2003-11-17 11:17:25 johnsonr Exp $
 */
public interface MethodMatcher {
	
	/**
	 * Perform static checking. If this returns false, or if the isRuntime() method
	 * returns false, no runtime check will be made.
	 * @param m candidate method
	 * @param targetClass target class (may be null, in which case the candidate
	 * class must be taken to be the method's declaring class)
	 * @return whether or not this method matches statically
	 */
	boolean matches(Method m, Class targetClass);
	
	/**
	 * Is this MethodMatcher dynamic?
	 * Must a final call be made on the matches(Method, Class, Object[]) method at runtime
	 * even if the 2-arg matches method returns true?
	 * Can be invoked when an AOP proxy is created, and need not be invoked
	 * again before each method invocation,
	 * @return whether or not a runtime matche via the 3-arg matches() method is
	 * required if static matching passed.
	 * <br>Note: could be pulled up into a Matcher superinterface to apply to
	 * fields also
	 */
	boolean isRuntime();
	
	/**
	 * Is there a runtime (dynamic) match for this method, which must have matched
	 * statically. This method is
	 * invoked only if the 2-arg matches method returns true for the given method
	 * and target class, and if the isRuntime() method returns true.
	 * Invoked immediately before potentially running of the advice, after any
	 * advice earlier in the advice chain has run.
	 * @param m candidate method
	 * @param targetClass target class
	 * @param args arguments to the method
	 * @return whether there's a runtime match
	 * @see MethodMatcher#matches(Method, Class)
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
