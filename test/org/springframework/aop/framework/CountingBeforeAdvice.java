/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;

import org.springframework.aop.MethodBeforeAdvice;


/**
 * Simple before advice example that we can use for counting checks.
 * 
 * @author Rod Johnson
 * @version $Id: CountingBeforeAdvice.java,v 1.1 2003-12-08 11:23:51 johnsonr Exp $
 */
public class CountingBeforeAdvice extends MethodCounter implements MethodBeforeAdvice {
	public void before(Method m, Object[] args, Object target) throws Throwable {
		count(m);
	}
}