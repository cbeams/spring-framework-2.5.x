
package org.springframework.aop.support.aspectj;

import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.Pointcut;
import org.aopalliance.aop.Advice;

/**
 * 
 * @author robh
 */
public class AspectJExpressionPointcutAdvisor extends AspectJExpressionPointcut implements PointcutAdvisor {

	private Advice advice;

	public Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public Advice getAdvice() {
		return this.advice;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}
}
