
package org.springframework.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.util.ObjectNameManager;

/**
 * @author Rob Harrop
 */
public class MBeanAutodetectionTests extends TestCase {

	public void testAutodetectMBean() throws Exception {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("org/springframework/jmx/mbeanAutodetect.xml");

		MBeanServer server = (MBeanServer)ctx.getBean("server");
		ObjectInstance instance = server.getObjectInstance(ObjectNameManager.getInstance("spring:mbean=true"));

		assertNotNull(instance);

	}
}
