/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for Advisors that are also static pointcuts.
 * @author Rod Johnson
 * @version $Id: StaticMethodMatcherPointcutAdvisor.java,v 1.2 2004-02-11 17:18:17 jhoeller Exp $
 */
public abstract class StaticMethodMatcherPointcutAdvisor extends StaticMethodMatcherPointcut
    implements PointcutAdvisor {

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public Pointcut getPointcut() {
		return this;
	}

}
