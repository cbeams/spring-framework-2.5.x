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

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <code>FactoryBean</code> implementation that creates an <code>MBeanServerConnection</code>
 * to a remote <code>MBeanServer</code> exposed via a <code>JMXServerConnector</code>.
 * Exposes the <code>MBeanServer</code> for bean references.
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanServerFactoryBean
 * @see ConnectorServerFactoryBean
 */
public class MBeanServerConnectionFactoryBean implements FactoryBean, InitializingBean {

	/**
	 * The service URL of the remote <code>MBeanServer</code>.
	 */
	private JMXServiceURL serviceUrl;

	/**
	 * The <code>MBeanServerConnection</code> to expose.
	 */
	private MBeanServerConnection server;


	/**
	 * Set the service URL of the remote <code>MBeanServer</code>.
	 */
	public void setServiceUrl(String url) throws MalformedURLException {
		this.serviceUrl = new JMXServiceURL(url);
	}

	/**
	 * Parses the <code>String</code> representation of the service URL into a
	 * <code>JMXServiceURL</code> instance.
	 */
	public void afterPropertiesSet() throws IOException {
		if (this.serviceUrl == null) {
			throw new IllegalArgumentException("serviceUrl is required");
		}
		JMXConnector connector = JMXConnectorFactory.connect(this.serviceUrl);
		this.server = connector.getMBeanServerConnection();
	}


	public Object getObject() {
		return this.server;
	}

	public Class getObjectType() {
		return (this.server != null ? this.server.getClass() : MBeanServerConnection.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
