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

package org.springframework.remoting.caucho;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * Interceptor for accessing a Hessian service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Hessian service.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>
 *
 * <p>Note: Hessian services accessed with this proxy factory do not have to be
 * exported via HessianServiceExporter, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class HessianClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor, InitializingBean {

	private final HessianProxyFactory proxyFactory = new HessianProxyFactory();

	private Object hessianProxy;

	/**
	 * Set the username that this factory should use to access the remote service.
	 */
	public void setUsername(String username) {
		this.proxyFactory.setUser(username);
	}

	/**
	 * Set the password that this factory should use to access the remote service.
	 */
	public void setPassword(String password) {
		this.proxyFactory.setPassword(password);
	}

	public void afterPropertiesSet() throws MalformedURLException {
		if (getServiceInterface() == null) {
			throw new IllegalArgumentException("serviceInterface is required");
		}
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		this.hessianProxy = this.proxyFactory.create(getServiceInterface(), getServiceUrl());
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.getMethod().invoke(this.hessianProxy, invocation.getArguments());
		}
		catch (UndeclaredThrowableException ex) {
			throw new RemoteAccessException("Cannot access Hessian service", ex.getUndeclaredThrowable());
		}
		catch (InvocationTargetException ex) {
			logger.debug("Hessian service [" + getServiceUrl() + "] threw exception", ex.getTargetException());
			if (ex.getTargetException() instanceof HessianRuntimeException) {
				HessianRuntimeException hre = (HessianRuntimeException) ex.getTargetException();
				Throwable rootCause = (hre.getRootCause() != null) ? hre.getRootCause() : hre;
				throw new RemoteAccessException("Cannot access Hessian service", rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new RemoteAccessException("Cannot access Hessian service", utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
	}

}
