/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop;

import java.lang.reflect.Method;

/**
 * Advice invoked before a method is invoked. Such advices cannot
 * prevent the method call proceeding, unless they throw a
 * Throwable. 
 * @author Rod Johnson
 * @version $Id: MethodBeforeAdvice.java,v 1.2 2004-02-22 09:59:59 johnsonr Exp $
 */
public interface MethodBeforeAdvice extends BeforeAdvice {
	
	/**
	 * Callback before a given method is invoked
	 * @param m method being invoked
	 * @param args arguments to the method
	 * @param target target of the method invocation. May be null
	 * @throws Throwable if this object wishes to abort the
	 * call. Any exception thrown will be returned to the caller
	 * if it's allowed by the method signature. Otherwise
	 * the exception will be wrapped as a runtime exception. 
	 */
	void before(Method m, Object[] args, Object target) throws Throwable;

}
