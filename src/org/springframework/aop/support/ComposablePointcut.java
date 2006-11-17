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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Convenient class for building up pointcuts. All methods return
 * ComposablePointcut, so we can use a concise idiom like:
 *
 * <code>
 * Pointcut pc = new ComposablePointcut().union(classFilter).intersection(methodMatcher).intersection(pointcut);
 * </code>
 *
 * <p>There is no <code>union(Pointcut, Pointcut)</code> method on this class.
 * Use the <code>Pointcuts.union</code> method for this.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 11.11.2003
 * @see Pointcuts#union(org.springframework.aop.Pointcut, org.springframework.aop.Pointcut)
 */
public class ComposablePointcut implements Pointcut, Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = -2743223737633663832L;

	private ClassFilter classFilter;

	private MethodMatcher methodMatcher;


	/**
	 * Create a default ComposablePointcut, with <code>ClassFilter.TRUE</code>
	 * and <code>MethodMatcher.TRUE</code>.
	 */
	public ComposablePointcut() {
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * Create a ComposablePointcut for the given ClassFilter,
	 * with <code>MethodMatcher.TRUE</code>.
	 * @param classFilter the ClassFilter to use
	 */
	public ComposablePointcut(ClassFilter classFilter) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		this.classFilter = classFilter;
		this.methodMatcher = MethodMatcher.TRUE;
	}

	/**
	 * Create a ComposablePointcut for the given MethodMatcher,
	 * with <code>ClassFilter.TRUE</code>.
	 * @param methodMatcher the MethodMatcher to use
	 */
	public ComposablePointcut(MethodMatcher methodMatcher) {
		Assert.notNull(methodMatcher, "MethodMatcher must not be null");
		this.classFilter = ClassFilter.TRUE;
		this.methodMatcher = methodMatcher;
	}

	/**
	 * Create a ComposablePointcut for the given ClassFilter and MethodMatcher.
	 * @param classFilter the ClassFilter to use
	 * @param methodMatcher the MethodMatcher to use
	 */
	public ComposablePointcut(ClassFilter classFilter, MethodMatcher methodMatcher) {
		Assert.notNull(classFilter, "ClassFilter must not be null");
		Assert.notNull(methodMatcher, "MethodMatcher must not be null");
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


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ComposablePointcut)) {
			return false;
		}

		ComposablePointcut that = (ComposablePointcut) other;
		return ObjectUtils.nullSafeEquals(that.classFilter, this.classFilter) &&
				ObjectUtils.nullSafeEquals(that.methodMatcher, this.methodMatcher);
	}

	public int hashCode() {
		int code = 17;
		if (this.classFilter != null) {
			code = 37 * code + this.classFilter.hashCode();
		}
		if (this.methodMatcher != null) {
			code = 37 * code + this.methodMatcher.hashCode();
		}
		return code;
	}

	public String toString() {
		return "ComposablePointcut consisting of ClassFilter [" + this.classFilter +
				"] and MethodMatcher [" + this.methodMatcher + "]";
	}

}
