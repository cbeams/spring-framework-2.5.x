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

package org.springframework.aop.framework;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.support.AopUtils;

/**
 * Spring's implementation of AOP Alliance MethodInvocation interface.
 *
 * <p>Invokes the target object using reflection. Subclasses can override the
 * invokeJoinpoint() method to change this behavior, so this is also a useful
 * base class for more specialized MethodInvocation implementations.
 *
 * <p>It is possible to clone an invocation, to invoke <code>proceed</code> repeatedly
 * (once per clone), using the <code>invocableClone</code> method. It is also possible to
 * add custom attributes to the invocation (since 1.2.6) using the 
 * <code>getUserAttributes()</code> method.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #invokeJoinpoint
 * @see #proceed
 * @see #invocableClone
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation, Cloneable {

	protected final Object proxy;

	protected final Object target;

	protected final Method method;
	
	protected Object[] arguments;
	
	private final Class targetClass;
	
	/**
	 * Lazily initialized map of user-specific attributes for this invocation.
	 */
	private Map userAttributes;

	/**
	 * List of MethodInterceptor and InterceptorAndDynamicMethodMatcher
	 * that need dynamic checks.
	 */
	protected final List interceptorsAndDynamicMethodMatchers;
	
	/**
	 * Index from 0 of the current interceptor we're invoking.
	 * -1 until we invoke: then the current interceptor
	 */
	private int currentInterceptorIndex = -1;

	
	/**
	 * Construct a new MethodInvocation with given arguments
	 * @param interceptorsAndDynamicMethodMatchers interceptors that should be applied,
	 * along with any InterceptorAndDynamicMethodMatchers that need evaluation at runtime.
	 * MethodMatchers included in this struct must already have been found to have matched
	 * as far as was possibly statically. Passing an array might be about 10% faster,
	 * but would complicate the code. And it would work only for static pointcuts.
	 */
	public ReflectiveMethodInvocation(
	    Object proxy, Object target, Method method, Object[] arguments,
	    Class targetClass, List interceptorsAndDynamicMethodMatchers) {

		this.proxy = proxy;
		this.target = target;
		this.targetClass = targetClass;
		this.method = method;
		this.arguments = arguments;
		this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
	}


	/**
	 * Return the proxy that this interception was made through.
	 */
	public final Object getProxy() {
		return this.proxy;
	}

	public final Object getThis() {
		return this.target;
	}

	public final AccessibleObject getStaticPart() {
		return this.method;
	}

	/**
	 * Return the method invoked on the proxied interface.
	 * May or may not correspond with a method invoked on an underlying
	 * implementation of that interface.
	 */
	public final Method getMethod() {
		return this.method;
	}

	public final Object[] getArguments() {
		return this.arguments;
	}

	/**
	 * Return user attributes associated with this invocation.
	 * This map is initialized lazily and is not used in the AOP framework itself.
	 * This method provides an invocation-bound alternative to a ThreadLocal.
	 * @return any user attributes associated with this invocation
	 * (never <code>null</code>)
	 */
	public Map getUserAttributes() {
		if (this.userAttributes == null) {
			this.userAttributes = new HashMap();
		}
		return this.userAttributes;
	}


	public Object proceed() throws Throwable {
		//	We start with an index of -1 and increment early.
		if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
			return invokeJoinpoint();
		}

		Object interceptorOrInterceptionAdvice =
		    this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
		if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
			// Evaluate dynamic method matcher here: static part will already have
			// been evaluated and found to match.
			InterceptorAndDynamicMethodMatcher dm =
			    (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
			if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
				return dm.interceptor.invoke(this);
			}
			else {
				// Dynamic matching failed.
				// Skip this interceptor and invoke the next in the chain.
				return proceed();
			}
		}
		else {
			// It's an interceptor, so we just invoke it: The pointcut will have
			// been evaluated statically before this object was constructed.
			return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
		}
	}
	
	/**
	 * Invoke the joinpoint using reflection.
	 * Subclasses can override this to use custom invocation.
	 * @return the return value of the joinpoint
	 * @throws Throwable if invoking the joinpoint resulted in an exception
	 */
	protected Object invokeJoinpoint() throws Throwable {
		return AopUtils.invokeJoinpointUsingReflection(this.target, this.method, this.arguments);
	}


	/**
	 * Create a clone of this object. If cloning is done before proceed() is invoked on this
	 * object, proceed() can be invoked once per clone to invoke the joinpoint (and the rest
	 * of the advice chain) more than once.
	 * <p>This method returns a shallow copy, except for the argument array, which is
	 * deep-copied to allow for independent modification. We want a shallow copy in this case:
	 * We want to use the same interceptor-chain and other object references, but we want an
	 * independent value for the current interceptor index. 
	 * @see java.lang.Object#clone()
	 * @return an invocable clone of this invocation. proceed() can be called once per clone.
	 */
	public MethodInvocation invocableClone() {
		try {
			ReflectiveMethodInvocation clone = (ReflectiveMethodInvocation) clone();
			// deep copy of arguments
			if (this.arguments != null) {
				clone.arguments = new Object[this.arguments.length];
				System.arraycopy(this.arguments, 0, clone.arguments, 0, this.arguments.length);
			}
			return clone;
		}
		catch (CloneNotSupportedException ex) {
			throw new AspectException("Should be able to clone object of " + getClass(), ex);
		}
	}

	public String toString() {
		// Don't do toString on target, it may be proxied.
		// toString on args may also fail.
		StringBuffer sb = new StringBuffer("invocation: method '");
		sb.append(this.method.getName()).append("', ").append("arguments ");
		sb.append(this.arguments != null ? Arrays.asList(this.arguments).toString() : "[]");
		sb.append("; ");
		if (this.target == null) {
			sb.append("target is null");
		}
		else {
			sb.append("target is of class [").append(this.target.getClass().getName()).append(']');
		}
		return sb.toString();
	}

}
