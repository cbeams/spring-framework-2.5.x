/*
 * Copyright 2002-2004 the original author or authors.
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
	
	protected static Log logger = LogFactory.getLog(ThrowsAdviceInterceptor.class);

	private Object throwsAdvice;

	/** Methods on throws advice, keyed by exception class */
	private Map exceptionHandlerHash;

	public ThrowsAdviceInterceptor(Object throwsAdvice) {
		this.throwsAdvice = throwsAdvice;

		Method[] methods = throwsAdvice.getClass().getMethods();
		exceptionHandlerHash = new HashMap();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getName().equals(AFTER_THROWING) &&
					//m.getReturnType() == null &&
					(m.getParameterTypes().length == 1 || m.getParameterTypes().length == 4) &&
					Throwable.class.isAssignableFrom(m.getParameterTypes()[m.getParameterTypes().length - 1])
				) {
				// Have an exception handler
				exceptionHandlerHash.put(m.getParameterTypes()[m.getParameterTypes().length - 1], m);
				logger.info("Found exception handler method [" + m + "]");
			}
		}
		
		if (exceptionHandlerHash.isEmpty()) {
			throw new IllegalArgumentException("At least one handler method must be found in class " +
			                                   throwsAdvice.getClass());
		}
	}
	
	public int getHandlerMethodCount() {
		return exceptionHandlerHash.size();
	}

	/**
	 * Can return null if not found.
	 * 
	 * @return a handler for the given exception type
	 * @param exception
	 *            Won't be a ServletException or IOException
	 */
	private Method getExceptionHandler(Throwable exception) {
		Class exceptionClass = exception.getClass();
		logger.info("Trying to find handler for exception of " + exceptionClass);
		Method handler = (Method) this.exceptionHandlerHash.get(exceptionClass);
		while (handler == null && !exceptionClass.equals(Throwable.class)) {
			logger.info("Looking at superclass " + exceptionClass);
			exceptionClass = exceptionClass.getSuperclass();
			handler = (Method) this.exceptionHandlerHash.get(exceptionClass);
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
		catch (Throwable t) {
			Method handlerMethod = getExceptionHandler(t);
			if (handlerMethod != null) {
				invokeHandlerMethod(mi, t, handlerMethod);
			}
			throw t;
		}
	}
	
	private void invokeHandlerMethod(MethodInvocation mi, Throwable t, Method m) throws Throwable {
		Object[] handlerArgs;
		if (m.getParameterTypes().length == 1) {
			handlerArgs = new Object[] { t };
		}
		else {
			handlerArgs = new Object[] { mi.getMethod(), mi.getArguments(), mi.getThis(), t };
		}
		try {
			m.invoke(this.throwsAdvice, handlerArgs);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

}
