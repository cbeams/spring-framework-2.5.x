
package org.springframework.jmx.registration;

import javax.management.JMException;
import javax.management.ObjectName;

/**
 * @author robh
 */
public interface RegistrationStrategy {

	void registerMBean(Object mbean, ObjectName objectName) throws JMException;

	void unregisterMBean(ObjectName objectName) throws JMException;
}
