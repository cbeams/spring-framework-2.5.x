/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 13-Feb-2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * AspectJPointcutAdvisor enforces the rule that the pointcut reference
 * held by this advisor must be to the same pointcut instance as that
 * held by the associated (AspectJ) advice.
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class AspectJPointcutAdvisor extends DefaultPointcutAdvisor {

	private static final long serialVersionUID = -7817774360632388588L;

	public AspectJPointcutAdvisor() {
		super();
	}

	/**
	 * Ensure pointcut instance in this advisor and the associated advice point to the
	 * same instance.
	 */
	public void setPointcut(Pointcut pointcut) {
		if (! (pointcut instanceof AspectJExpressionPointcut)) {
			String msg = "AspectJPointcutAdvisor requires an AspectJExpressionPointcut but " +
			             "was passed an instance of '" + pointcut.getClass().getName() + "'";
			throw new IllegalArgumentException(msg);
		}
		
		AspectJExpressionPointcut newPointcut = (AspectJExpressionPointcut) pointcut;
		
		Advice myAdvice = getAdvice();
		if (myAdvice instanceof AbstractAspectJAdvice) {
			AbstractAspectJAdvice myAjAdvice = (AbstractAspectJAdvice) myAdvice;
			String adviceExpression = myAjAdvice.getPointcut().getExpression();
			if (newPointcut.getExpression().equals(adviceExpression)) {
				// same expression, safe to use same instance
				super.setPointcut(myAjAdvice.getPointcut());
			}
			else {
				String msg = "Pointcut expression in advisor must match expression in associated advice:\n" +
										 "expression is '" + newPointcut.getExpression() + "'\n" +
										 "and expression in advice is '" + adviceExpression + "'";
				throw new IllegalStateException(msg);
			}
		}
		else {
			super.setPointcut(newPointcut);
		}
	}
	
	/**
	 * Ensure pointcut instance in this advisor and the associated advice point to the
	 * same instance.
	 */
	public void setAdvice(Advice advice) {
		super.setAdvice(advice);
		
		if (advice instanceof AbstractAspectJAdvice) {
			AbstractAspectJAdvice ajAdvice = (AbstractAspectJAdvice) advice;
			ensureAdviceAndPointcutReferToSamePointcutInstance(ajAdvice);	
			setOrder(ajAdvice.getOrder());
		}
		
	}

	private void ensureAdviceAndPointcutReferToSamePointcutInstance(AbstractAspectJAdvice ajAdvice) {
		if (getPointcut() != null) {
			AspectJExpressionPointcut ajPointcut = (AspectJExpressionPointcut) getPointcut();
			if (!ajPointcut.getExpression().equals(ajAdvice.getPointcut().getExpression())) {
				String msg = "Pointcut expression in advisor must match expression in associated advice:\n" +
	             "expression is '" + ajPointcut.getExpression() + "'\n" +
	             "and expression in advice is '" + ajAdvice.getPointcut().getExpression() + "'";
				throw new IllegalStateException(msg);
			}
		}
		
		setPointcut(ajAdvice.getPointcut());
	}
	
}
