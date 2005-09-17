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

import java.net.MalformedURLException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

/**
 * Common base class for Hessian/Burlap accessors (HessianClientInterceptor
 * and BurlapClientInterceptor), factoring out common properties.
 *
 * @author Juergen Hoeller
 * @since 1.1.4
 * @see HessianClientInterceptor
 * @see BurlapClientInterceptor
 */
public class CauchoRemoteAccessor extends UrlBasedRemoteAccessor implements InitializingBean {

	private String username;

	private String password;

	private boolean overloadEnabled;


	/**
	 * Set the username that this factory should use to access the remote service.
	 * Default is none.
	 * <p>The username will be sent by Hessian/Burlap via HTTP Basic Authentication.
	 * @see com.caucho.hessian.client.HessianProxyFactory#setUser
	 * @see com.caucho.burlap.client.BurlapProxyFactory#setUser
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Set the password that this factory should use to access the remote service.
	 * Default is none.
	 * <p>The password will be sent by Hessian/Burlap via HTTP Basic Authentication.
	 * @see com.caucho.hessian.client.HessianProxyFactory#setPassword
	 * @see com.caucho.burlap.client.BurlapProxyFactory#setPassword
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Set whether overloaded methods should be enabled for remote invocations.
	 * Default is "false".
	 * @see com.caucho.hessian.client.HessianProxyFactory#setOverloadEnabled
	 * @see com.caucho.burlap.client.BurlapProxyFactory#setOverloadEnabled
	 */
	public void setOverloadEnabled(boolean overloadEnabled) {
		this.overloadEnabled = overloadEnabled;
	}

	public boolean isOverloadEnabled() {
		return overloadEnabled;
	}


	public void afterPropertiesSet() throws MalformedURLException {
		prepare();
	}

	/**
	 * Initialize the underlying Hessian/Burlap proxy for this accessor.
	 * <p>This implementation just checks whether "serviceInterface" and
	 * "serviceUrl" have been specified. Concrete initialization is added
	 * in HessianClientInterceptor and BurlapClientInterceptor.
	 * @throws java.net.MalformedURLException if thrown by Hessian/Burlap API
	 * @see HessianClientInterceptor#prepare
	 * @see BurlapClientInterceptor#prepare
	 */
	public void prepare() throws MalformedURLException {
		if (getServiceInterface() == null) {
			throw new IllegalArgumentException("serviceInterface is required");
		}
		if (getServiceUrl() == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
	}

}
