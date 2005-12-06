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
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.PointcutExpression;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.core.ParameterNameDiscoverer;

/**
 * Spring AOP around advice (MethodInterceptor) that wraps
 * an AspectJ advice method. Exposes ProceedingJoinPoint.
 * @author Rod Johnson
 * @since 2.0
 */
public class AspectJAroundAdvice extends AbstractAspectJAdvice implements MethodInterceptor {
	
	private final ParameterNameDiscoverer parameterNameDiscoverer;

	public AspectJAroundAdvice(Method aspectJAroundAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aif, ParameterNameDiscoverer parameterNameDiscoverer) {
		super(aspectJAroundAdviceMethod, pointcut.getPointcutExpression(), aif);
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	public AspectJAroundAdvice(Method aspectJAroundAdviceMethod, PointcutExpression pe, AspectInstanceFactory aif, ParameterNameDiscoverer parameterNameDiscoverer) {
		super(aspectJAroundAdviceMethod, pe, aif);
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}

	public Object invoke(MethodInvocation mi) throws Throwable {
		ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(mi);
		Object[] formals = argBinding(mi.getArguments());
		if (formals == null) {
			formals = new Object[0];
		}
		Object[] args = new Object[formals.length + 1];
		args[0] = pjp;
		ReflectiveMethodInvocation invocation = (ReflectiveMethodInvocation) mi;

		String[] argNames = parameterNameDiscoverer.getParameterNames(aspectJAdviceMethod,aspectJAdviceMethod.getDeclaringClass());
		if (argNames == null) {
			// basic mapping applies
			System.arraycopy(formals, 0, args, 1, formals.length);				
		} else {
			// map based on bindings
			Map bindingMap = invocation.getUserAttributes();
			for (int i = 1; i < args.length; i++) {
				// should be made more robust, works for now...
				args[i] = bindingMap.get(argNames[i-1]);
			}
		}
				
		return invokeAdviceMethodWithGivenArgs(args);
	}
}