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
public class AbstractJmxTests extends TestCase {

	private ApplicationContext ctx;

	protected MBeanServer server;

	public void setUp() throws Exception {
		server = MBeanServerFactory.createMBeanServer();
		ctx = new ClassPathXmlApplicationContext(getApplicationContextPath());
	}

	protected String getApplicationContextPath() {
		return "org/springframework/jmx/applicationContext.xml";
	}

	public void tearDown() throws Exception {
		MBeanServerFactory.releaseMBeanServer(server);
		server = null;
	}

	protected ApplicationContext getContext() {
		return this.ctx;
	}
}