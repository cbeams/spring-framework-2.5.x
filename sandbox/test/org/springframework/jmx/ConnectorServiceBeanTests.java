package org.springframework.jmx;

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

import org.springframework.jmx.exceptions.ObjectNamingException;
import org.springframework.jmx.remote.ConnectorServiceBean;

/**
 * @author Rob Harrop
 */
public class ConnectorServiceBeanTests extends AbstractJmxTests {

	private static final String OBJECT_NAME = "spring:type=connector,name=test";

	public ConnectorServiceBeanTests(String name) {
		super(name);
	}

	public void testStartupWithLocatedServer() throws Exception {
		ConnectorServiceBean bean = new ConnectorServiceBean();
		bean.start();

		checkServerConnection(bean, server);
	}

	public void testStartupWithSuppliedServer() throws Exception {
		ConnectorServiceBean bean = new ConnectorServiceBean();
		bean.setMBeanServer(server);
		bean.start();

		checkServerConnection(bean, server);
	}

	public void testRegisterWithMBeanServer() throws Exception {
		ConnectorServiceBean bean = new ConnectorServiceBean();
		bean.setObjectName(OBJECT_NAME);
		bean.setRegisterConnectorAsMBean(true);
		bean.start();

		// try to get the connector bean
		ObjectInstance instance = server.getObjectInstance(ObjectName
				.getInstance(OBJECT_NAME));

		assertNotNull("ObjectInstance should not be null", instance);
	}

	public void testNoRegisterWithMBeanServer() throws Exception {
		ConnectorServiceBean bean = new ConnectorServiceBean();
		bean.setRegisterConnectorAsMBean(false);
		bean.setObjectName(OBJECT_NAME);
		bean.start();

		//	 try to get the connector bean
		try {
			ObjectInstance instance = server.getObjectInstance(ObjectName
					.getInstance(OBJECT_NAME));
			fail("Instance should not be found");
		} catch (InstanceNotFoundException ex) {

		}
	}

	public void testRegisterWithNoObjectName() throws Exception {
		ConnectorServiceBean bean = new ConnectorServiceBean();
		bean.setRegisterConnectorAsMBean(true);
		bean.setObjectName(null);
		try {
			bean.start();
			fail("ObjectNamingException should have been thrown");
		} catch (ObjectNamingException ex) {

		}
	}

	private void checkServerConnection(ConnectorServiceBean bean,
			MBeanServer hostedServer) throws IOException, MalformedURLException {
		// try to connect using client
		JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(
				bean.getServiceUrl()));
		assertNotNull("Client Connector should not be null", connector);

		// get the mbean server connection
		MBeanServerConnection connection = connector.getMBeanServerConnection();
		assertNotNull("MBeanServerConnection should not be null", connection);

		// test for mbean server equality
		assertEquals("Registered MBean Count should be the same", hostedServer
				.getMBeanCount(), connection.getMBeanCount());
	}

}