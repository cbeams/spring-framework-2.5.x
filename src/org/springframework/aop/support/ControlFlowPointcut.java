/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Pointcut and method matcher for use in simple <b>cflow</b>-style pointcut.
 * Note that evaluating such pointcuts is 10-15 times slower than evaluating
 * normal pointcuts, but they are useful in some cases.
 * @see org.springframework.aop.support.ControlFlow
 * @author Rod Johnson
 * @version $Id: ControlFlowPointcut.java,v 1.2 2003-12-15 14:39:29 johnsonr Exp $
 */
public class ControlFlowPointcut implements Pointcut, ClassFilter, MethodMatcher {
	
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
	 * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
	 */
	public boolean matches(Class clazz) {
		return true;
	}
		
	/**
	 * Subclasses can override this if it's possible to filter out
	 * some candidate classes.
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public boolean matches(Method m, Class targetClass) {
		return true;
	}

	/**
	 * @see org.springframework.aop.MethodMatcher#isRuntime()
	 */
	public boolean isRuntime() {
		return true;
	}

	/**
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class, java.lang.Object[])
	 */
	public boolean matches(Method m, Class targetClass, Object[] args) {
		++evaluations;
		ControlFlow cflow = ControlFlowFactory.getInstance().createControlFlow();
		return (methodName != null) ? cflow.under(clazz, methodName) : cflow.under(clazz);
	}
	
	
	/**
	 * It's useful to know how many times we've fired, for optimization
	 * @return
	 */
	public int getEvaluations() {
		return evaluations;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return this;
	}

	/**
	 * @see org.springframework.aop.Pointcut#getMethodMatcher()
	 */
	public MethodMatcher getMethodMatcher() {
		return this;
	}

}
