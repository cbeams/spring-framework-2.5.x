
package org.springframework.jmx.registration;

import javax.management.MBeanServer;

/**
 * @author robh
 */
public interface MBeanServerAwareRegistrationStrategy extends RegistrationStrategy {

	void setMBeanServer(MBeanServer server);
}
