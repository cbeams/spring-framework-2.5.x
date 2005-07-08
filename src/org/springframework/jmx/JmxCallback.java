package org.springframework.jmx;

import javax.management.MBeanServer;
import javax.management.JMException;

/**
 * @author robh
 */
public interface JmxCallback {
	Object doWithMBeanServer(MBeanServer server) throws JMException;
}
