/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * 
 * @author Rod Johnson
 * @version $Id: UnsupportedInterceptor.java,v 1.1 2003-12-08 11:24:20 johnsonr Exp $
 */
public class UnsupportedInterceptor implements MethodInterceptor {

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		throw new UnsupportedOperationException(mi.getMethod().getName());
	}

}
