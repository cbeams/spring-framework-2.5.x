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

import java.rmi.ConnectException;
import java.rmi.Remote;

import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiObjectLocator;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;

/**
 * Interceptor for accessing RMI services from JNDI.
 * Typically used for RMI-IIOP (CORBA), but can also be used for EJB home objects
 * (for example, a Stateful Session Bean home). In contrast to a plain JNDI lookup,
 * this accessor also performs narrowing through PortableRemoteObject.
 *
 * <p>With conventional RMI services, this invoker is typically used with the RMI
 * service interface. Alternatively, this invoker can also proxy a remote RMI service
 * with a matching non-RMI business interface, i.e. an interface that mirrors the RMI
 * service methods but does not declare RemoteExceptions. In the latter case,
 * RemoteExceptions thrown by the RMI stub will automatically get converted to
 * Spring's unchecked RemoteAccessException.
 *
 * <p>The JNDI environment can be specified as jndiEnvironment property,
 * or be configured in a jndi.properties file or as system properties.
 * For example:
 *
 * <pre>
 * &lt;property name="jndiEnvironment"&gt;
 * 	 &lt;props>
 *		 &lt;prop key="java.naming.factory.initial"&gt;com.sun.jndi.cosnaming.CNCtxFactory&lt;/prop&gt;
 *		 &lt;prop key="java.naming.provider.url"&gt;iiop://localhost:1050&lt;/prop&gt;
 *	 &lt;/props&gt;
 * &lt;/property&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setJndiTemplate
 * @see #setJndiEnvironment
 * @see #setJndiName
 * @see JndiRmiServiceExporter
 * @see JndiRmiProxyFactoryBean
 * @see org.springframework.remoting.RemoteAccessException
 * @see java.rmi.RemoteException
 * @see java.rmi.Remote
 * @see javax.rmi.PortableRemoteObject#narrow
 */
public class JndiRmiClientInterceptor extends JndiObjectLocator
    implements MethodInterceptor, InitializingBean {

	private Class serviceInterface;

	private boolean lookupRmiProxyOnStartup = true;

	private boolean cacheRmiProxy = true;

	private boolean refreshRmiProxyOnConnectFailure = false;

	private Remote cachedRmiProxy;


	/**
	 * Set the interface of the service to access.
	 * The interface must be suitable for the particular service and remoting tool.
	 * <p>Typically required to be able to create a suitable service proxy,
	 * but can also be optional if the lookup returns a typed proxy.
	 */
	public void setServiceInterface(Class serviceInterface) {
		if (serviceInterface != null && !serviceInterface.isInterface()) {
			throw new IllegalArgumentException("serviceInterface must be an interface");
		}
		this.serviceInterface = serviceInterface;
	}

	/**
	 * Return the interface of the service to access.
	 */
	public Class getServiceInterface() {
		return serviceInterface;
	}

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
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		// cache RMI proxy on initialization?
		if (this.lookupRmiProxyOnStartup) {
			Remote rmiProxy = lookupRmiProxy();
			if (this.cacheRmiProxy) {
				this.cachedRmiProxy = rmiProxy;
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
	 * @throws NamingException if proxy creation failed
	 * @see #setCacheRmiProxy
	 * @see #getRmiProxy()
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote lookupRmiProxy() throws NamingException {
		Object proxy = lookup();
		if (getServiceInterface() != null && Remote.class.isAssignableFrom(getServiceInterface())) {
			proxy = PortableRemoteObject.narrow(proxy, getServiceInterface());
		}
		if (!(proxy instanceof Remote)) {
			throw new AspectException("Located RMI proxy [" + proxy + "] does not implement java.rmi.Remote");
		}
		return (Remote) proxy;
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
	 * @throws NamingException if proxy creation failed
	 * @see #lookupRmiProxy
	 */
	protected Remote getRmiProxy() throws NamingException {
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
	 * @see #doInvoke
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Remote rmiProxy = null;
		try {
			rmiProxy = getRmiProxy();
		}
		catch (Throwable ex) {
			throw new RemoteLookupFailureException("RMI lookup for service [" + getJndiName() + "] failed", ex);
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
				logger.debug("Could not connect to RMI service [" + getJndiName() + "] - retrying", ex);
			}
			else if (logger.isWarnEnabled()) {
				logger.warn("Could not connect to RMI service [" + getJndiName() + "] - retrying");
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
				throw new RemoteLookupFailureException("RMI lookup for service [" + getJndiName() + "] failed", ex);
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
		// traditional RMI proxy invocation
		return RmiClientInterceptorUtils.invoke(invocation, rmiProxy, getJndiName());
	}

}
