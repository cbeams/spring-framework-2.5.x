/*
 * Created on Jul 5, 2004
 */

package org.springframework.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rob Harrop
 */
public abstract class AbstractJmxTests extends TestCase {

	private ClassPathXmlApplicationContext ctx;

	protected MBeanServer server;

	public void setUp() throws Exception {
		server = MBeanServerFactory.createMBeanServer();
		ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/applicationContext.xml";
	}

	protected ApplicationContext getContext() {
		return this.ctx;
	}

	public void tearDown() throws Exception {
		ctx.close();
		MBeanServerFactory.releaseMBeanServer(server);
		assertTrue("MBeanServers not fully cleaned up", MBeanServerFactory.findMBeanServer(null).isEmpty());
	}

}
