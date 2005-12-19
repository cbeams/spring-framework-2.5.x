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
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;

/**
 * Spring AOP implementation of AspectJ JoinPoint.
 * Does not require an AOP Alliance MethodInvocation.
 *
 * <p>Not currently used, but may be used for optimization in the future.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint
 */
public class JoinPointImpl implements JoinPoint {
	
	private final Method method;
	
	private final Object target;
	
	private final Object[] args;


	public JoinPointImpl(Method method, Object target, Object[] args) {
		this.method = method;
		this.target = target;
		this.args = args;
	}


	public String toShortString() {
		return "execution of " + method.getName();
	}

	public String toLongString() {
		return "execution of " + method.getName();
	}

	public Object getThis() {
		return target;
	}

	public Object getTarget() {
		return target;
	}

	public Object[] getArgs() {
		return args;
	}

	public Signature getSignature() {
		throw new UnsupportedOperationException();
	}

	public SourceLocation getSourceLocation() {
		throw new UnsupportedOperationException();
	}

	public StaticPart getStaticPart() {
		throw new UnsupportedOperationException();
	}

	public String getKind() {
		return JoinPoint.METHOD_EXECUTION;
	}

}
