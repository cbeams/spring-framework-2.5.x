/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * Advice for after returning advice. This means on normal return,
 * not exception.
 * @author Rod Johnson
 * @version $Id: MethodAfterReturningAdvice.java,v 1.1 2004-01-05 18:47:00 johnsonr Exp $
 */
public interface MethodAfterReturningAdvice extends AfterReturningAdvice {
	
	void afterReturning(Object returnValue, Method m, Object[] args, Object target) throws Throwable;

}
