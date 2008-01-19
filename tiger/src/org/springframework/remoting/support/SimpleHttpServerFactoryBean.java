/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.remoting.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.ConcurrentExecutorAdapter;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * {@link org.springframework.beans.factory.FactoryBean} that creates a simple
 * HTTP server, based on the HTTP server that is included in Sun's JRE 1.6.
 * Starts the HTTP server on initialization and stops it on destruction.
 * Exposes the resulting {@link com.sun.net.httpserver.HttpServer} object.
 *
 * <p>Allows for registering {@link com.sun.net.httpserver.HttpHandler HttpHandlers}
 * for specific {@link #setContexts context paths}. Alternatively,
 * register such context-specific handlers programmatically on the
 * {@link com.sun.net.httpserver.HttpServer} itself.
 *
 * @author Juergen Hoeller
 * @since 2.5.1
 * @see #setPort
 * @see #setContexts
 */
public class SimpleHttpServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private int port = 8080;

	private int backlog = -1;

	private Executor executor;

	private Map<String, HttpHandler> contexts;

	private int shutdownDelay = 0;

	private HttpServer server;


	/**
	 * Specify the HTTP server's port. Default is 8080.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Specify the HTTP server's TCP backlog. Default is -1,
	 * indicating the system's default value.
	 */
	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	/**
	 * Set the JDK concurrent executor to use for dispatching incoming requests.
	 * @see com.sun.net.httpserver.HttpServer#setExecutor
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	/**
	 * Set the Spring TaskExecutor to use for dispatching incoming requests.
	 * @see com.sun.net.httpserver.HttpServer#setExecutor
	 */
	public void setTaskExecutor(TaskExecutor executor) {
		this.executor = new ConcurrentExecutorAdapter(executor);
	}

	/**
	 * Register {@link com.sun.net.httpserver.HttpHandler HttpHandlers}
	 * for specific context paths.
	 * @param contexts a Map with context paths as keys and HttpHandler
	 * objects as values
	 * @see org.springframework.remoting.httpinvoker.SimpleHttpInvokerServiceExporter
	 * @see org.springframework.remoting.caucho.SimpleHessianServiceExporter
	 * @see org.springframework.remoting.caucho.SimpleBurlapServiceExporter
	 */
	public void setContexts(Map<String, HttpHandler> contexts) {
		this.contexts = contexts;
	}

	/**
	 * Specify the number of seconds to wait until HTTP exchanges have
	 * completed when shutting down the HTTP server. Default is 0.
	 */
	public void setShutdownDelay(int shutdownDelay) {
		this.shutdownDelay = shutdownDelay;
	}


	public void afterPropertiesSet() throws IOException {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Initializing HttpServer for port " + this.port);
		}
		this.server = HttpServer.create(new InetSocketAddress(this.port), this.backlog);
		if (this.executor != null) {
			this.server.setExecutor(this.executor);
		}
		if (this.contexts != null) {
			for (Map.Entry<String, HttpHandler> entry : this.contexts.entrySet()) {
				this.server.createContext(entry.getKey(), entry.getValue());
			}
		}
		this.server.start();
	}

	public Object getObject() {
		return this.server;
	}

	public Class getObjectType() {
		return (this.server != null ? this.server.getClass() : HttpServer.class);
	}

	public boolean isSingleton() {
		return true;
	}

	public void destroy() {
		this.server.stop(this.shutdownDelay);
	}

}
