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

package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;

/**
 * Static methods useful for composing MethodMatchers. A MethodMatcher may be
 * evaluated statically (based on method and target class) or need further
 * evaluation dynamically (based on arguments at the time of method invocation).
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
	
	
	private static class UnionMethodMatcher implements MethodMatcher, Serializable {
		
		private MethodMatcher a;
		private MethodMatcher b;
		
		private UnionMethodMatcher(MethodMatcher a, MethodMatcher b) {
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
	

	private static class IntersectionMethodMatcher implements MethodMatcher, Serializable {
		
		private MethodMatcher a;
		private MethodMatcher b;
	
		private IntersectionMethodMatcher(MethodMatcher a, MethodMatcher b) {
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
			return aMatches && bMatches;
		}
	}

}
