
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.BeforeAdvisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass for static method pointcuts that hold a a BeforeAdvice,
 * making them an Advice. Analogous to the old Spring StaticMethodPointcut.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutBeforeAdvisor.java,v 1.1 2003-12-05 13:05:15 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutBeforeAdvisor extends StaticMethodMatcherPointcut implements BeforeAdvisor {

	private boolean isPerInstance;

	private MethodBeforeAdvice beforeAdvice;
	
	protected StaticMethodMatcherPointcutBeforeAdvisor() {
	}

	protected StaticMethodMatcherPointcutBeforeAdvisor(MethodBeforeAdvice beforeAdvice) {
		this.beforeAdvice = beforeAdvice;
	}

	public abstract boolean matches(Method m, Class targetClass);

	public void setBeforeAdvice(MethodBeforeAdvice beforeAdvice) {
		this.beforeAdvice = beforeAdvice;
	}
	
	public void setIsPerInstance(boolean isPerInstance) {
		this.isPerInstance = isPerInstance;
	}

	public BeforeAdvice getBeforeAdvice() {
		return beforeAdvice;
	}

	public final Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		return this.isPerInstance;
	}

}
