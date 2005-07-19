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

package org.springframework.jmx.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <code>FactoryBean</code> implementation that creates an <code>MBeanServerConnection</code>
 * to a remote <code>MBeanServer</code> exposed via a <code>JMXServerConnector</code>.
 * Exposes the <code>MBeanServer</code> for bean references.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanServerFactoryBean
 * @see ConnectorServerFactoryBean
 */
public class MBeanServerConnectionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	private JMXServiceURL serviceUrl;

	private Map environment;

	private JMXConnector connector;

	private MBeanServerConnection connection;


	/**
	 * Set the service URL of the remote <code>MBeanServer</code>.
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * Set the environment properties used to construct the <code>JMXConnector</code>
	 * as <code>java.util.Properties</code> (String key/value pairs).
	 */
	public void setEnvironment(Properties environment) {
		this.environment = environment;
	}

	/**
	 * Set the environment properties used to construct the <code>JMXConnector</code>
	 * as a <code>Map</code> of String keys and arbitrary Object values.
	 */
	public void setEnvironmentMap(Map environment) {
		this.environment = environment;
	}


	/**
	 * Creates a <code>JMXConnector</code> for the given settings
	 * and exposes the associated <code>MBeanServerConnection</code>.
	 */
	public void afterPropertiesSet() throws IOException {
		if (this.serviceUrl == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		this.connector = JMXConnectorFactory.connect(this.serviceUrl, this.environment);
		this.connection = this.connector.getMBeanServerConnection();
	}


	public Object getObject() {
		return this.connection;
	}

	public Class getObjectType() {
		return (this.connection != null ? this.connection.getClass() : MBeanServerConnection.class);
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Closes the underlying <code>JMXConnector</code>.
	 */
	public void destroy() throws IOException {
		this.connector.close();
	}

}
