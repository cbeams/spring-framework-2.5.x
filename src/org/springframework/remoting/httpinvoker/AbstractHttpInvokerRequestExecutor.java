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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * Abstract base implementation of the HttpInvokerRequestExecutor interface.
 *
 * <p>Preimplements serialization of RemoteInvocation objects and
 * deserialization of RemoteInvocationResults objects.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #doExecuteRequest
 */
public abstract class AbstractHttpInvokerRequestExecutor implements HttpInvokerRequestExecutor {

	private static final int SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE = 500;

	protected static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";

	protected static final String HTTP_METHOD_POST = "POST";


	protected final Log logger = LogFactory.getLog(getClass());


	public final RemoteInvocationResult executeRequest(
			HttpInvokerClientConfiguration config, RemoteInvocation invocation)
			throws IOException, ClassNotFoundException {

		ByteArrayOutputStream baos = getByteArrayOutputStream(invocation);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending HTTP invoker request for service at [" + config.getServiceUrl() +
					"], with size " + baos.size());
		}
		return doExecuteRequest(config, baos);
	}

	/**
	 * Serialize the given RemoteInvocation into a ByteArrayOutputStream.
	 * @param invocation the RemoteInvocation object
	 * @return a ByteArrayOutputStream with the serialized RemoteInvocation
	 * @throws IOException if thrown by I/O methods
	 */
	protected ByteArrayOutputStream getByteArrayOutputStream(RemoteInvocation invocation) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE);
		writeRemoteInvocation(invocation, baos);
		return baos;
	}

	/**
	 * Serialize the given RemoteInvocation to the given OutputStream.
	 * @param invocation the RemoteInvocation object
	 * @param os the OutputStream to write to
	 * @throws IOException if thrown by I/O methods
	 */
	protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(os);
		try {
			oos.writeObject(invocation);
			oos.flush();
		}
		finally {
			oos.close();
		}
	}

	/**
	 * Execute a request to send the given serialized remote invocation.
	 * <p>Implementations will usually call readRemoteInvocationResult
	 * to deserialize a returned RemoteInvocationResult object.
	 * @param config the HTTP invoker configuration that specifies the
	 * target service
	 * @param baos the ByteArrayOutputStream that contains the serialized
	 * RemoteInvocation object
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O operations
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @see #readRemoteInvocationResult
	 */
	protected abstract RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException;

	/**
	 * Deserialize a RemoteInvocationResult from the given InputStream.
	 * @param is the InputStream to read from
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O methods
	 */
	protected RemoteInvocationResult readRemoteInvocationResult(InputStream is)
			throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(is);
		try {
			Object obj = ois.readObject();
			if (!(obj instanceof RemoteInvocationResult)) {
				throw new IOException("Deserialized object needs to be a RemoteInvocationResult: " + obj);
			}
			return (RemoteInvocationResult) obj;
		}
		finally {
			ois.close();
		}
	}

}
