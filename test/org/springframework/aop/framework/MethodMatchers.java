/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;


/**
 * Static methods useful for composing Pointcuts.
 * A MethodMatcher may be evaluated statically (based on Method and target class)
 * or need further evaluation dynamically (based on arguments at the time of
 * method invocation).
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: MethodMatchers.java,v 1.1 2003-11-11 18:31:53 johnsonr Exp $
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
			return a.matches(m, targetClass, args) && b.matches(m, targetClass, args);
		}
	
	}

}
