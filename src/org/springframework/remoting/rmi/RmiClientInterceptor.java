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
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;

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
 * @see RmiProxyFactoryBean
 * @see RmiInvocationHandler
 * @see org.springframework.remoting.RemoteAccessException
 * @see java.rmi.RemoteException
 * @see java.rmi.Remote
 */
public class RmiClientInterceptor extends RemoteInvocationBasedAccessor
		implements MethodInterceptor, InitializingBean {

	private boolean lookupStubOnStartup = true;

	private boolean cacheStub = true;

	private boolean refreshStubOnConnectFailure = false;

	private Remote cachedStub;


	/**
	 * Set whether to look up the RMI stub on startup. Default is true.
	 * <p>Can be turned off to allow for late start of the RMI server.
	 * In this case, the RMI stub will be fetched on first access.
	 * @see #setCacheStub
	 */
	public void setLookupStubOnStartup(boolean lookupStubOnStartup) {
		this.lookupStubOnStartup = lookupStubOnStartup;
	}

	/**
	 * Set whether to cache the RMI stub once it has been located.
	 * Default is true.
	 * <p>Can be turned off to allow for hot restart of the RMI server.
	 * In this case, the RMI stub will be fetched for each invocation.
	 * @see #setLookupStubOnStartup
	 */
	public void setCacheStub(boolean cacheStub) {
		this.cacheStub = cacheStub;
	}

	/**
	 * Set whether to refresh the RMI stub on connect failure.
	 * Default is false.
	 * <p>Can be turned on to allow for hot restart of the RMI server.
	 * If a cached RMI stub throws a ConnectException, a fresh proxy
	 * will be fetched and the invocation will be retried.
	 * @see java.rmi.ConnectException
	 */
	public void setRefreshStubOnConnectFailure(boolean refreshStubOnConnectFailure) {
		this.refreshStubOnConnectFailure = refreshStubOnConnectFailure;
	}


	/**
	 * Fetches RMI stub on startup, if necessary.
	 * @see #setLookupStubOnStartup
	 * @see #lookupStub
	 */
	public void afterPropertiesSet() throws Exception {
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		// cache RMI stub on initialization?
		if (this.lookupStubOnStartup) {
			Remote remoteObj = lookupStub();
			if (logger.isInfoEnabled()) {
				if (remoteObj instanceof RmiInvocationHandler) {
					logger.info("RMI stub [" + getServiceUrl() + "] is an RMI invoker");
				}
				else if (getServiceInterface() != null) {
					boolean isImpl = getServiceInterface().isInstance(remoteObj);
					logger.info("Using service interface [" + getServiceInterface().getName() +
					    "] for RMI stub [" + getServiceUrl() + "] - " +
					    (!isImpl ? "not " : "") + "directly implemented");
				}
			}
			if (this.cacheStub) {
				this.cachedStub = remoteObj;
			}
		}
	}

	/**
	 * Create the RMI stub, typically by looking it up.
	 * Called on interceptor initialization if cacheStub is true;
	 * else called for each invocation by getStub().
	 * <p>Default implementation looks up the service URL via java.rmi.Naming.
	 * Can be overridden in subclasses.
	 * @return the RMI stub to store in this interceptor
	 * @throws Exception if proxy creation failed
	 * @see #setCacheStub
	 * @see #getStub()
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote lookupStub() throws Exception {
		Remote stub = Naming.lookup(getServiceUrl());
		if (logger.isInfoEnabled()) {
			logger.info("Located object with RMI URL [" + getServiceUrl() + "]: value=[" + stub + "]");
		}
		return stub;
	}

	/**
	 * Return the RMI stub to use. Called for each invocation.
	 * <p>Default implementation returns the proxy created on initialization,
	 * if any; else, it invokes lookupStub to get a new proxy for each invocation.
	 * <p>Can be overridden in subclasses, for example to cache a proxy for
	 * a given amount of time before recreating it, or to test the proxy
	 * whether it is still alive.
	 * @return the RMI stub to use for an invocation
	 * @throws Exception if proxy creation failed
	 * @see #lookupStub
	 */
	protected Remote getStub() throws Exception {
		if (!this.cacheStub || (this.lookupStubOnStartup && !this.refreshStubOnConnectFailure)) {
			return (this.cachedStub != null ? this.cachedStub : lookupStub());
		}
		else {
			synchronized (this) {
				if (this.cachedStub == null) {
					this.cachedStub = lookupStub();
				}
				return this.cachedStub;
			}
		}
	}


	/**
	 * Fetches an RMI stub and delegates to doInvoke.
	 * If configured to refresh on connect failure, it will call
	 * refreshAndRetry on ConnectException.
	 * @see #getStub
	 * @see #doInvoke(MethodInvocation, Remote)
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Remote stub = null;
		try {
			stub = getStub();
		}
		catch (Throwable ex) {
			throw new RemoteLookupFailureException("RMI lookup for service [" + getServiceUrl() + "] failed", ex);
		}
		try {
			return doInvoke(invocation, stub);
		}
		catch (RemoteConnectFailureException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
		catch (ConnectException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
	}

	private Object handleRemoteConnectFailure(MethodInvocation invocation, Exception ex) throws Throwable {
		if (this.refreshStubOnConnectFailure) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not connect to RMI service [" + getServiceUrl() + "] - retrying", ex);
			}
			else if (logger.isWarnEnabled()) {
				logger.warn("Could not connect to RMI service [" + getServiceUrl() + "] - retrying");
			}
			return refreshAndRetry(invocation);
		}
		else {
			throw ex;
		}
	}

	/**
	 * Refresh the RMI stub and retry the given invocation.
	 * Called by invoke on connect failure.
	 * @param invocation the AOP method invocation
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 * @see #invoke
	 */
	protected Object refreshAndRetry(MethodInvocation invocation) throws Throwable {
		Remote freshStub = null;
		synchronized (this) {
			try {
				freshStub = lookupStub();
				if (this.cacheStub) {
					this.cachedStub = freshStub;
				}
			}
			catch (Throwable ex) {
				throw new RemoteLookupFailureException("RMI lookup for service [" + getServiceUrl() + "] failed", ex);
			}
		}
		return doInvoke(invocation, freshStub);
	}

	/**
	 * Perform the given invocation on the given RMI stub.
	 * @param invocation the AOP method invocation
	 * @param stub the RMI stub to invoke
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 */
	protected Object doInvoke(MethodInvocation invocation, Remote stub) throws Throwable {
		if (stub instanceof RmiInvocationHandler) {
			// RMI invoker
			try {
				return doInvoke(invocation, (RmiInvocationHandler) stub);
			}
			catch (RemoteException ex) {
				throw RmiClientInterceptorUtils.convertRmiAccessException(
				    invocation.getMethod(), ex, getServiceUrl());
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			catch (Throwable ex) {
				throw new AspectException("Failed to invoke remote service [" + getServiceUrl() + "]", ex);
			}
		}
		else {
			// traditional RMI stub
			return RmiClientInterceptorUtils.invoke(invocation, stub, getServiceUrl());
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
	protected Object doInvoke(MethodInvocation methodInvocation, RmiInvocationHandler invocationHandler)
	    throws RemoteException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return invocationHandler.invoke(createRemoteInvocation(methodInvocation));
	}

}
