/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;


/**
 * Static methods useful for composing Pointcuts.
 * A MethodMatcher may be evaluated statically (based on Method and target class)
 * or need further evaluation dynamically (based on arguments at the time of
 * method invocation).
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: MethodMatchers.java,v 1.1 2003-11-16 12:54:58 johnsonr Exp $
 */
public abstract class MethodMatchers {
	
	
	public static MethodMatcher union(MethodMatcher a, MethodMatcher b) {
		return new UnionMethodMatcher(a, b);
	}
	public static MethodMatcher intersection(MethodMatcher a, MethodMatcher b) {
		return new IntersectionMethodMatcher(a, b);
	}
	
	
	private static class UnionMethodMatcher implements MethodMatcher {
		
		private MethodMatcher a;
		private MethodMatcher b;
		
		public UnionMethodMatcher(MethodMatcher a, MethodMatcher b) {
			this.a = a;
			this.b = b;
		}

		public boolean matches(Method m, Class targetClass) {
			return a.matches(m, targetClass) || b.matches(m, targetClass);
		}
		
		public boolean isRuntime() {
			return a.isRuntime() || b.isRuntime();
		}
		
		public boolean matches(Method m, Class targetClass, Object[] args) {
			return a.matches(m, targetClass, args) || b.matches(m, targetClass, args);
		}
		
	}
	
	private static class IntersectionMethodMatcher implements MethodMatcher {
		
		private MethodMatcher a;
		private MethodMatcher b;
	
		public IntersectionMethodMatcher(MethodMatcher a, MethodMatcher b) {
			this.a = a;
			this.b = b;
		}


		public boolean matches(Method m, Class targetClass) {
			return a.matches(m, targetClass) && b.matches(m, targetClass);
		}
		
		public boolean isRuntime() {
			return a.isRuntime() || b.isRuntime();
		}
		
		public boolean matches(Method m, Class targetClass, Object[] args) {
			// Because a dynamic intersection may be composed of a static and dynamic part,
			// we must avoid calling the 3-arg matches method on a dynamic matcher, as
			// it will probably be an unsupported operation.
			boolean aMatches = a.isRuntime() ? a.matches(m, targetClass, args) : a.matches(m, targetClass);
			boolean bMatches = b.isRuntime() ? b.matches(m, targetClass, args) : b.matches(m, targetClass);
			return  aMatches && bMatches;
		}
	
	}

}
