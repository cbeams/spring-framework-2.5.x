/*
 * Created on Sep 26, 2004
 */
package org.springframework.jmx;

import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.jmx.remote.MBeanServerConnectionFactoryBean;

/**
 * @author robh
 *
 */
public class MBeanServerConnectionFactoryBeanTests extends AbstractJmxTests {

	private static final String SERVICE_URL = "service:jmx:jmxmp://localhost:9876";
	
	public MBeanServerConnectionFactoryBeanTests(String name) {
		super(name);
	}
	
	
	private JMXServiceURL getServiceUrl() throws MalformedURLException{
		return new JMXServiceURL(SERVICE_URL);
	}
	
	private JMXConnectorServer getConnectorServer() throws Exception{
		return JMXConnectorServerFactory.newJMXConnectorServer(getServiceUrl(), null, server);
	}
	
	public void testValidConnection() throws Exception {
		JMXConnectorServer connectorServer = getConnectorServer();
		connectorServer.start();
		
		MBeanServerConnectionFactoryBean bean = new MBeanServerConnectionFactoryBean();
		bean.setServiceUrl(SERVICE_URL);
		bean.afterPropertiesSet();
		
		MBeanServerConnection connection = (MBeanServerConnection)bean.getObject();
		assertNotNull("Connection should not be null", connection);
		
		// perform simple mbean count test
		assertEquals("MBean count should be the same", server.getMBeanCount(), connection.getMBeanCount());
	}
	
	public void testWithNoServiceUrl() throws Exception {
		MBeanServerConnectionFactoryBean bean = new MBeanServerConnectionFactoryBean();
		
		try {
			bean.afterPropertiesSet();
			fail("IllegalArgumentException should be raised when no service url is provided");
		} catch(IllegalArgumentException ex) {
			
		}
	}
}
