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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * <code>FactoryBean</code> implementation to create an <code>MBeanServerConnection</code> to a remote
 * <code>MBeanServer</code> exposed via a <code>JMXServerConnector</code>.
 *
 * @author Rob Harrop
 * @see ConnectorServerFactoryBean
 */
public class MBeanServerConnectionFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * The service URL of the remote <code>MBeanServer</code>.
	 */
	private String serviceUrl;

	/**
	 * The parsed service URL of the remote <code>MBeanServer</code>.
	 */
	private JMXServiceURL url;

	/**
	 * Sets the service URL of the remote <code>MBeanServer</code>.
	 * @param serviceUrl
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Parses the <code>String</code> representation of the service URL into a
	 * <code>JMXServiceURL</code> instance.
	 */
	public void afterPropertiesSet() throws Exception {
		if (!StringUtils.hasText(this.serviceUrl)) {
			throw new IllegalArgumentException("serviceUrl is required");
		}

		// Parse the URL now to save time later.
		this.url = new JMXServiceURL(this.serviceUrl);
	}

	/**
	 * Gets an <code>MBeanServerConnection</code> to the remote <code>MBeanServer</code>.
	 * Each call returns a new instance.
	 * @return the <code>MBeanServerConnection</code>.
	 * @throws Exception if there is a problem connecting to the remote <code>MBeanServer</code>.
	 */
	public Object getObject() throws Exception {
		// Create the connector and return the connection.
		JMXConnector connector = JMXConnectorFactory.connect(this.url);
		return connector.getMBeanServerConnection();
	}

	/**
	 * Returns the type of the object managed by this <code>FactoryBean</code>.
	 * @return Always <code>MBeanServerConnection</code>.
	 */
	public Class getObjectType() {
		return MBeanServerConnection.class;
	}

	/**
	 * Always returns <code>false</code>.
	 * @return always <code>false</code>.
	 */ 
	public boolean isSingleton() {
		return false;
	}

}
