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

package org.springframework.aop.framework.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interceptor to wrap an after throwing advice.
 *
 * <p>The signatures on handler methods on the throwsAdvice constructor argument
 * must be of form:<br>
 * <code>void afterThrowing([Method], [args], [target], ThrowableSubclass);</code><br>
 * Only the last argument is required.
 *
 * <p>This is a framework class that need not be used directly by Spring users.
 * 
 * <p>You can, however, use this class to wrap Spring ThrowsAdvice implementations
 * for use in other AOP frameworks supporting the AOP Alliance
 * interfaces.
 *
 * @author Rod Johnson
 */
public final class ThrowsAdviceInterceptor implements MethodInterceptor {
	
	// TODO make serializable (methods are not serializable)
	
	private static final String AFTER_THROWING = "afterThrowing";
	
	private static final Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);


	private final Object throwsAdvice;

	/** Methods on throws advice, keyed by exception class */
	private final Map exceptionHandlerMap = new HashMap();


	/**
	 * Create a new ThrowsAdviceInterceptor for the given ThrowsAdvice.
	 * @param throwsAdvice the advice object that defines the exception
	 * handler methods (usually a ThrowsAdvice implementation)
	 * @see org.springframework.aop.ThrowsAdvice
	 */
	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		this.throwsAdvice = throwsAdvice;

		Method[] methods = throwsAdvice.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().equals(AFTER_THROWING) &&
					//m.getReturnType() == null &&
					(method.getParameterTypes().length == 1 || method.getParameterTypes().length == 4) &&
					Throwable.class.isAssignableFrom(method.getParameterTypes()[method.getParameterTypes().length - 1])
				) {
				// Have an exception handler
				this.exceptionHandlerMap.put(method.getParameterTypes()[method.getParameterTypes().length - 1], method);
				if (logger.isDebugEnabled()) {
					logger.debug("Found exception handler method: " + method);
				}
			}
		}
		
		if (this.exceptionHandlerMap.isEmpty()) {
			throw new IllegalArgumentException(
					"At least one handler method must be found in class [" + throwsAdvice.getClass() + "]");
		}
	}
	
	public int getHandlerMethodCount() {
		return this.exceptionHandlerMap.size();
	}

	/**
	 * Determine the exception handle method. Can return null if not found.
	 * @param exception the exception thrown
	 * @return a handler for the given exception type
	 */
	private Method getExceptionHandler(Throwable exception) {
		Class exceptionClass = exception.getClass();
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to find handler for exception of type [" + exceptionClass.getName() + "]");
		}
		Method handler = (Method) this.exceptionHandlerMap.get(exceptionClass);
		while (handler == null && !exceptionClass.equals(Throwable.class)) {
			exceptionClass = exceptionClass.getSuperclass();
			handler = (Method) this.exceptionHandlerMap.get(exceptionClass);
		}
		return handler;
	}

	/**
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation mi) throws Throwable {
		try {
			return mi.proceed();
		}
		catch (Throwable ex) {
			Method handlerMethod = getExceptionHandler(ex);
			if (handlerMethod != null) {
				invokeHandlerMethod(mi, ex, handlerMethod);
			}
			throw ex;
		}
	}
	
	private void invokeHandlerMethod(MethodInvocation mi, Throwable ex, Method method) throws Throwable {
		Object[] handlerArgs;
		if (method.getParameterTypes().length == 1) {
			handlerArgs = new Object[] { ex };
		}
		else {
			handlerArgs = new Object[] { mi.getMethod(), mi.getArguments(), mi.getThis(), ex };
		}
		try {
			method.invoke(this.throwsAdvice, handlerArgs);
		}
		catch (InvocationTargetException targetEx) {
			throw targetEx.getTargetException();
		}
	}

}
