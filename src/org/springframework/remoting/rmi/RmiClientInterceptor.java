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
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * Interceptor for accessing conventional RMI services or RMI invokers.
 * The service URL must be a valid RMI URL like "rmi://localhost:1099/myservice".
 *
 * <p>RMI invokers work at the RmiInvocationHandler level, needing only one stub
 * for any service. Service interfaces do not have to extend java.rmi.Remote or
 * throw RemoteException; Spring's unchecked RemoteAccessException will be thrown on
 * remote invocation failure. Of course, in and out parameters have to be serializable.
 *
 * <p>With conventional RMI services, this invoker is typically used with the RMI
 * service interface. Alternatively, this invoker can also proxy a remote RMI service
 * with a matching non-RMI business interface, i.e. an interface that mirrors the RMI
 * service methods but does not declare RemoteExceptions. In the latter case,
 * RemoteExceptions thrown by the RMI stub will automatically get converted to
 * Spring's unchecked RemoteAccessException.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see RmiServiceExporter
 * @see RmiInvocationHandler
 * @see org.springframework.remoting.RemoteAccessException
 * @see java.rmi.RemoteException
 * @see java.rmi.Remote
 */
public class RmiClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor, InitializingBean {

	private Remote rmiProxy;

	public void afterPropertiesSet() throws Exception {
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		Remote remoteObj = createRmiProxy();
		if (remoteObj instanceof RmiInvocationHandler) {
			logger.info("RMI object [" + getServiceUrl() + "] is an RMI invoker");
		}
		else if (getServiceInterface() != null) {
			boolean isImpl = getServiceInterface().isInstance(remoteObj);
			logger.info("Using service interface [" + getServiceInterface().getName() + "] for RMI object [" +
									getServiceUrl() + "] - " + (!isImpl ? "not " : "") + "directly implemented");
		}
		this.rmiProxy = remoteObj;
	}

	/**
	 * Create the RMI proxy. Default implementation looks up the service URL
	 * via java.rmi.Naming. Can be overridden in subclasses.
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote createRmiProxy() throws Exception {
		return Naming.lookup(getServiceUrl());
	}

	/**
	 * Return the underlying RMI proxy that this interceptor delegates to.
	 */
	protected Remote getRmiProxy() {
		return rmiProxy;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			if (this.rmiProxy instanceof RmiInvocationHandler) {
				RmiInvocationHandler invocationHandler = (RmiInvocationHandler) this.rmiProxy;
				return invoke(invocation, invocationHandler);
			}
			else {
				Method method = invocation.getMethod();
				if (method.getDeclaringClass().isInstance(this.rmiProxy)) {
					// directly implemented
					return method.invoke(this.rmiProxy, invocation.getArguments());
				}
				else {
					// not directly implemented
					Method proxyMethod = this.rmiProxy.getClass().getMethod(method.getName(), method.getParameterTypes());
					return proxyMethod.invoke(this.rmiProxy, invocation.getArguments());
				}
			}
		}
		catch (RemoteException ex) {
			logger.debug("RMI invoker for service [" + getServiceUrl() + "] threw exception", ex);
			if (!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access RMI invoker for [" + getServiceUrl() + "]", ex);
			}
			else {
				throw ex;
			}
		}
		catch (InvocationTargetException ex) {
			Throwable targetException = ex.getTargetException();
			logger.debug("RMI method of service [" + getServiceUrl() + "] threw exception", targetException);
			if (targetException instanceof RemoteException &&
					!Arrays.asList(invocation.getMethod().getExceptionTypes()).contains(RemoteException.class)) {
				throw new RemoteAccessException("Cannot access RMI service [" + getServiceUrl() + "]", targetException);
			}
			else {
				throw targetException;
			}
		}
		catch (RuntimeException ex) {
			throw new RemoteAccessException("Failed to invoke RMI service [" + getServiceUrl() + "]", ex);
		}
	}

	/**
	 * Apply the given AOP method invocation to the given RmiInvocationHandler.
	 * The default implementation calls invoke with a plain RemoteInvocation.
	 * <p>Can be overridden in subclasses to provide custom RemoteInvocation
	 * subclasses, containing additional invocation parameters like user
	 * credentials. Can also process the returned result object.
	 * @param methodInvocation the current AOP method invocation
	 * @param invocationHandler the RmiInvocationHandler to apply the invocation to
	 * @return the invocation result
	 * @throws NoSuchMethodException if the method name could not be resolved
	 * @throws IllegalAccessException if the method could not be accessed
	 * @throws InvocationTargetException if the method invocation resulted in an exception
	 * @see org.springframework.remoting.support.RemoteInvocation
	 */
	protected Object invoke(MethodInvocation methodInvocation, RmiInvocationHandler invocationHandler)
	    throws RemoteException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return invocationHandler.invoke(new RemoteInvocation(methodInvocation));
	}

}
