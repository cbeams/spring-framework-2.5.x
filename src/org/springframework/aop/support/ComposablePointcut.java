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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Convenient class for building up pointcuts.
 * All methods return ComposablePointcut, so we can use a concise
 * idiom like:
 * <code>
 * Pointcut pc = new ComposablePointcut().union(classFilter).intersection(methodMatcher).intersection(pointcut);
 * </code>
 * There is no union() method on this class. Use the Pointcuts.union()
 * method for this.
 * @author Rod Johnson
 * @since 11-Nov-2003
 */
public class ComposablePointcut implements Pointcut, Serializable {
	
	private ClassFilter classFilter;
	
	private MethodMatcher methodMatcher;
	
	public ComposablePointcut() {
		this.classFilter =  ClassFilter.TRUE;
		this.methodMatcher = MethodMatcher.TRUE;
	}
	
	public ComposablePointcut(ClassFilter classFilter, MethodMatcher methodMatcher) {
		this.classFilter = classFilter;
		this.methodMatcher = methodMatcher;
	}
	
	public ComposablePointcut union(ClassFilter filter) {
		this.classFilter = ClassFilters.union(this.classFilter, filter);
		return this;
	}
	
	public ComposablePointcut intersection(ClassFilter filter) {
		this.classFilter = ClassFilters.intersection(this.classFilter, filter);
		return this;
	}

	public ComposablePointcut union(MethodMatcher mm) {
		this.methodMatcher = MethodMatchers.union(this.methodMatcher, mm);
		return this;
	}

	public ComposablePointcut intersection(MethodMatcher mm) {
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, mm);
		return this;
	}
	
	
	public ComposablePointcut intersection(Pointcut other) {
		this.classFilter = ClassFilters.intersection(this.classFilter, other.getClassFilter());
		this.methodMatcher = MethodMatchers.intersection(this.methodMatcher, other.getMethodMatcher());
		return this;
	}

	public ClassFilter getClassFilter() {
		return this.classFilter;
	}

	public MethodMatcher getMethodMatcher() {
		return this.methodMatcher;
	}

}
