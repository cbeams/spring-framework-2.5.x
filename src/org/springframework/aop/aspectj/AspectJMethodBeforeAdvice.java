/*
 * Copyright 2002-2005 the original author or authors.
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
 */
package org.springframework.aop.aspectj;

import java.lang.reflect.Method;

import org.aspectj.weaver.tools.PointcutExpression;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Spring AOP advice that wraps an AspectJ before method.
 * @author Rod Johnson
 * @since 1.3
 */
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice {

	public AspectJMethodBeforeAdvice(Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut.getPointcutExpression(), aif);
	}

	public AspectJMethodBeforeAdvice(Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pe, aif);
	}
	
	public void before(Method method, Object[] args, Object target) throws Throwable {
		invokeAdviceMethod(args);
	}
}