package org.springframework.jmx;

import junit.framework.TestCase;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @author Rob Harrop
 */
public abstract class AbstractMBeanServerTests extends TestCase {

	protected MBeanServer server;

	public void setUp() throws Exception {
		this.server = MBeanServerFactory.createMBeanServer();
		onSetUp();
	}

	protected void tearDown() throws Exception {
		MBeanServerFactory.releaseMBeanServer(this.server);
	}

	protected abstract void onSetUp() throws Exception;
}
