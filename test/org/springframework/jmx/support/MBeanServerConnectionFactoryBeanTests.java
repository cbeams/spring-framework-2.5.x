/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.support;

import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.core.JdkVersion;
import org.springframework.jmx.AbstractJmxTests;

/**
 * @author Rob Harrop
 */
public class MBeanServerConnectionFactoryBeanTests extends AbstractJmxTests {

	private static final String SERVICE_URL = "service:jmx:jmxmp://localhost:9876";

	private JMXServiceURL getServiceUrl() throws MalformedURLException {
		return new JMXServiceURL(SERVICE_URL);
	}

	private JMXConnectorServer getConnectorServer() throws Exception {
		return JMXConnectorServerFactory.newJMXConnectorServer(getServiceUrl(), null, server);
	}

	public void testValidConnection() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			// to avoid NoClassDefFoundError for JSSE
			return;
		}

		JMXConnectorServer connectorServer = getConnectorServer();
		connectorServer.start();

		MBeanServerConnectionFactoryBean bean = new MBeanServerConnectionFactoryBean();
		bean.setServiceUrl(SERVICE_URL);
		bean.afterPropertiesSet();

		MBeanServerConnection connection = (MBeanServerConnection) bean.getObject();
		assertNotNull("Connection should not be null", connection);

		// perform simple mbean count test
		assertEquals("MBean count should be the same", server.getMBeanCount(), connection.getMBeanCount());
	}

	public void testWithNoServiceUrl() throws Exception {
		MBeanServerConnectionFactoryBean bean = new MBeanServerConnectionFactoryBean();
		try {
			bean.afterPropertiesSet();
			fail("IllegalArgumentException should be raised when no service url is provided");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

}
