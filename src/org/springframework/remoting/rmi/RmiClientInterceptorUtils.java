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
import java.rmi.RemoteException;
import java.util.Arrays;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.remoting.RemoteAccessException;

/**
 * Factored-out methods for performing invocations within an RMI client.
 * Can handle both RMI and non-RMI service interfaces working on an RMI proxy.
 * @author Juergen Hoeller
 * @since 1.1
 */
public abstract class RmiClientInterceptorUtils {

	private static final Log logger = LogFactory.getLog(RmiClientInterceptorUtils.class);

	/**
	 * Apply the given method invocation to the given RMI proxy.
	 * <p>Delegate to the corresponding method if the RMI proxy does not directly
	 * implemented the invoked method. This typically happens when a non-RMI service
	 * interface is used for an RMI service. The methods of such a service interface
	 * have to match the RMI proxy methods, but they typically don't declare
	 * java.rmi.RemoteException: A RemoteException thrown by the RMI proxy will
	 * be automatically converted to Spring's RemoteAccessException.
	 * @param invocation the AOP MethodInvocation
	 * @param rmiProxy the RMI proxy
	 * @param serviceName the name of the service (for debugging purposes)
	 * @return the invocation result, if any
	 * @throws Throwable exception to be thrown to the caller
	 * @see java.rmi.RemoteException
	 * @see org.springframework.remoting.RemoteAccessException
	 */
	public static Object invoke(MethodInvocation invocation, Object rmiProxy, String serviceName) throws Throwable {
		try {
			Method method = invocation.getMethod();
			if (method.getDeclaringClass().isInstance(rmiProxy)) {
				// directly implemented
				return method.invoke(rmiProxy, invocation.getArguments());
			}
			else {
				// not directly implemented
				Method proxyMethod = rmiProxy.getClass().getMethod(method.getName(), method.getParameterTypes());
				return proxyMethod.invoke(rmiProxy, invocation.getArguments());
			}
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			if (logger.isDebugEnabled()) {
				logger.debug("Remote method of service [" + serviceName + "] threw exception", targetException);
			}
			if (targetException instanceof RemoteException &&
					!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access remote service [" + serviceName + "]", targetException);
			}
			else {
				throw targetException;
			}
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke remote service [" + serviceName + "]", ex);
		}
	}

}
