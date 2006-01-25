/*
 * Copyright 2002-2006 the original author or authors.
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
import org.springframework.util.Assert;

/**
 * Static methods useful for composing MethodMatchers. A MethodMatcher may be
 * evaluated statically (based on method and target class) or need further
 * evaluation dynamically (based on arguments at the time of method invocation).
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 11.11.2003
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
			Assert.notNull(a, "'a' cannot be null.");
			Assert.notNull(b, "'b' cannot be null.");
			this.a = a;
			this.b = b;
		}

		public boolean matches(Method method, Class targetClass) {
			return a.matches(method, targetClass) || b.matches(method, targetClass);
		}

		public boolean isRuntime() {
			return a.isRuntime() || b.isRuntime();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			return a.matches(method, targetClass, args) || b.matches(method, targetClass, args);
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof UnionMethodMatcher)) {
				return false;
			}

			UnionMethodMatcher that = (UnionMethodMatcher) obj;
			return (this.a.equals(that.a) && this.b.equals(that.b));
		}

		public int hashCode() {
			int code = 17;
			code = 37 * code + this.a.hashCode();
			code = 37 * code + this.b.hashCode();
			return code;
		}
	}


	private static class IntersectionMethodMatcher implements MethodMatcher, Serializable {

		private MethodMatcher a;
		private MethodMatcher b;

		private IntersectionMethodMatcher(MethodMatcher a, MethodMatcher b) {
			Assert.notNull(a, "'a' cannot be null.");
			Assert.notNull(b, "'b' cannot be null.");
			this.a = a;
			this.b = b;
		}

		public boolean matches(Method method, Class targetClass) {
			return a.matches(method, targetClass) && b.matches(method, targetClass);
		}

		public boolean isRuntime() {
			return a.isRuntime() || b.isRuntime();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			// Because a dynamic intersection may be composed of a static and dynamic part,
			// we must avoid calling the 3-arg matches method on a dynamic matcher, as
			// it will probably be an unsupported operation.
			boolean aMatches = a.isRuntime() ? a.matches(method, targetClass, args) : a.matches(method, targetClass);
			boolean bMatches = b.isRuntime() ? b.matches(method, targetClass, args) : b.matches(method, targetClass);
			return aMatches && bMatches;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof IntersectionMethodMatcher)) {
				return false;
			}

			IntersectionMethodMatcher that = (IntersectionMethodMatcher) obj;
			return (this.a.equals(that.a) && this.b.equals(that.b));
		}

		public int hashCode() {
			int code = 17;
			code = 37 * code + this.a.hashCode();
			code = 37 * code + this.b.hashCode();
			return code;
		}
	}

}
