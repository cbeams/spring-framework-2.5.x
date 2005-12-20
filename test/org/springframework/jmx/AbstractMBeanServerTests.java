package org.springframework.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * @author Rob Harrop
 */
public abstract class AbstractMBeanServerTests extends TestCase {

	protected MBeanServer server;

	public final void setUp() throws Exception {
		this.server = MBeanServerFactory.createMBeanServer();
		try {
			onSetUp();
		}
		catch (Exception e) {
			releaseServer();
			throw e;
		}
	}

	protected void tearDown() throws Exception {
		releaseServer();
		onTearDown();
	}

	private void releaseServer() {
		MBeanServerFactory.releaseMBeanServer(this.getServer());
	}

	protected void onTearDown() throws Exception {
	}

	protected void onSetUp() throws Exception {
	}

	public MBeanServer getServer() {
		return server;
	}

	protected void assertIsRegistered(String message, ObjectName objectName) {
		assertTrue(message, getServer().isRegistered(objectName));
	}
}
