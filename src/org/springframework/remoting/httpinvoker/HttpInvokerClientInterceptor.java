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

package org.springframework.remoting.httpinvoker;

import java.io.IOException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * Interceptor for accessing an HTTP invoker service.
 * The service URL must be an HTTP URL exposing an HTTP invoker service.
 *
 * <p>Simply serializes remote invocation objects and deserializes remote
 * invocation results. Uses Java serialization just like RMI, but provides
 * the same ease of setup as Caucho's HTTP-based Hessian and Burlap protocols.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setServiceUrl
 * @see HttpInvokerServiceExporter
 * @see HttpInvokerProxyFactoryBean
 */
public class HttpInvokerClientInterceptor extends RemoteInvocationBasedAccessor
		implements MethodInterceptor, HttpInvokerClientConfiguration {

	private HttpInvokerRequestExecutor httpInvokerRequestExecutor = new SimpleHttpInvokerRequestExecutor();

	public void setHttpInvokerRequestExecutor(HttpInvokerRequestExecutor httpInvokerRequestExecutor) {
		this.httpInvokerRequestExecutor = httpInvokerRequestExecutor;
	}

	public HttpInvokerRequestExecutor getHttpInvokerRequestExecutor() {
		return httpInvokerRequestExecutor;
	}

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
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

	/**
	 * Recreate the invocation result contained in the given RemoteInvocationResult
	 * object. The default implementation calls the default recreate method.
	 * <p>Can be overridden in subclass to provide custom recreation, potentially
	 * processing the returned result object.
	 * @param result the RemoteInvocationResult to recreate
	 * @return a return value if the invocation result is a successful return
	 * @throws Throwable if the invocation result is an exception
	 * @see org.springframework.remoting.support.RemoteInvocationResult#recreate
	 */
	protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
		return result.recreate();
	}

}
