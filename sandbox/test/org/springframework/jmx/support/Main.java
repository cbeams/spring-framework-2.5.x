/*
 * Created on 11-Aug-2004
 */
package org.springframework.jmx.support;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @author Rob Harrop
 */
public class Main {

	public static void main(String[] args) throws Exception {
		MBeanServer server = MBeanServerFactory.createMBeanServer();
		ConnectorServerFactoryBean connector = new ConnectorServerFactoryBean();
		connector.setServer(server);
		connector.start();
		/*HtmlAdaptorServer adapter = new HtmlAdaptorServer(9000);

		server.registerMBean(adapter, ObjectNameManager.getInstance("adapter:type=http"));
		adapter.start();

		ApplicationContext ctx = new FileSystemXmlApplicationContext(
						"./sandbox/test/org/springframework/jmx/applicationContext.xml");
		System.out.println("Running");
		System.in.read();
		adapter.stop();*/


	}
}