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

package org.springframework.jmx.support;

import java.io.IOException;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedRuntimeException;
import org.springframework.jmx.util.JmxUtils;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Marcus Brito
 */
public class ConnectorServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	public static final String DEFAULT_SERVICE_URL = "service:jmx:jmxmp://localhost:9876";


	private MBeanServer server;

	private String serviceUrl = DEFAULT_SERVICE_URL;

	private Map environment;

	private String objectName;

	private boolean threaded = false;

	private boolean daemon = false;

	private JMXConnectorServer connectorServer;


	public void setServer(MBeanServer mbeanServer) {
		this.server = mbeanServer;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public void setEnvironment(Map environment) {
		this.environment = environment;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}


	public void afterPropertiesSet() throws IOException, JMException {
		start();
	}

	public void start() throws IOException, JMException {
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// Create the service URL.
		JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

		// Create the connector server now.
		this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				url, this.environment, this.server);

		// Do we want to register the connector with the MBean server?
		if (this.objectName != null) {
			ObjectName name = ObjectNameManager.getInstance(this.objectName);
			this.server.registerMBean(this.connectorServer, name);
		}

		if (this.threaded) {
			// Start the connector via a thread.
			Thread connectorThread = new Thread() {
				public void run() {
					try {
						connectorServer.start();
					}
					catch (IOException ex) {
						throw new DelayedConnectorStartException(ex);
					}
				}
			};

			connectorThread.setName("JMX Connector Thread [" + this.serviceUrl + "]");
			connectorThread.setDaemon(this.daemon);
			connectorThread.start();
		}
		else {
			this.connectorServer.start();
		}
	}


	public Object getObject() throws Exception {
		return this.connectorServer;
	}

	public Class getObjectType() {
		return (this.connectorServer != null ? this.connectorServer.getClass() : JMXConnectorServer.class);
	}

	public boolean isSingleton() {
		return true;
	}


	public void destroy() throws IOException {
		this.connectorServer.stop();
	}


	/**
	 * Exception to be thrown if the JMX connector server cannot be started
	 * (in a concurrent thread).
	 */
	public static class DelayedConnectorStartException extends NestedRuntimeException {

		private DelayedConnectorStartException(IOException ex) {
			super("Could not start JMX connector server after delay", ex);
		}
	}

}
