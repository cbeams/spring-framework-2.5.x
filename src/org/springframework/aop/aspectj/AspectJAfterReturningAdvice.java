/*
 * Copyright 2002-2007 the original author or authors.
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

import org.springframework.aop.AfterAdvice;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.core.JdkVersion;
import org.springframework.util.ClassUtils;
import org.springframework.util.GenericUtils;

/**
 * Spring AOP advice wrapping an AspectJ after-returning advice method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @since 2.0
 */
public class AspectJAfterReturningAdvice extends AbstractAspectJAdvice implements AfterReturningAdvice, AfterAdvice {

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
		if (shouldInvokeOnReturnValueOf(method, returnValue)) {
			invokeAdviceMethod(getJoinPointMatch(), returnValue, null);
		}
	}

	/**
	 * Following AspectJ semantics, if a returning clause was specified, then the
	 * advice is only invoked if the returned value is an instance of the given
	 * returning type and genetic type parameters, if any, match the assignment
	 * rules. If the returning type is Object, the advice is *always* invoked.
	 * @param returnValue the return value of the target method
	 * @return whether to invoke the advice method for the given return value
	 */
	private boolean shouldInvokeOnReturnValueOf(Method method, Object returnValue) {
		Class discoveredReturningType = getDiscoveredReturningType();
		Object discoveredReturningGenericType = getDiscoveredReturningGenericType();
		if (ClassUtils.isAssignableValue(discoveredReturningType, returnValue)) {
			// We are using Java 5 and above and we aren't dealing with a raw type, check if 
			// generic parameters are assignable
			if (JdkVersion.isAtLeastJava15() && (discoveredReturningType != discoveredReturningGenericType)) {
				return GenericUtils.isAssignable(discoveredReturningGenericType, method);
			}
			return true;
		}
		return false;
	}

}
