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

package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jndi.JndiObjectLocator;
import org.springframework.remoting.RemoteConnectFailureException;
import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;

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

	private RemoteInvocationFactory remoteInvocationFactory = new DefaultRemoteInvocationFactory();

	private boolean lookupStubOnStartup = true;

	private boolean cacheStub = true;

	private boolean refreshStubOnConnectFailure = false;

	private Remote cachedStub;


	/**
	 * Set the interface of the service to access.
	 * The interface must be suitable for the particular service and remoting tool.
	 * <p>Typically required to be able to create a suitable service proxy,
	 * but can also be optional if the lookup returns a typed stub.
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
	 * Set the RemoteInvocationFactory to use for this accessor.
	 * Default is a DefaultRemoteInvocationFactory.
	 * <p>A custom invocation factory can add further context information
	 * to the invocation, for example user credentials.
	 */
	public void setRemoteInvocationFactory(RemoteInvocationFactory remoteInvocationFactory) {
		this.remoteInvocationFactory = remoteInvocationFactory;
	}

	/**
	 * Return the RemoteInvocationFactory used by this accessor.
	 */
	public RemoteInvocationFactory getRemoteInvocationFactory() {
		return remoteInvocationFactory;
	}

	/**
	 * Set whether to look up the RMI stub on startup. Default is "true".
	 * <p>Can be turned off to allow for late start of the RMI server.
	 * In this case, the RMI stub will be fetched on first access.
	 * @see #setCacheStub
	 */
	public void setLookupStubOnStartup(boolean lookupStubOnStartup) {
		this.lookupStubOnStartup = lookupStubOnStartup;
	}

	/**
	 * Set whether to cache the RMI stub once it has been located.
	 * Default is "true".
	 * <p>Can be turned off to allow for hot restart of the RMI server.
	 * In this case, the RMI stub will be fetched for each invocation.
	 * @see #setLookupStubOnStartup
	 */
	public void setCacheStub(boolean cacheStub) {
		this.cacheStub = cacheStub;
	}

	/**
	 * Set whether to refresh the RMI stub on connect failure.
	 * Default is "false".
	 * <p>Can be turned on to allow for hot restart of the RMI server.
	 * If a cached RMI stub throws an RMI exception that indicates a
	 * remote connect failure, a fresh proxy will be fetched and the
	 * invocation will be retried.
	 * @see java.rmi.ConnectException
	 * @see java.rmi.ConnectIOException
	 * @see java.rmi.NoSuchObjectException
	 */
	public void setRefreshStubOnConnectFailure(boolean refreshStubOnConnectFailure) {
		this.refreshStubOnConnectFailure = refreshStubOnConnectFailure;
	}


	/**
	 * Fetch the RMI stub on startup, if necessary.
	 * @see #prepare()
	 */
	public void afterPropertiesSet() throws NamingException {
		super.afterPropertiesSet();
		prepare();
	}

	/**
	 * Fetches RMI stub on startup, if necessary.
	 * @throws NamingException if thrown by JNDI API
	 * @see #setLookupStubOnStartup
	 * @see #lookupStub
	 */
	public void prepare() throws NamingException {
		// Cache RMI stub on initialization?
		if (this.lookupStubOnStartup) {
			Remote stub = lookupStub();
			if (this.cacheStub) {
				this.cachedStub = stub;
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
	 * @throws NamingException if stub creation failed
	 * @see #setCacheStub
	 * @see #getStub()
	 * @see java.rmi.Naming#lookup
	 */
	protected Remote lookupStub() throws NamingException {
		Object stub = lookup();
		if (getServiceInterface() != null && Remote.class.isAssignableFrom(getServiceInterface())) {
			stub = PortableRemoteObject.narrow(stub, getServiceInterface());
		}
		if (!(stub instanceof Remote)) {
			throw new NamingException("Located RMI stub of class [" + stub.getClass().getName() +
					"], with JNDI name [" + getJndiName() + "], does not implement interface [java.rmi.Remote]");
		}
		// TODO: narrow to RmiInvocationHandler?
		return (Remote) stub;
	}

	/**
	 * Return the RMI stub to use. Called for each invocation.
	 * <p>Default implementation returns the stub created on initialization,
	 * if any; else, it invokes lookupStub to get a new stub for each invocation.
	 * <p>Can be overridden in subclasses, for example to cache a stub for
	 * a given amount of time before recreating it, or to test the stub
	 * whether it is still alive.
	 * @return the RMI stub to use for an invocation
	 * @throws NamingException if stub creation failed
	 * @see #lookupStub
	 */
	protected Remote getStub() throws NamingException {
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
	 * refreshAndRetry on corresponding RMI exceptions.
	 * @see #getStub
	 * @see #doInvoke
	 * @see #refreshAndRetry
	 * @see java.rmi.ConnectException
	 * @see java.rmi.ConnectIOException
	 * @see java.rmi.NoSuchObjectException
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Remote stub = null;
		try {
			stub = getStub();
		}
		catch (Throwable ex) {
			throw new RemoteLookupFailureException("RMI lookup for service [" + getJndiName() + "] failed", ex);
		}
		try {
			return doInvoke(invocation, stub);
		}
		catch (RemoteConnectFailureException ex) {
			return handleRemoteConnectFailure(invocation, ex);
		}
		catch (RemoteException ex) {
			if (isConnectFailure(ex)) {
				return handleRemoteConnectFailure(invocation, ex);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Determine whether the given RMI exception indicates a connect failure.
	 * Default implementation delegates to RmiClientInterceptorUtils.
	 * @param ex the RMI exception to check
	 * @return whether the exception should be treated as connect failure
	 * @see org.springframework.remoting.rmi.RmiClientInterceptorUtils#isConnectFailure
	 */
	protected boolean isConnectFailure(RemoteException ex) {
		return RmiClientInterceptorUtils.isConnectFailure(ex);
	}

	private Object handleRemoteConnectFailure(MethodInvocation invocation, Exception ex) throws Throwable {
		if (this.refreshStubOnConnectFailure) {
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
				throw new RemoteLookupFailureException("RMI lookup for service [" + getJndiName() + "] failed", ex);
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
				    invocation.getMethod(), ex, isConnectFailure(ex), getJndiName());
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			catch (Throwable ex) {
				throw new AspectException("Failed to invoke remote service [" + getJndiName() + "]", ex);
			}
		}
		else {
			// traditional RMI stub
			try {
				return RmiClientInterceptorUtils.doInvoke(invocation, stub);
			}
			catch (InvocationTargetException ex) {
				Throwable targetEx = ex.getTargetException();
				if (targetEx instanceof RemoteException) {
					RemoteException rex = (RemoteException) targetEx;
					throw RmiClientInterceptorUtils.convertRmiAccessException(
							invocation.getMethod(), rex, isConnectFailure(rex), getJndiName());
				}
				else {
					throw targetEx;
				}
			}
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

		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return "RMI invoker proxy for service URL [" + getJndiName() + "]";
		}

		return invocationHandler.invoke(createRemoteInvocation(methodInvocation));
	}

	/**
	 * Create a new RemoteInvocation object for the given AOP method invocation.
	 * The default implementation delegates to the RemoteInvocationFactory.
	 * <p>Can be overridden in subclasses to provide custom RemoteInvocation
	 * subclasses, containing additional invocation parameters like user credentials.
	 * Note that it is preferable to use a custom RemoteInvocationFactory which
	 * is a reusable strategy.
	 * @param methodInvocation the current AOP method invocation
	 * @return the RemoteInvocation object
	 * @see RemoteInvocationFactory#createRemoteInvocation
	 */
	protected RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
		return getRemoteInvocationFactory().createRemoteInvocation(methodInvocation);
	}

}
