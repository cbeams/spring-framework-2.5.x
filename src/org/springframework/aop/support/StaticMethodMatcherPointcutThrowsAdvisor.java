
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.Pointcut;
import org.springframework.aop.ThrowsAdvisor;

/**
 * Convenient superclass for static method pointcuts that hold a a ThrowsAdvice,
 * making them an Advisor. Analogous to the old Spring StaticMethodPointcut.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutThrowsAdvisor.java,v 1.1 2003-12-05 16:28:10 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutThrowsAdvisor extends StaticMethodMatcherPointcut implements ThrowsAdvisor {

	private boolean isPerInstance;

	private Object throwsAdvice;
	
	protected StaticMethodMatcherPointcutThrowsAdvisor() {
	}

	protected StaticMethodMatcherPointcutThrowsAdvisor(Object throwsAdvice) {
		this.throwsAdvice = throwsAdvice;
	}

	public abstract boolean matches(Method m, Class targetClass);

	public void setThrowsAdvice(Object throwsAdvice) {
		this.throwsAdvice = throwsAdvice;
	}
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	public Object getThrowsAdvice() {
		return throwsAdvice;
	}

	public final Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
