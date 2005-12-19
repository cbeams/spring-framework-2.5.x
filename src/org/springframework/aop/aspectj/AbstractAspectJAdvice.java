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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.weaver.tools.PointcutExpression;

import org.springframework.aop.framework.AopConfigException;

/**
 * Superclass for Spring Advices wrapping an AspectJ aspect
 * or annotated advice method
 *
 * @author Rod Johnson
 * @since 2.0
 */
abstract class AbstractAspectJAdvice {
	
	protected final Method aspectJAdviceMethod;
	
	private final PointcutExpression pointcutExpression;
	
	private final AspectInstanceFactory aif;


	public AbstractAspectJAdvice(Method aspectJAdviceMethod, PointcutExpression pointcutExpression, AspectInstanceFactory aif) {
		this.aspectJAdviceMethod = aspectJAdviceMethod;
		this.pointcutExpression = pointcutExpression;
		this.aif = aif;
	}
	
	public Method getAspectJAdviceMethod() {
		return this.aspectJAdviceMethod;
	}


	/**
	 * Take the arguments in the call to the advised method and output a set of arguments
	 * to the advice method
	 * @param availableArgs arguments to the method being invoked
	 * @return the empty array if there are no arguments
	 */
	protected Object[] argBinding(Object[] availableArgs) {
		// TODO may not bind all of them
		//pointcutExpression.
		
		// TODO wrong way to get target
		//ShadowMatch sm = pointcutExpression.matchesMethodExecution(aspectJAdviceMethod);
		
		return availableArgs;
	}
	
	/**
	 * Invoke the advice method.
	 * @param argsInCall arguments to the method being invoked, which is adviced by the
	 * advice
	 */
	protected Object invokeAdviceMethod(Object[] argsInCall) throws Throwable {
		return invokeAdviceMethodWithGivenArgs(argBinding(argsInCall));
	}
	
	protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
		// TODO really a hack
		if (aspectJAdviceMethod.getParameterTypes().length == 0) {
			args = null;
		}
		
		try {
			// TODO AOPutils.InvokeJoinpointUsingReflection
			return aspectJAdviceMethod.invoke(aif.getAspectInstance(), args);
		}
		catch (IllegalArgumentException ex) {
			throw new AopConfigException("Mismatch on arguments to advice method " + aspectJAdviceMethod + "; " +
					"pointcut expression='" + pointcutExpression.getPointcutExpression() + "'", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}
}