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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * HttpInvokerRequestExecutor implementation that uses
 * <a href="http://jakarta.apache.org/commons/httpclient">Jakarta Commons HttpClient</a>
 * to execute POST requests.
 *
 * <p>Allows to use a preconfigured HttpClient instance, potentially
 * with authentication, HTTP connection pooling, etc.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see SimpleHttpInvokerRequestExecutor
 */
public class CommonsHttpInvokerRequestExecutor extends AbstractHttpInvokerRequestExecutor {

	private HttpClient httpClient;


	/**
	 * Create a new CommonsHttpInvokerRequestExecutor with a default
	 * HttpClient that uses a default MultiThreadedHttpConnectionManager.
	 * @see org.apache.commons.httpclient.HttpClient
	 * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
	 */
	public CommonsHttpInvokerRequestExecutor() {
		this.httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
	}

	/**
	 * Create a new CommonsHttpInvokerRequestExecutor with the given
	 * HttpClient instance.
	 * @param httpClient the HttpClient instance to use for this request executor
	 */
	public CommonsHttpInvokerRequestExecutor(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Set the HttpClient instance to use for this request executor.
	 */
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Return the HttpClient instance that this request executor uses.
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}


	protected RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException {

		PostMethod postMethod = createPostMethod(config);
		try {
			postMethod.setRequestBody(new ByteArrayInputStream(baos.toByteArray()));
			executePostMethod(config, this.httpClient, postMethod);
			return readRemoteInvocationResult(postMethod.getResponseBodyAsStream());
		}
		finally {
			// need to explicitly release because it might be pooled
			postMethod.releaseConnection();
		}
	}

	/**
	 * Create a PostMethod for the given configuration.
	 * @param config the HTTP invoker configuration that specifies the
	 * target service
	 * @return the PostMethod instance
	 * @throws IOException if thrown by I/O methods
	 */
	protected PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
		PostMethod postMethod = new PostMethod(config.getServiceUrl());
		postMethod.setRequestHeader(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_SERIALIZED_OBJECT);
		return postMethod;
	}

	/**
	 * Execute the given PostMethod instance.
	 * @param config the HTTP invoker configuration that specifies the
	 * target service
	 * @param httpClient the HttpClient to execute on
	 * @param postMethod the PostMethod to execute
	 * @throws IOException if thrown by I/O methods
	 */
	protected void executePostMethod(
			HttpInvokerClientConfiguration config, HttpClient httpClient, PostMethod postMethod)
			throws IOException {
		this.httpClient.executeMethod(postMethod);
	}

}
