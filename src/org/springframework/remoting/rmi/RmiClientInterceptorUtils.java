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

package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.ConnectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * Factored-out methods for performing invocations within an RMI client.
 * Can handle both RMI and non-RMI service interfaces working on an RMI stub.
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class RmiClientInterceptorUtils {

	private static final Log logger = LogFactory.getLog(RmiClientInterceptorUtils.class);

	/**
	 * Apply the given method invocation to the given RMI stub.
	 * <p>Delegate to the corresponding method if the RMI stub does not directly
	 * implemented the invoked method. This typically happens when a non-RMI service
	 * interface is used for an RMI service. The methods of such a service interface
	 * have to match the RMI stub methods, but they typically don't declare
	 * java.rmi.RemoteException: A RemoteException thrown by the RMI stub will
	 * be automatically converted to Spring's RemoteAccessException.
	 * @param invocation the AOP MethodInvocation
	 * @param stub the RMI stub
	 * @param serviceName the name of the service (for debugging purposes)
	 * @return the invocation result, if any
	 * @throws Throwable exception to be thrown to the caller
	 * @see java.rmi.RemoteException
	 * @see org.springframework.remoting.RemoteAccessException
	 */
	public static Object invoke(MethodInvocation invocation, Remote stub, String serviceName) throws Throwable {
		try {
			return doInvoke(invocation, stub);
		}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx instanceof RemoteException) {
				throw convertRmiAccessException(invocation.getMethod(), (RemoteException) targetEx, serviceName);
			}
			else {
				throw targetEx;
			}
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke remote service [" + serviceName + "]", ex);
		}
	}

	/**
	 * Perform a raw method invocation on the given RMI stub,
	 * letting reflection exceptions through as-is.
	 * @param invocation the AOP MethodInvocation
	 * @param stub the RMI stub
	 * @return the invocation result, if any
	 * @throws NoSuchMethodException if thrown by reflection
	 * @throws IllegalAccessException if thrown by reflection
	 * @throws InvocationTargetException if thrown by reflection
	 */
	public static Object doInvoke(MethodInvocation invocation, Remote stub)
	    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = invocation.getMethod();
		if (method.getDeclaringClass().isInstance(stub)) {
			// directly implemented
			return method.invoke(stub, invocation.getArguments());
		}
		else {
			// not directly implemented
			Method stubMethod = stub.getClass().getMethod(method.getName(), method.getParameterTypes());
			return stubMethod.invoke(stub, invocation.getArguments());
		}
	}

	/**
	 * Convert the given RemoteException that happened during remote access
	 * to Spring's RemoteAccessException if the method signature does not
	 * support RemoteException. Else, return the original RemoteException.
	 * @param method the invoked method
	 * @param ex the RemoteException that happended
	 * @param serviceName the name of the service (for debugging purposes)
	 * @return the exception to be thrown to the caller
	 */
	public static Exception convertRmiAccessException(Method method, RemoteException ex, String serviceName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Remote service [" + serviceName + "] threw exception", ex);
		}
		if (!Arrays.asList(method.getExceptionTypes()).contains(RemoteException.class)) {
			if (ex instanceof ConnectException) {
				return new RemoteConnectFailureException("Cannot connect to remote service [" + serviceName + "]", ex);
			}
			else {
				return new RemoteAccessException("Cannot access remote service [" + serviceName + "]", ex);
			}
		}
		else {
			return ex;
		}
	}

	/**
	 * Wrap the given arbitrary exception that happened during remote access
	 * in either a RemoteException or a Spring RemoteAccessException (if the
	 * method signature does not support RemoteException).
	 * <p>Only call this for remote access exceptions, not for exceptions
	 * thrown by the target service itself!
	 * @param method the invoked method
	 * @param ex the exception that happened, to be used as cause for the
	 * RemoteAccessException respectively RemoteException
	 * @param message the message for the RemoteAccessException respectively
	 * RemoteException
	 * @return the exception to be thrown to the caller
	 */
	public static Exception convertRmiAccessException(Method method, Throwable ex, String message) {
		if (logger.isDebugEnabled()) {
			logger.debug(message, ex);
		}
		if (!Arrays.asList(method.getExceptionTypes()).contains(RemoteException.class)) {
			return new RemoteAccessException(message, ex);
		}
		else {
			return new RemoteException(message, ex);
		}
	}

}
