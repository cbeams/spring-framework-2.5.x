
package org.springframework.jmx.registration;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author robh
 */
public class DefaultRegistrationStrategy implements MBeanServerAwareRegistrationStrategy {

	private MBeanServer server;

	public void setMBeanServer(MBeanServer server) {
		this.server = server;
	}

	public void registerMBean(Object mbean, ObjectName objectName) throws JMException {
		server.registerMBean(mbean, objectName);
	}

	public void unregisterMBean(ObjectName objectName) throws JMException {
		server.unregisterMBean(objectName);
	}
}
