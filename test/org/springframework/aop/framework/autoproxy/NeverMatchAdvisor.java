/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: NeverMatchAdvisor.java,v 1.2 2004-02-22 09:48:55 johnsonr Exp $
 */
public class NeverMatchAdvisor extends StaticMethodMatcherPointcutAdvisor {
	
	public NeverMatchAdvisor() {
		super(new NopInterceptor());
	}
	
	/**
	 * This method is solely to allow us to create a mixture of dependencies in
	 * the bean definitions. The dependencies don't have any meaning, and don't
	 * <b>do</b> anything.
	 */
	public void setDependencies(List l) {
		
	}

	/**
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public boolean matches(Method m, Class targetClass) {
		//System.err.println("NeverMAtch test");
		return false;
	}

}
