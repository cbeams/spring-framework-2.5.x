
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.ThrowsAdvisor;

/**
 * Convenient superclass for static method pointcuts that hold a a ThrowsAdvice,
 * making them an Advisor. Analogous to the old Spring StaticMethodPointcut.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutThrowsAdvisor.java,v 1.3 2004-01-13 16:34:31 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutThrowsAdvisor extends StaticMethodMatcherPointcutAdvisor implements ThrowsAdvisor {

	private ThrowsAdvice throwsAdvice;
	
	protected StaticMethodMatcherPointcutThrowsAdvisor() {
	}

	protected StaticMethodMatcherPointcutThrowsAdvisor(ThrowsAdvice throwsAdvice) {
		this.throwsAdvice = throwsAdvice;
	}

	public abstract boolean matches(Method m, Class targetClass);

	public void setThrowsAdvice(ThrowsAdvice throwsAdvice) {
		this.throwsAdvice = throwsAdvice;
	}
	

	public ThrowsAdvice getThrowsAdvice() {
		return throwsAdvice;
	}

}
