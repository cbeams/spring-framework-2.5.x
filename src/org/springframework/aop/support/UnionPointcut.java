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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Pointcut unions are tricky, because we can't just
 * OR the MethodMatchers: we need to check that each MethodMatcher's
 * ClassFilter was happy as well.
 * @author Rod Johnson
 */
class UnionPointcut implements Pointcut, Serializable {
	
	private final Pointcut a;

	private final Pointcut b;
	
	private MethodMatcher mm;

	public UnionPointcut(Pointcut a, Pointcut b) {
		this.a = a;
		this.b = b;
		this.mm = new PointcutUnionMethodMatcher();
	}

	/**
	 * @see org.springframework.aop.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return ClassFilters.union(a.getClassFilter(), b.getClassFilter());
	}

	/**
	 * @see org.springframework.aop.Pointcut#getMethodMatcher()
	 */
	public MethodMatcher getMethodMatcher() {
		// Complicated: we need to consider both class filter and method matcher
		return mm; 
	}
	
	private class PointcutUnionMethodMatcher implements MethodMatcher, Serializable {

		public boolean matches(Method m, Class targetClass) {
			return (a.getClassFilter().matches(targetClass) && a.getMethodMatcher().matches(m, targetClass)) || 
				 (b.getClassFilter().matches(targetClass) && b.getMethodMatcher().matches(m, targetClass));
		}
	
		public boolean isRuntime() {
			return a.getMethodMatcher().isRuntime() || b.getMethodMatcher().isRuntime();
		}
	
		public boolean matches(Method m, Class targetClass, Object[] args) {
			// 2-arg matcher will already have run, so we don't need to do class filtering again
			return a.getMethodMatcher().matches(m, targetClass, args) || b.getMethodMatcher().matches(m, targetClass, args);
		}
	
	}

}
