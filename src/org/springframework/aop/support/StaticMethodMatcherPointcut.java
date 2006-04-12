/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass when we want to force subclasses to implement the
 * MethodMatcher interface, but subclasses will want to be pointcuts.
 *
 * <p>The "classFilter" property can be set to customize ClassFilter behavior.
 * The default is <code>ClassFilter.TRUE</code>.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setClassFilter
 * @see ClassFilter#TRUE
 */
public abstract class StaticMethodMatcherPointcut extends StaticMethodMatcher implements Pointcut {

	private ClassFilter classFilter = ClassFilter.TRUE;


	/**
	 * Set the {@link ClassFilter} to use for this pointcut.
	 * Default is {@link ClassFilter#TRUE}.
	 */
	public void setClassFilter(ClassFilter classFilter) {
		this.classFilter = classFilter;
	}

	public ClassFilter getClassFilter() {
		return classFilter;
	}


	public final MethodMatcher getMethodMatcher() {
		return this;
	}

}
