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
 */

package org.springframework.aop.aspectj;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;

/**
 * Spring AOP advice wrapping an AspectJ after-returning advice method.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice {

	public AspectJAfterReturningAdvice(
			Method aspectJBeforeAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif) {

		super(aspectJBeforeAdviceMethod, pointcut, aif);
	}

	public boolean isBeforeAdvice() {
		return false;
	}

	public boolean isAfterAdvice() {
		return true;
	}

	public void setReturningName(String name) {
		setReturningNameNoCheck(name);
	}

	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		if (shouldInvokeOnReturnValueOf(returnValue)) {
			invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
		}
	}

	/**
	 * Following AspectJ semantics, if a returning clause was specified, then the
	 * advice is only invoked if the returned value is an instance of the given
	 * returning type. Iff the returning type is object, the advice is *always* invoked.
	 */
	private boolean shouldInvokeOnReturnValueOf(Object returnValue) {
		Class returningType = getDiscoveredReturningType();
		if (returningType.equals(Object.class) || returningType.isPrimitive()) {
			return true;
		}
		else {
			return returningType.isAssignableFrom(returnValue.getClass());
		}
	}

}
