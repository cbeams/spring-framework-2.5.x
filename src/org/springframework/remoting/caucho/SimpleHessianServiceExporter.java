/*
 * Copyright 2002-2007 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.SerializerFactory;
import com.caucho.hessian.server.HessianSkeleton;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.logging.Log;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteExporter;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * HTTP request handler that exports the specified service bean as
 * Hessian service endpoint, accessible via a Hessian proxy.
 * Designed for Sun's JRE 1.6 HTTP server, implementing the
 * {@link com.sun.net.httpserver.HttpHandler} interface.
 *
 * <p>Hessian is a slim, binary RPC protocol.
 * For information on Hessian, see the
 * <a href="http://www.caucho.com/hessian">Hessian website</a>.
 * This exporter requires Hessian 3.0.20 or above.
 *
 * <p>Note: Hessian services exported with this class can be accessed by
 * any Hessian client, as there isn't any special handling involved.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 * @see org.springframework.remoting.caucho.HessianClientInterceptor
 * @see org.springframework.remoting.caucho.HessianProxyFactoryBean
 * @see SimpleBurlapServiceExporter
 * @see org.springframework.remoting.httpinvoker.SimpleHttpInvokerServiceExporter
 */
public class SimpleHessianServiceExporter extends RemoteExporter implements HttpHandler, InitializingBean {

	private SerializerFactory serializerFactory = new SerializerFactory();

	private Log debugLogger;

	private HessianSkeletonInvoker skeletonInvoker;


	/**
	 * Specify the Hessian SerializerFactory to use.
	 * <p>This will typically be passed in as an inner bean definition
	 * of type <code>com.caucho.hessian.io.SerializerFactory</code>,
	 * with custom bean property values applied.
	 */
	public void setSerializerFactory(SerializerFactory serializerFactory) {
		this.serializerFactory = (serializerFactory != null ? serializerFactory : new SerializerFactory());
	}

	/**
	 * Set whether to send the Java collection type for each serialized
	 * collection. Default is "true".
	 */
	public void setSendCollectionType(boolean sendCollectionType) {
		this.serializerFactory.setSendCollectionType(sendCollectionType);
	}

	/**
	 * Set whether Hessian's debug mode should be enabled, logging to
	 * this exporter's Commons Logging log. Default is "false".
	 * @see com.caucho.hessian.client.HessianProxyFactory#setDebug
	 */
	public void setDebug(boolean debug) {
		this.debugLogger = (debug ? logger : null);
	}


	public void afterPropertiesSet() {
		prepare();
	}

	/**
	 * Initialize this service exporter.
	 */
	public void prepare() {
		checkService();
		checkServiceInterface();
		HessianSkeleton skeleton = new HessianSkeleton(getProxyForService(), getServiceInterface());
		this.skeletonInvoker = new Hessian2SkeletonInvoker(skeleton, this.serializerFactory, this.debugLogger);
	}


	/**
	 * Processes the incoming Hessian request and creates a Hessian response.
	 */
	public void handle(HttpExchange exchange) throws IOException {
		Assert.notNull(this.skeletonInvoker, "HessianServiceExporter has not been initialized");

		if (!"POST".equals(exchange.getRequestMethod())) {
			throw new IOException("HessianServiceExporter only supports POST requests");
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
		try {
			this.skeletonInvoker.invoke(exchange.getRequestBody(), output);
		}
		catch (Throwable ex) {
			exchange.sendResponseHeaders(500, -1);
		  throw new IOException("Hessian skeleton invocation failed", ex);
		}
		exchange.sendResponseHeaders(200, output.size());
		FileCopyUtils.copy(output.toByteArray(), exchange.getResponseBody());
	}

}
