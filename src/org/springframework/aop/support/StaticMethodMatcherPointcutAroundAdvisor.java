
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.aopalliance.intercept.Interceptor;

import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass for static method pointcuts that hold an IntroductionInterceptor,
 * making them an Advice. nalogous to the old Spring StaticMethodPointcut.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAroundAdvisor.java,v 1.2 2003-11-21 22:45:29 jhoeller Exp $
 */
public abstract class StaticMethodMatcherPointcutAroundAdvisor extends StaticMethodMatcherPointcut implements InterceptionAroundAdvisor {

	private boolean isPerInstance;

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
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	public Interceptor getInterceptor() {
		return interceptor;
	}

	public final Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
