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

package org.springframework.remoting.caucho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.MalformedURLException;

import com.caucho.burlap.client.BurlapProxyFactory;
import com.caucho.burlap.client.BurlapRuntimeException;
import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * Interceptor for accessing a Burlap service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Burlap service.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>Note: Burlap services accessed with this proxy factory do not
 * have to be exported via BurlapServiceExporter, as there isn't
 * any special handling involved. Therefore, you can also access
 * services that are exported via Caucho's BurlapServlet.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see #setUsername
 * @see #setPassword
 * @see BurlapServiceExporter
 * @see BurlapProxyFactoryBean
 * @see com.caucho.burlap.client.BurlapProxyFactory
 */
public class BurlapClientInterceptor extends CauchoRemoteAccessor implements MethodInterceptor {

	private BurlapProxyFactory proxyFactory;

	private Object burlapProxy;


	/**
	 * Set the BurlapProxyFactory instance to use.
	 * If not specified, a default BurlapProxyFactory will be created.
	 * <p>Allows to use an externally configured factory instance,
	 * in particular a custom BurlapProxyFactory subclass.
	 */
	public void setProxyFactory(BurlapProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	/**
	 * Initialize the Burlap proxy for this interceptor.
	 */
	public void prepare() throws MalformedURLException {
		super.prepare();

		if (this.proxyFactory == null) {
			this.proxyFactory = new BurlapProxyFactory();
		}
		if (getUsername() != null) {
			this.proxyFactory.setUser(getUsername());
		}
		if (getPassword() != null) {
			this.proxyFactory.setPassword(getPassword());
		}
		if (isOverloadEnabled()) {
			this.proxyFactory.setOverloadEnabled(isOverloadEnabled());
		}

		this.burlapProxy = createBurlapProxy(this.proxyFactory);
	}

	/**
	 * Create the Burlap proxy that is wrapped by this interceptor.
	 * @param proxyFactory the proxy factory to use
	 * @return the Burlap proxy
	 * @throws MalformedURLException if thrown by the proxy factory
	 * @see com.caucho.burlap.client.BurlapProxyFactory#create
	 */
	protected Object createBurlapProxy(BurlapProxyFactory proxyFactory) throws MalformedURLException {
		return proxyFactory.create(getServiceInterface(), getServiceUrl());
	}


	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (this.burlapProxy == null) {
			throw new IllegalStateException("BurlapClientInterceptor is not properly initialized - " +
					"invoke 'prepare' before attempting any operations");
		}

		try {
			return invocation.getMethod().invoke(this.burlapProxy, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof BurlapRuntimeException) {
				BurlapRuntimeException bre = (BurlapRuntimeException) ex.getTargetException();
				Throwable rootCause = (bre.getRootCause() != null) ? bre.getRootCause() : bre;
				throw convertBurlapAccessException(rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw convertBurlapAccessException(utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke Burlap service [" + getServiceUrl() + "]", ex);
		}
	}

	/**
	 * Convert the given Burlap access exception to an appropriate
	 * Spring RemoteAccessException.
	 * @param ex the exception to convert
	 * @return the RemoteAccessException to throw
	 */
	protected RemoteAccessException convertBurlapAccessException(Throwable ex) {
		if (ex instanceof ConnectException) {
			throw new RemoteConnectFailureException(
					"Cannot connect to Burlap service at [" + getServiceUrl() + "]", ex);
		}
		else {
			throw new RemoteAccessException(
			    "Cannot access Burlap service at [" + getServiceUrl() + "]", ex);
		}
	}

}
