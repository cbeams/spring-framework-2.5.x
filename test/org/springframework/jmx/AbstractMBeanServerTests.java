package org.springframework.jmx;

import junit.framework.TestCase;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * @author Rob Harrop
 */
public abstract class AbstractMBeanServerTests extends TestCase {

	protected MBeanServer server;

	public final void setUp() throws Exception {
		this.server = MBeanServerFactory.createMBeanServer();
		onSetUp();
	}

	protected void tearDown() throws Exception {
		MBeanServerFactory.releaseMBeanServer(this.getServer());
	}

	protected void onSetUp() throws Exception{
	}

	public MBeanServer getServer() {
		return server;
	}

	protected void assertIsRegistered(String message, ObjectName objectName) {
		assertTrue(message, getServer().isRegistered(objectName));
	}
}
