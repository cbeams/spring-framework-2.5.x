/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
