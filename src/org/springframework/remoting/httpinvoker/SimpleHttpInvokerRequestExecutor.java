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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * HttpInvokerRequestExecutor implementation that uses J2SE facilities
 * to execute POST requests, without support for HTTP authentication
 * or advanced configuration options.
 *
 * <p>Consider CommonsHttpInvokerRequestExecutor for more sophisticated needs.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see CommonsHttpInvokerRequestExecutor
 */
public class SimpleHttpInvokerRequestExecutor extends AbstractHttpInvokerRequestExecutor {

	protected RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException {

		URLConnection con = new URL(config.getServiceUrl()).openConnection();
		if (!(con instanceof HttpURLConnection)) {
			throw new IOException("Service URL [" + config.getServiceUrl() + "] is not an HTTP URL");
		}
		HttpURLConnection httpCon = (HttpURLConnection) con;

		prepareConnection(httpCon, baos.size());
		baos.writeTo(httpCon.getOutputStream());
		return readRemoteInvocationResult(httpCon.getInputStream());
	}

	/**
	 * Prepare the given HTTP connection.
	 * <p>Default implementation specifies POST as method,
	 * "application/x-java-serialized-object" as "Content-Type" header,
	 * and the given content length as "Content-Length" header.
	 * @param con the HTTP connection to prepare
	 * @param contentLength the length of the content to send
	 * @throws IOException if thrown by HttpURLConnection methods
	 */
	protected void prepareConnection(HttpURLConnection con, int contentLength) throws IOException {
		con.setDoOutput(true);
		con.setRequestMethod(HTTP_METHOD_POST);
		con.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, CONTENT_TYPE_SERIALIZED_OBJECT);
		con.setRequestProperty(HTTP_HEADER_CONTENT_LENGTH, Integer.toString(contentLength));
	}

}
