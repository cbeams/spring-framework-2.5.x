
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.AfterReturningAdvisor;
import org.springframework.aop.MethodAfterReturningAdvice;
import org.springframework.aop.Pointcut;

/**
 * Convenient superclass for static method pointcuts that hold a a BeforeAdvice,
 * making them an Advice. Analogous to the old Spring StaticMethodPointcut.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAfterReturningAdvisor.java,v 1.1 2004-01-05 18:47:01 johnsonr Exp $
 */
public abstract class StaticMethodMatcherPointcutAfterReturningAdvisor extends StaticMethodMatcherPointcut implements AfterReturningAdvisor {

	private boolean isPerInstance;

	private MethodAfterReturningAdvice afterAdvice;
	
	protected StaticMethodMatcherPointcutAfterReturningAdvisor() {
	}

	protected StaticMethodMatcherPointcutAfterReturningAdvisor(MethodAfterReturningAdvice afterAdvice) {
		this.afterAdvice = afterAdvice;
	}

	public abstract boolean matches(Method m, Class targetClass);
	

	public AfterReturningAdvice getAfterReturningAdvice() {
		return afterAdvice;
	}

	public final Pointcut getPointcut() {
		return this;
	}
	

	/**
	 * @see org.springframework.aop.Advisor#isPerInstance()
	 */
	public boolean isPerInstance() {
		throw new UnsupportedOperationException();
	}

}
