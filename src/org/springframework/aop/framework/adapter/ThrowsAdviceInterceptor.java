/*
 * The Spring Framework is published under the terms of the Apache Software License.
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
import org.springframework.aop.framework.AopConfigException;

/**
 * Interceptor to wrap an after throwing advice.
 * The signatures on handler methods on the throwsAdvice constructor argument must be of form
 * void afterThrowing([Method], [args], [target], ThrowableSubclass);
 * Only the last argument is required.
 * <br>This is a framework class that need not be used directly by Spring users.
 * 
 * @author Rod Johnson
 * @version $Id: ThrowsAdviceInterceptor.java,v 1.1 2003-12-11 14:51:37 johnsonr Exp $
 */
final class ThrowsAdviceInterceptor implements MethodInterceptor {
	
	private static final String AFTER_THROWING = "afterThrowing";
	
	private final Log logger = LogFactory.getLog(getClass());

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
		
		if (exceptionHandlerHash.isEmpty())
			throw new AopConfigException("At least one handler method must be found in class " + throwsAdvice.getClass());
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
		catch (IllegalArgumentException ex) {
			throw new AopConfigException("Internal error", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopConfigException("Internal error", ex);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}

}