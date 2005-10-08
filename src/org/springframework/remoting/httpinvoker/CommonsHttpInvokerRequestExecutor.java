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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * HttpInvokerRequestExecutor implementation that uses
 * <a href="http://jakarta.apache.org/commons/httpclient">Jakarta Commons HttpClient</a>
 * to execute POST requests. Compatible with Commons HttpClient 2.0 and 3.0.
 *
 * <p>Allows to use a preconfigured HttpClient instance, potentially
 * with authentication, HTTP connection pooling, etc. Also designed
 * for easy subclassing, customizing specific template methods.
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


	/**
	 * Execute the given request through Commons HttpClient.
	 * <p>This method implements the basic processing workflow:
	 * The actual work happens in this class's template methods.
	 * @see #createPostMethod
	 * @see #setRequestBody
	 * @see #executePostMethod
	 * @see #validateResponse
	 * @see #getResponseBody
	 */
	protected RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException {

		PostMethod postMethod = createPostMethod(config);
		try {
			setRequestBody(config, postMethod, baos);
			executePostMethod(config, getHttpClient(), postMethod);
			validateResponse(config, postMethod);
			InputStream responseBody = getResponseBody(config, postMethod);
			return readRemoteInvocationResult(responseBody, config.getCodebaseUrl());
		}
		finally {
			// Need to explicitly release because it might be pooled.
			postMethod.releaseConnection();
		}
	}

	/**
	 * Create a PostMethod for the given configuration.
	 * <p>The default implementation creates a standard PostMethod with
	 * "application/x-java-serialized-object" as "Content-Type" header.
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
	 * Set the given serialized remote invocation as request body.
	 * <p>The default implementation simply sets the serialized invocation
	 * as the PostMethod's request body. This can be overridden, for example,
	 * to write a specific encoding and potentially set appropriate HTTP
	 * request headers.
	 * @param config the HTTP invoker configuration that specifies the target service
	 * @param postMethod the PostMethod to set the request body on
	 * @param baos the ByteArrayOutputStream that contains the serialized
	 * RemoteInvocation object
	 * @throws IOException if thrown by I/O methods
	 * @see org.apache.commons.httpclient.methods.PostMethod#setRequestBody(java.io.InputStream)
	 * @see org.apache.commons.httpclient.methods.PostMethod#setRequestEntity
	 * @see org.apache.commons.httpclient.methods.InputStreamRequestEntity
	 */
	protected void setRequestBody(
			HttpInvokerClientConfiguration config, PostMethod postMethod, ByteArrayOutputStream baos)
			throws IOException {

		// Need to call setRequestBody for compatibility with Commons HttpClient 2.0
		postMethod.setRequestBody(new ByteArrayInputStream(baos.toByteArray()));
	}

	/**
	 * Execute the given PostMethod instance.
	 * @param config the HTTP invoker configuration that specifies the target service
	 * @param httpClient the HttpClient to execute on
	 * @param postMethod the PostMethod to execute
	 * @throws IOException if thrown by I/O methods
	 * @see org.apache.commons.httpclient.HttpClient#executeMethod(org.apache.commons.httpclient.HttpMethod)
	 */
	protected void executePostMethod(
			HttpInvokerClientConfiguration config, HttpClient httpClient, PostMethod postMethod)
			throws IOException {

		httpClient.executeMethod(postMethod);
	}

	/**
	 * Validate the given response as contained in the PostMethod object,
	 * throwing an exception if it does not correspond to a successful HTTP response.
	 * <p>Default implementation rejects any HTTP status code beyond 2xx, to avoid
	 * parsing the response body and trying to deserialize from a corrupted stream.
	 * @param config the HTTP invoker configuration that specifies the target service
	 * @param postMethod the executed PostMethod to validate
	 * @throws IOException if validation failed
	 * @see org.apache.commons.httpclient.methods.PostMethod#getStatusCode()
	 * @see org.apache.commons.httpclient.HttpException
	 */
	protected void validateResponse(HttpInvokerClientConfiguration config, PostMethod postMethod)
			throws IOException {

		if (postMethod.getStatusCode() >= 300) {
			throw new HttpException(
					"Did not receive successful HTTP response: status code = " + postMethod.getStatusCode() +
					", status message = [" + postMethod.getStatusText() + "]");
		}
	}

	/**
	 * Extract the response body from the given executed remote invocation
	 * request.
	 * <p>The default implementation simply fetches the PostMethod's response
	 * body stream. This can be overridden, for example, to check for GZIP
	 * response encoding and wrap the returned InputStream in a GZIPInputStream.
	 * @param config the HTTP invoker configuration that specifies the target service
	 * @param postMethod the PostMethod to read the response body from
	 * @return an InputStream for the response body
	 * @throws IOException if thrown by I/O methods
	 * @see org.apache.commons.httpclient.methods.PostMethod#getResponseBodyAsStream()
	 * @see org.apache.commons.httpclient.methods.PostMethod#getResponseHeader(String)
	 */
	protected InputStream getResponseBody(HttpInvokerClientConfiguration config, PostMethod postMethod)
			throws IOException {

		return postMethod.getResponseBodyAsStream();
	}

}
