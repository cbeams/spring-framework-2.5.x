
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.*;
import org.springframework.aop.InterceptionAroundAdvisor;


/**
 * Convenient superclass for static method pointcuts that hold an IntroductionInterceptor, making them
 * an Advice.
 * <br>Analogous to the old Spring StaticMethodPointcut.
 * 
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAroundAdvisor.java,v 1.1 2003-11-16 12:54:58 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutAroundAdvisor extends StaticMethodMatcherPointcut implements InterceptionAroundAdvisor {

	private boolean isPerInstance;

	private Interceptor interceptor;
	
	protected StaticMethodMatcherPointcutAroundAdvisor() {
	}
	
	
	protected StaticMethodMatcherPointcutAroundAdvisor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}

	/**
	 * @see org.springframework.aop.pointcut.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	public abstract boolean matches(Method m, Class targetClass);
	
	
	public void setInterceptor(Interceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	/**
	 * @see org.springframework.aop.framework.InterceptionAdvice#getInterceptor()
	 */
	public Interceptor getInterceptor() {
		return interceptor;
	}

	/**
	 * @see org.springframework.aop.framework.Advice#getPointcut()
	 */
	public final Pointcut getPointcut() {
		return this;
	}

	

	/**
	 * @see org.springframework.aop.Advice#isPerInstance()
	 */
	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
