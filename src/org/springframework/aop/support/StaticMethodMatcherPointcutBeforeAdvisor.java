
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.BeforeAdvisor;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Convenient superclass for static method pointcuts that hold a a BeforeAdvice,
 * making them an Advisor.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutBeforeAdvisor.java,v 1.2 2004-01-13 16:34:31 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutBeforeAdvisor extends StaticMethodMatcherPointcutAdvisor implements BeforeAdvisor {

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
	
	public BeforeAdvice getBeforeAdvice() {
		return beforeAdvice;
	}

}
