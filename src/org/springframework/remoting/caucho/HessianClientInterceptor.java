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

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

/**
 * Interceptor for accessing a Hessian service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Hessian service.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>Note: Hessian services accessed with this proxy factory do not
 * have to be exported via HessianServiceExporter, as there isn't
 * any special handling involved. Therefore, you can also access
 * services that are exported via Caucho's HessianServlet.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see #setUsername
 * @see #setPassword
 * @see HessianServiceExporter
 * @see HessianProxyFactoryBean
 * @see com.caucho.hessian.client.HessianProxyFactory
 */
public class HessianClientInterceptor extends CauchoRemoteAccessor implements MethodInterceptor {

	private HessianProxyFactory proxyFactory;

	private Object hessianProxy;


	/**
	 * Set the HessianProxyFactory instance to use.
	 * If not specified, a default HessianProxyFactory will be created.
	 * <p>Allows to use an externally configured factory instance,
	 * in particular a custom HessianProxyFactory subclass.
	 */
	public void setProxyFactory(HessianProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	/**
	 * Initialize the Hessian proxy for this interceptor.
	 */
	public void prepare() throws MalformedURLException {
		super.prepare();

		if (this.proxyFactory == null) {
			this.proxyFactory = new HessianProxyFactory();
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

		this.hessianProxy = createHessianProxy(this.proxyFactory);
	}

	/**
	 * Create the Hessian proxy that is wrapped by this interceptor.
	 * @param proxyFactory the proxy factory to use
	 * @return the Hessian proxy
	 * @throws MalformedURLException if thrown by the proxy factory
	 * @see com.caucho.hessian.client.HessianProxyFactory#create
	 */
	protected Object createHessianProxy(HessianProxyFactory proxyFactory) throws MalformedURLException {
		return proxyFactory.create(getServiceInterface(), getServiceUrl());
	}


	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (this.hessianProxy == null) {
			throw new IllegalStateException("HessianClientInterceptor is not properly initialized - " +
					"invoke 'prepare' before attempting any operations");
		}

		try {
			return invocation.getMethod().invoke(this.hessianProxy, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				HessianRuntimeException hre = (HessianRuntimeException) ex.getTargetException();
				Throwable rootCause = (hre.getRootCause() != null) ? hre.getRootCause() : hre;
				throw convertHessianAccessException(rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw convertHessianAccessException(utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke Hessian service [" + getServiceUrl() + "]", ex);
		}
	}

	/**
	 * Convert the given Hessian access exception to an appropriate
	 * Spring RemoteAccessException.
	 * @param ex the exception to convert
	 * @return the RemoteAccessException to throw
	 */
	protected RemoteAccessException convertHessianAccessException(Throwable ex) {
		if (ex instanceof ConnectException) {
			throw new RemoteConnectFailureException(
					"Cannot connect to Hessian service at [" + getServiceUrl() + "]", ex);
		}
		else {
			throw new RemoteAccessException(
			    "Cannot access Hessian service at [" + getServiceUrl() + "]", ex);
		}
	}

}
