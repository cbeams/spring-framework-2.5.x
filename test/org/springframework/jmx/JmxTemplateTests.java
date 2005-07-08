package org.springframework.jmx;

import javax.management.*;


import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jmx.support.ObjectNameManager;

/**
 * @author robh
 */
public class JmxTemplateTests extends TestCase {
	private MBeanServer mockServer;

	private MockControl mockControl;

	private JmxTemplate template;

	private ObjectName dummyObjectName;

	public void setUp() throws javax.management.MalformedObjectNameException {
		this.mockControl = MockControl.createControl(MBeanServer.class);
		this.mockServer = (MBeanServer) this.mockControl.getMock();
		this.template = new JmxTemplate(mockServer);
		this.dummyObjectName = ObjectNameManager.getInstance("foo:name=bar");
	}

	public void testWithSuppliedMBeanServer() {
		this.template.execute(new JmxCallback() {
			public Object doWithMBeanServer(MBeanServer innerServer) throws JMException {
				assertSame("Supplied MBeanServer not used", JmxTemplateTests.this.mockServer, innerServer);
				return null;
			}
		});
	}

	public void testWithLocatedMBeanServer() {
		final MBeanServer server = MBeanServerFactory.createMBeanServer();
		try {
			JmxTemplate template = new JmxTemplate();
			template.execute(new JmxCallback() {
				public Object doWithMBeanServer(MBeanServer innerServer) throws JMException {
					assertSame(server, innerServer);
					return null;
				}
			});
		}
		finally {
			MBeanServerFactory.releaseMBeanServer(server);
		}
	}

	public void testRegisterMBean() throws Exception {
		Object mbean = new Object();
		ObjectInstance expectedReturnValue = new ObjectInstance(dummyObjectName, Object.class.getName());
		this.mockControl.expectAndReturn(this.mockServer.registerMBean(mbean, dummyObjectName), expectedReturnValue);
		this.mockControl.replay();

		ObjectInstance actualReturnValue = this.template.registerMBean(mbean, dummyObjectName);

		this.mockControl.verify();

		assertEquals(expectedReturnValue, actualReturnValue);
	}

	public void testUnregisterMBean() throws Exception {
		this.mockServer.unregisterMBean(this.dummyObjectName);
		this.mockControl.setVoidCallable();
		this.mockControl.replay();

		this.template.unregisterMBean(this.dummyObjectName);

		this.mockControl.verify();
	}
	
	public void testExecuteFails() throws Exception {
		JMException expectedException = new javax.management.NotCompliantMBeanException("");

		this.mockControl.expectAndThrow(this.mockServer.registerMBean(null, null), expectedException);
		this.mockControl.replay();

		try {
			this.template.registerMBean(null, null);
			fail("Mock configuration error - should throw exception");
		} catch(JmxException ex) {
			assertTrue(ex instanceof NotCompliantMBeanException);
			assertEquals(expectedException, ex.getCause());
		}
	}
}
