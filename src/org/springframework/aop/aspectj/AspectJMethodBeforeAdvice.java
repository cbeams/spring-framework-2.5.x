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

import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.tools.PointcutExpression;

import org.springframework.aop.MethodBeforeAdvice;

/**
 * Spring AOP advice that wraps an AspectJ before method.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class AspectJMethodBeforeAdvice extends AbstractAspectJAdvice implements MethodBeforeAdvice {

	public AspectJMethodBeforeAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pointcut.getPointcutExpression(), aif);
	}

	public AspectJMethodBeforeAdvice(
			Method aspectJBeforeAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif) {
		super(aspectJBeforeAdviceMethod, pe, aif);
	}
	
	
	public void before(Method method, Object[] args, Object target) throws Throwable {
		// TODO binding not properly implemented; will be post 2.0 M1
		if (this.aspectJAdviceMethod.getParameterTypes().length > 0 &&
				JoinPoint.class.isAssignableFrom(aspectJAdviceMethod.getParameterTypes()[0])) {
			invokeAdviceMethodWithGivenArgs(new Object[] { ExposeJoinPointInterceptor.currentJoinPoint() });
		}
		else {
			invokeAdviceMethod(args);
		}
	}

}
