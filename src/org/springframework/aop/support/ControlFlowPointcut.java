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
import org.springframework.core.ControlFlow;
import org.springframework.core.ControlFlowFactory;

/**
 * Pointcut and method matcher for use in simple <b>cflow</b>-style pointcut.
 * Note that evaluating such pointcuts is 10-15 times slower than evaluating
 * normal pointcuts, but they are useful in some cases.
 * @see org.springframework.core.ControlFlow
 * @author Rod Johnson
 */
public class ControlFlowPointcut implements Pointcut, ClassFilter, MethodMatcher, Serializable {
	
	private Class clazz;
	
	private String methodName;
	
	private int evaluations;
	
	public ControlFlowPointcut(Class clazz) {
		this(clazz, null);
	}
	
	/**
	 * Construct a new pointcut that matches all calls below the
	 * given method in the given class. If the method name is null,
	 * matches all control flows below that class.
	 * @param clazz
	 * @param methodName
	 */
	public ControlFlowPointcut(Class clazz, String methodName) {
		this.clazz = clazz;
		this.methodName = methodName;
	}

	/** 
	 * Subclasses can override this for greater filtering (and performance).
	 */
	public boolean matches(Class clazz) {
		return true;
	}
		
	/**
	 * Subclasses can override this if it's possible to filter out
	 * some candidate classes.
	 */
	public boolean matches(Method m, Class targetClass) {
		return true;
	}

	public boolean isRuntime() {
		return true;
	}

	public boolean matches(Method m, Class targetClass, Object[] args) {
		++evaluations;
		ControlFlow cflow = ControlFlowFactory.createControlFlow();
		return (methodName != null) ? cflow.under(clazz, methodName) : cflow.under(clazz);
	}

	/**
	 * It's useful to know how many times we've fired, for optimization.
	 */
	public int getEvaluations() {
		return evaluations;
	}

	public ClassFilter getClassFilter() {
		return this;
	}

	public MethodMatcher getMethodMatcher() {
		return this;
	}

}
