
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;

/**
 * Convenient superclass for static method pointcuts that hold interception 
 * around advice, via an AOP Alliance Interceptor.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAroundAdvisor.java,v 1.3 2004-01-13 16:34:31 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements InterceptionAroundAdvisor {

	private Interceptor interceptor;
	
	protected StaticMethodMatcherPointcutAroundAdvisor() {
	}

	protected StaticMethodMatcherPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	public abstract boolean matches(Method m, Class targetClass);

	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public Interceptor getInterceptor() {
		return interceptor;
	}

}
