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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Web controller that exports the specified service bean as HTTP invoker
 * service endpoint, accessible via an HTTP invoker proxy.
 *
 * <p>Simply deserializes remote invocation objects and serializes remote
 * invocation results. Uses Java serialization just like RMI, but provides
 * the same ease of setup as Caucho's HTTP-based Hessian and Burlap protocols.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see HttpInvokerProxyFactoryBean
 */
public class HttpInvokerServiceExporter extends RemoteInvocationBasedExporter
		implements Controller, InitializingBean {

	protected static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	private Object proxy;

	public void afterPropertiesSet() {
		this.proxy = getProxyForService();
	}

	/**
	 * Read a remote invocation from the request and write a
	 * remote invocation result to the response.
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ClassNotFoundException {
		RemoteInvocation invocation = readRemoteInvocation(request);
		RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
		writeRemoteInvocationResult(response, result);
		return null;
	}

	/**
	 * Read a RemoteInvocation from the given HTTP request.
	 * @param request current HTTP request
	 * @return the RemoteInvocation object
	 * @throws IOException if thrown by operations on the request
	 * @throws ClassNotFoundException if thrown by deserialization
	 */
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request)
			throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
		try {
			Object obj = ois.readObject();
			if (!(obj instanceof RemoteInvocation)) {
				throw new IOException("Deserialized object needs to be a RemoteInvocation: " + obj);
			}
			RemoteInvocation invocation = (RemoteInvocation) obj;
			return invocation;
		}
		finally {
			ois.close();
		}
	}

	/**
	 * Write the given RemoteInvocationResult to the given HTTP response.
	 * @param response current HTTP response
	 * @param result the RemoteInvocationResult object
	 * @throws IOException if thrown by operations on the response
	 */
	protected void writeRemoteInvocationResult(HttpServletResponse response, RemoteInvocationResult result)
			throws IOException {
		response.setContentType(CONTENT_TYPE_SERIALIZED_OBJECT);
		ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
		try {
			oos.writeObject(result);
		}
		finally {
			oos.close();
		}
	}

}
