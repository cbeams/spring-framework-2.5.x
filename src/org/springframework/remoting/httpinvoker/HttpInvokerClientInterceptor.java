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

package org.springframework.remoting.httpinvoker;

import java.io.IOException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * Interceptor for accessing an HTTP invoker service.
 * The service URL must be an HTTP URL exposing an HTTP invoker service.
 *
 * <p>Serializes remote invocation objects and deserializes remote invocation
 * result objects. Uses Java serialization just like RMI, but provides the
 * same ease of setup as Caucho's HTTP-based Hessian and Burlap protocols.
 *
 * <P>HTTP invoker is a very extensible and customizable protocol.
 * It supports the RemoteInvocationFactory mechanism, like RMI invoker,
 * allowing to include additional invocation attributes (for example,
 * a security context). Furthermore, it allows to customize request
 * execution via the HttpInvokerRequestExecutor strategy.
 *
 * <p>Can use the JDK's RMIClassLoader to load classes from a given codebase,
 * performing on-demand dynamic code download from a remote location.
 * The codebase can consist of multiple URLs, separated by spaces.
 * Note that RMIClassLoader requires a SecurityManager to be set, like when
 * using dynamic class download with standard RMI! (See the RMI documentation
 * for details.)
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setServiceUrl
 * @see #setCodebaseUrl
 * @see #setRemoteInvocationFactory
 * @see #setHttpInvokerRequestExecutor
 * @see HttpInvokerServiceExporter
 * @see HttpInvokerProxyFactoryBean
 * @see java.rmi.server.RMIClassLoader
 */
public class HttpInvokerClientInterceptor extends RemoteInvocationBasedAccessor
		implements MethodInterceptor, HttpInvokerClientConfiguration {

	private String codebaseUrl;

	private HttpInvokerRequestExecutor httpInvokerRequestExecutor = new SimpleHttpInvokerRequestExecutor();


	/**
	 * Set the codebase URL to download classes from if not found locally.
	 * Can consists of multiple URLs, separated by spaces.
	 * <p>Follows RMI's codebase conventions for dynamic class download.
	 * In contrast to RMI, where the server determines the URL for class download
	 * (via the "java.rmi.server.codebase" system property), it's the client
	 * that determines the codebase URL here. The server will usually be the
	 * same as for the service URL, just pointing to a different path there.
	 * @see #setServiceUrl
	 * @see org.springframework.remoting.rmi.CodebaseAwareObjectInputStream
	 * @see java.rmi.server.RMIClassLoader
	 */
	public void setCodebaseUrl(String codebaseUrl) {
		this.codebaseUrl = codebaseUrl;
	}

	/**
	 * Return the codebase URL to download classes from if not found locally.
	 */
	public String getCodebaseUrl() {
		return codebaseUrl;
	}

	/**
	 * Set the HttpInvokerRequestExecutor implementation to use for executing
	 * remote invocations.
	 * <p>Default is SimpleHttpInvokerRequestExecutor. Alternatively, consider
	 * CommonsHttpInvokerRequestExecutor for more sophisticated needs.
	 * @see SimpleHttpInvokerRequestExecutor
	 * @see CommonsHttpInvokerRequestExecutor
	 */
	public void setHttpInvokerRequestExecutor(HttpInvokerRequestExecutor httpInvokerRequestExecutor) {
		this.httpInvokerRequestExecutor = httpInvokerRequestExecutor;
	}

	/**
	 * Return the HttpInvokerRequestExecutor used by this remote accessor.
	 */
	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		return httpInvokerRequestExecutor;
	}


	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			return "HTTP invoker proxy for service URL [" + getServiceUrl() + "]";
		}

		RemoteInvocation invocation = createRemoteInvocation(methodInvocation);
		RemoteInvocationResult result = null;
		try {
			result = executeRequest(invocation);
		}
		catch (IOException ex) {
			throw new RemoteAccessException("Cannot access HTTP invoker remote service at [" + getServiceUrl() + "]", ex);
		}
		catch (ClassNotFoundException ex) {
			throw new RemoteAccessException("Cannot deserialize result from [" + getServiceUrl() + "]", ex);
		}
		return recreateRemoteInvocationResult(result);
	}

	/**
	 * Execute the given remote invocation via the HttpInvokerRequestExecutor.
	 * <p>Can be overridden in subclasses to pass a different configuration object
	 * to the executor. Alternatively, add further configuration properties in a
	 * subclass of this accessor: By default, the accessor passed itself as
	 * configuration object to the executor.
	 * @param invocation the RemoteInvocation to execute
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O operations
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @see #getHttpInvokerRequestExecutor
	 * @see HttpInvokerClientConfiguration
	 */
	protected RemoteInvocationResult executeRequest(RemoteInvocation invocation)
			throws IOException, ClassNotFoundException {
		return getHttpInvokerRequestExecutor().executeRequest(this, invocation);
	}

}
