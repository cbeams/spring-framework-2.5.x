/*
 * Copyright 2002-2004 the original author or authors.
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

import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.jmx.AbstractJmxTests;

/**
 * @author Rob Harrop
 */
public class ConnectorServerFactoryBeanTests extends AbstractJmxTests {

	private static final String OBJECT_NAME = "spring:type=connector,name=test";

	public void testStartupWithLocatedServer() throws Exception {
		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.start();

		checkServerConnection(bean, server);
	}

	public void testStartupWithSuppliedServer() throws Exception {
		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.setServer(server);
		bean.start();

		checkServerConnection(bean, server);
	}

	public void testRegisterWithMBeanServer() throws Exception {
		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.setObjectName(OBJECT_NAME);
		bean.start();

		// try to get the connector bean
		ObjectInstance instance = server.getObjectInstance(ObjectName.getInstance(OBJECT_NAME));

		assertNotNull("ObjectInstance should not be null", instance);
	}

	public void testNoRegisterWithMBeanServer() throws Exception {
		ConnectorServerFactoryBean bean = new ConnectorServerFactoryBean();
		bean.start();

		//	try to get the connector bean
		try {
			ObjectInstance instance = server.getObjectInstance(ObjectName.getInstance(OBJECT_NAME));
			fail("Instance should not be found");
		}
		catch (InstanceNotFoundException ex) {

		}
	}

	private void checkServerConnection(ConnectorServerFactoryBean bean,
			MBeanServer hostedServer) throws IOException, MalformedURLException {
		// try to connect using client
		JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(
				ConnectorServerFactoryBean.DEFAULT_SERVICE_URL));
		assertNotNull("Client Connector should not be null", connector);

		// get the mbean server connection
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		assertNotNull("MBeanServerConnection should not be null", connection);

		// test for mbean server equality
		assertEquals("Registered MBean Count should be the same",
				hostedServer.getMBeanCount(), connection.getMBeanCount());
	}

}
