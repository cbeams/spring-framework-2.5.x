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
 * Implementation <code>FactoryBean</code> that creates a JSR-160 <code>JMXConnectorServer</code>,
 * optionally registers it with the <code>MBeanServer</code> and then starts it.
 * <p/>
 * The <code>JMXConnectorServer</code> can be started in a separate thread by setting the
 * <code>threaded</code> property to <code>true</code>. You can configure this thread to be a
 * daemon thread by setting the <code>daemon</code> property to <code>true</code>.
 * <p/>
 * The <code>JMXConnectorServer</code> is correctly shutdown when this class is destoryed by
 * the <code>ApplicationContext</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Marcus Brito
 * @see	FactoryBean
 * @see JMXConnectorServer
 * @see MBeanServer
 */
public class ConnectorServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	/**
	 * The default service URL.
	 */
	public static final String DEFAULT_SERVICE_URL = "service:jmx:jmxmp://localhost:9876";

	/**
	 * Stores a reference to the <code>MBeanServer</code> that the connector is
	 * exposing.
	 */
	private MBeanServer server;

	/**
	 * Stores the actual service URL used for the connector.
	 */
	private String serviceUrl = DEFAULT_SERVICE_URL;

	/**
	 * Stores the JSR-160 environment parameters to pass to the <code>JMXConnectorServerFactory</code>.
	 */
	private Map environment;

	/**
	 * The <code>String</code> representation of the <code>ObjectName</code> for the
	 * <code>JMXConnectorServer</code>.
	 */
	private String objectName;

	/**
	 * Indicates whether or not the <code>JMXConnectorServer</code> should be started in a
	 * separate thread.
	 */
	private boolean threaded = false;

	/**
	 * Indicates whether or not the <code>JMXConnectorServer</code> should be started in a
	 * daemon thread. Only applicable if <code>threaded</code> is set to <code>true</code>.
	 */
	private boolean daemon = false;

	/**
	 * Stores the <code>JMXConnectoreServer</code> instance.
	 */
	private JMXConnectorServer connectorServer;

	/**
	 * Sets the <code>MBeanServer</code> that the <code>JMXConnectorServer</code> should
	 * expose.
	 *
	 * @param mbeanServer an <code>MBeanServer</code>.
	 */
	public void setServer(MBeanServer mbeanServer) {
		this.server = mbeanServer;
	}

	/**
	 * Sets the service URL for the <code>JMXConnectorServer</code>.
	 *
	 * @param serviceUrl the service URL.
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Sets the environment properties used to construct the <code>JMXConnectorServer</code>.
	 *
	 * @param environment the environment properties.
	 */
	public void setEnvironment(Map environment) {
		this.environment = environment;
	}

	/**
	 * Sets the <code>ObjectName</code> used to register the <code>JMXConnectorServer</code> itself
	 * with the <code>MBeanServer</code>.
	 *
	 * @param objectName the <code>String</code> representation of the <code>ObjectName</code>.
	 */
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	/**
	 * Sets the <code>threaded</code> flag, indicating whether the <code>JMXConnectorServer</code> should
	 * be started in a separate thread.
	 *
	 * @param threaded <code>true</code> to start the <code>JMXConnectorServer</code> in a separate thread,
	 * otherwise <code>false</code>.
	 * @see #start()
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	/**
	 * Sets the <code>daemon</code> flag, indicating whether any threads started for the
	 * <code>JMXConnectorServer</code> should be started daemon threads.
	 *
	 * @param daemon <code>true</code> to start daemon threads,otherwise <code>false</code>.
	 * @see #start()
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Starts the <code>JMXConnectorServer</code> automatically when running
	 * in an <code>ApplicationContext</code>.
	 *
	 * @throws IOException if there is a problem starting or running the <code>JMXConnectorServer</code>.
	 * @throws JMException if a problem occured when registering the <code>JMXConnectorServer</code> with
	 * the <code>MBeanServer</code>.
	 * @see #start()
	 */
	public void afterPropertiesSet() throws IOException, JMException {
		start();
	}

	/**
	 * Starts the <code>JMXConnectorServer</code>. If the <code>threaded</code> flag is set to <code>true</code>, the
	 * <code>JMXConnectorServer</code> will be started in a separate thread. If the <code>daemon</code> flag is set
	 * to <code>true</code> this thread will be started as a daemon thread.
	 *
	 * @throws IOException if there is a problem starting or running the <code>JMXConnectorServer</code>.
	 * @throws JMException if a problem occured when registering the <code>JMXConnectorServer</code> with
	 * the <code>MBeanServer</code>.
	 */
	public void start() throws IOException, JMException {
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// Create the service URL.
		JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

		// Create the connector server now.
		this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, this.environment, this.server);

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


	/**
	 * Gets the <code>JMXConnectorServer</code> instance.
	 *
	 * @return an instance of <code>JMXConnectorServer</code>.
	 * @throws Exception
	 */
	public Object getObject() throws Exception {
		return this.connectorServer;
	}

	/**
	 * Returns the type of object managed by this class.
	 *
	 * @return if possible, the exact implementation type of the
	 *         <code>JMXConnectorServer</code>, otherwise returns
	 *         <code>JMXConnectorServer.</code>
	 */
	public Class getObjectType() {
		return (this.connectorServer != null ? this.connectorServer.getClass() : JMXConnectorServer.class);
	}

	/**
	 * Indicates that the <code>JMXConnectorServer</code> instance managed by this object
	 * is a singleton.
	 *
	 * @return always <code>true</code>.
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Stops the <code>JMXConnectorServer</code> managed by an instance of this class.
	 *
	 * @throws IOException if there is an error stopping the <code>JMXConnectorServer</code>.
	 */
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
