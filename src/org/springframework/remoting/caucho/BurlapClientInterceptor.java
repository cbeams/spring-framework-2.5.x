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

import com.caucho.burlap.client.BurlapProxyFactory;
import com.caucho.burlap.client.BurlapRuntimeException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * Interceptor for accessing a Burlap service.
 * Supports authentication via username and password.
 * The service URL must be an HTTP URL exposing a Burlap service.
 *
 * <p>Burlap is a slim, XML-based RPC protocol.
 * For information on Burlap, see the
 * <a href="http://www.caucho.com/burlap">Burlap website</a>
 *
 * <p>Note: Burlap services accessed with this proxy factory do not have to be
 * exported via BurlapServiceExporter, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class BurlapClientInterceptor extends UrlBasedRemoteAccessor implements MethodInterceptor, InitializingBean {

	private final BurlapProxyFactory proxyFactory = new BurlapProxyFactory();

	private Object burlapProxy;

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
		this.burlapProxy = this.proxyFactory.create(getServiceInterface(), getServiceUrl());
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.getMethod().invoke(this.burlapProxy, invocation.getArguments());
		}
		catch (InvocationTargetException ex) {
			logger.debug("Burlap service [" + getServiceUrl() + "] threw exception", ex.getTargetException());
			if (ex.getTargetException() instanceof BurlapRuntimeException) {
				BurlapRuntimeException bre = (BurlapRuntimeException) ex.getTargetException();
				Throwable rootCause = (bre.getRootCause() != null) ? bre.getRootCause() : bre;
				throw new RemoteAccessException("Cannot access Burlap service", rootCause);
			}
			else if (ex.getTargetException() instanceof UndeclaredThrowableException) {
				UndeclaredThrowableException utex = (UndeclaredThrowableException) ex.getTargetException();
				throw new RemoteAccessException("Cannot access Burlap service", utex.getUndeclaredThrowable());
			}
			throw ex.getTargetException();
		}
	}

}
