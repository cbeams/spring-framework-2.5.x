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

	private boolean lookupRmiProxyOnStartup = true;

	private boolean cacheRmiProxy = true;

	private boolean refreshRmiProxyOnConnectFailure = false;

	private Remote cachedRmiProxy;


	/**
	 * Set whether to look up the RMI proxy on startup. Default is true.
	 * <p>Can be turned off to allow for late start of the RMI server.
	 * In this case, the RMI proxy will be fetched on first access.
	 * @see #setCacheRmiProxy
	 */
	public void setLookupRmiProxyOnStartup(boolean lookupRmiProxyOnStartup) {
		this.lookupRmiProxyOnStartup = lookupRmiProxyOnStartup;
	}

	/**
	 * Set whether to cache the RMI proxy once it has been located.
	 * Default is true.
	 * <p>Can be turned off to allow for hot restart of the RMI server.
	 * In this case, the RMI proxy will be fetched for each invocation.
	 * @see #setLookupRmiProxyOnStartup
	 */
	public void setCacheRmiProxy(boolean cacheRmiProxy) {
		this.cacheRmiProxy = cacheRmiProxy;
	}

	/**
	 * Set whether to refresh the RMI proxy on connect failure.
	 * Default is false.
	 * <p>Can be turned on to allow for hot restart of the RMI server.
	 * If a cached RMI proxy throws a ConnectException, a fresh proxy
	 * will be fetched and the invocation will be retried.
	 * @see java.rmi.ConnectException
	 */
	public void setRefreshRmiProxyOnConnectFailure(boolean refreshRmiProxyOnConnectFailure) {
		this.refreshRmiProxyOnConnectFailure = refreshRmiProxyOnConnectFailure;
	}


	/**
	 * Fetches RMI proxy on startup, if necessary.
	 * @see #setLookupRmiProxyOnStartup
	 * @see #lookupRmiProxy
	 */
	public void afterPropertiesSet() throws Exception {
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		// cache RMI proxy on initialization?
		if (this.lookupRmiProxyOnStartup) {
			Remote remoteObj = lookupRmiProxy();
			if (logger.isInfoEnabled()) {
				if (remoteObj instanceof RmiInvocationHandler) {
					logger.info("RMI object [" + getServiceUrl() + "] is an RMI invoker");
				}
				else if (getServiceInterface() != null) {
					boolean isImpl = getServiceInterface().isInstance(remoteObj);
					logger.info("Using service interface [" + getServiceInterface().getName() +
					    "] for RMI object [" + getServiceUrl() + "] - " +
					    (!isImpl ? "not " : "") + "directly implemented");
				}
			}
			if (this.cacheRmiProxy) {
				this.cachedRmiProxy = remoteObj;
			}
		}
	}

	/**
	 * Create the RMI proxy, typically by looking it up.
	 * Called on interceptor initialization if cacheRmiProxy is true;
	 * else called for each invocation by getRmiProxy().
	 * <p>Default implementation looks up the service URL via java.rmi.Naming.
	 * Can be overridden in subclasses.
	 * @return the RMI proxy to store in this interceptor
	 * @throws Exception if proxy creation failed
	 * @see #setCacheRmiProxy
	 * @see #getRmiProxy()
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote lookupRmiProxy() throws Exception {
		Remote rmiProxy = Naming.lookup(getServiceUrl());
		if (logger.isInfoEnabled()) {
			logger.info("Located object with RMI URL [" + getServiceUrl() + "]: value=[" + rmiProxy + "]");
		}
		return rmiProxy;
	}

	/**
	 * Return the RMI proxy to use. Called for each invocation.
	 * <p>Default implementation returns the proxy created on initialization,
	 * if any; else, it invokes createRmiProxy to get a new proxy for each
	 * invocation.
	 * <p>Can be overridden in subclasses, for example to cache a proxy for
	 * a given amount of time before recreating it, or to test the proxy
	 * whether it is still alive.
	 * @return the RMI proxy to use for an invocation
	 * @throws Exception if proxy creation failed
	 * @see #lookupRmiProxy
	 */
	protected Remote getRmiProxy() throws Exception {
		if (!this.cacheRmiProxy || (this.lookupRmiProxyOnStartup && !this.refreshRmiProxyOnConnectFailure)) {
			return (this.cachedRmiProxy != null ? this.cachedRmiProxy : lookupRmiProxy());
		}
		else {
			synchronized (this) {
				if (this.cachedRmiProxy == null) {
					this.cachedRmiProxy = lookupRmiProxy();
				}
				return this.cachedRmiProxy;
			}
		}
	}


	/**
	 * Fetches an RMI proxy and delegates to doInvoke.
	 * If configured to refresh on connect failure, it will call
	 * refreshAndRetry on ConnectException.
	 * @see #getRmiProxy
	 * @see #doInvoke(MethodInvocation, Remote)
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Remote rmiProxy = null;
		try {
			rmiProxy = getRmiProxy();
		}
		catch (Throwable ex) {
			throw new RemoteLookupFailureException("RMI lookup for service [" + getServiceUrl() + "] failed", ex);
		}
		try {
			return doInvoke(invocation, rmiProxy);
		}
		catch (RemoteConnectFailureException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
		catch (ConnectException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
	}

	private Object handleRemoteConnectFailure(MethodInvocation invocation, Exception ex) throws Throwable {
		if (this.refreshRmiProxyOnConnectFailure) {
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
	 * Refresh the RMI proxy and retry the given invocation.
	 * Called by invoke on connect failure.
	 * @param invocation the AOP method invocation
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 * @see #invoke
	 */
	protected Object refreshAndRetry(MethodInvocation invocation) throws Throwable {
		Remote freshRmiProxy = null;
		synchronized (this) {
			try {
				freshRmiProxy = lookupRmiProxy();
				if (this.cacheRmiProxy) {
					this.cachedRmiProxy = freshRmiProxy;
				}
			}
			catch (Throwable ex) {
				throw new RemoteLookupFailureException("RMI lookup for service [" + getServiceUrl() + "] failed", ex);
			}
		}
		return doInvoke(invocation, freshRmiProxy);
	}

	/**
	 * Perform the given invocation on the given RMI proxy.
	 * @param invocation the AOP method invocation
	 * @param rmiProxy the RMI proxy to invoke
	 * @return the invocation result, if any
	 * @throws Throwable in case of invocation failure
	 */
	protected Object doInvoke(MethodInvocation invocation, Remote rmiProxy) throws Throwable {
		if (rmiProxy instanceof RmiInvocationHandler) {
			// RMI invoker
			try {
				return doInvoke(invocation, (RmiInvocationHandler) rmiProxy);
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
			// traditional RMI proxy
			return RmiClientInterceptorUtils.invoke(invocation, rmiProxy, getServiceUrl());
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
