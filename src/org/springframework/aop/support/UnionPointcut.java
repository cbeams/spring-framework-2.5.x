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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;

/**
 * Pointcut unions are tricky, because we can't just
 * OR the MethodMatchers: we need to check that each MethodMatcher's
 * ClassFilter was happy as well.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */
class UnionPointcut implements Pointcut, Serializable {

	private final Pointcut a;

	private final Pointcut b;

	private MethodMatcher methodMatcher;


	public UnionPointcut(Pointcut a, Pointcut b) {
		Assert.notNull(a, "'a' cannot be null.");
		Assert.notNull(b, "'b' cannot be null.");
		this.a = a;
		this.b = b;
		this.methodMatcher = new PointcutUnionMethodMatcher();
	}

	public ClassFilter getClassFilter() {
		return ClassFilters.union(this.a.getClassFilter(), this.b.getClassFilter());
	}

	public MethodMatcher getMethodMatcher() {
		// Complicated: we need to consider both class filter and method matcher.
		return this.methodMatcher;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof UnionPointcut)) {
			return false;
		}

		UnionPointcut that = (UnionPointcut) obj;

		return (this.a.equals(that.a) && this.b.equals(that.b));
	}

	public int hashCode() {
		int code = 17;
		code = 37 * code + this.a.hashCode();
		code = 37 * code + this.b.hashCode();
		return code;
	}

	private class PointcutUnionMethodMatcher implements MethodMatcher, Serializable {

		public boolean matches(Method method, Class targetClass) {
			return (a.getClassFilter().matches(targetClass) && a.getMethodMatcher().matches(method, targetClass)) ||
				 (b.getClassFilter().matches(targetClass) && b.getMethodMatcher().matches(method, targetClass));
		}

		public boolean isRuntime() {
			return a.getMethodMatcher().isRuntime() || b.getMethodMatcher().isRuntime();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			// 2-arg matcher will already have run, so we don't need to do class filtering again.
			return a.getMethodMatcher().matches(method, targetClass, args) ||
					b.getMethodMatcher().matches(method, targetClass, args);
		}
	}

}
