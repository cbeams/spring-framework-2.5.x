/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * 
 * @author Rod Johnson
 * @version $Id: MethodBeforeAdvice.java,v 1.1 2003-12-05 13:05:54 johnsonr Exp $
 */
public interface MethodBeforeAdvice extends BeforeAdvice {
	
	void before(Method m, Object[] args, Object target) throws Throwable;

}
