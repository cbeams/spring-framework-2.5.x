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

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

/**
 * Implementation of AspectJ ProceedingJoinPoint interface
 * wrapping an AOP Alliance MethodInvocation.
 * 
 * @author Rod Johnson
 * @since 1.3
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint {
	
	private final MethodInvocation methodInvocation;
	
	public MethodInvocationProceedingJoinPoint(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
	}

	public void set$AroundClosure(AroundClosure aroundClosure) {
		throw new UnsupportedOperationException();
	}

	public Object proceed() throws Throwable {
		return methodInvocation.proceed();
	}

	public Object proceed(Object[] args) throws Throwable {
		Object[] oldArgs = methodInvocation.getArguments();
		for (int i = 0; i < oldArgs.length; i++) {
			oldArgs[i] = args[i];
		}
		return methodInvocation.proceed();
	}

	public String toShortString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public String toLongString() {
		return "execution of " + methodInvocation.getMethod().getName();
	}

	public Object getThis() {
		throw new UnsupportedOperationException("Cannot support caller semantics");
	}

	public Object getTarget() {
		return methodInvocation.getThis();
	}

	public Object[] getArgs() {
		return methodInvocation.getArguments();
	}

	public Signature getSignature() {
		throw new UnsupportedOperationException();
	}

	public SourceLocation getSourceLocation() {
		throw new UnsupportedOperationException();
	}

	public String getKind() {
		return ProceedingJoinPoint.METHOD_EXECUTION;
	}

	public StaticPart getStaticPart() {
		throw new UnsupportedOperationException();
	}

}
