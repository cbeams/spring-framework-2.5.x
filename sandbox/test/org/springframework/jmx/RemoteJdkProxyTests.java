/*
 * Created on Sep 26, 2004
 */
package org.springframework.jmx;

import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * @author robh
 *  
 */
public class RemoteJdkProxyTests extends JdkProxyTests {

	private static final String SERVICE_URL = "service:jmx:jmxmp://localhost:9876";

	private JMXConnectorServer connectorServer;

	private JMXConnector connector;

	public RemoteJdkProxyTests(String name) {
		super(name);

	}

	public void setUp() throws Exception {
		super.setUp();

		connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(
				getServiceUrl(), null, server);
		connectorServer.start();
	}

	public void tearDown() throws Exception {
		connector.close();
		connectorServer.stop();
		super.tearDown();
	}

	private JMXServiceURL getServiceUrl() throws MalformedURLException {
		return new JMXServiceURL(SERVICE_URL);
	}

	protected MBeanServerConnection getServerConnection() throws Exception {
		connector = JMXConnectorFactory.connect(getServiceUrl());
		return connector.getMBeanServerConnection();
	}
}