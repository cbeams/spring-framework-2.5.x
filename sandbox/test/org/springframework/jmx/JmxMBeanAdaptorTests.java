/*
 * Created on Jul 29, 2004
 */
package org.springframework.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * @author robh
 */
public class JmxMBeanAdaptorTests extends TestCase {

    private static final String OBJECT_NAME = "spring:test=jmxMBeanAdaptor";
    
    public void testWithSuppliedMBeanServer() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
        adaptor.setBeans(getBeanMap());
        adaptor.setServer(server);
        adaptor.registerBeans();
        assertTrue("The bean was not registered with the MBeanServer", beanExists(server, ObjectName.getInstance(OBJECT_NAME)));
    }
    
    public void testWithLocatedMBeanServer() throws Exception {
        MBeanServer server = MBeanServerFactory.createMBeanServer();
        JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
        adaptor.setBeans(getBeanMap());
        adaptor.afterPropertiesSet();
        adaptor.registerBeans();
        assertTrue("The bean was not registered with the MBeanServer", beanExists(server, ObjectName.getInstance(OBJECT_NAME)));
    }

    private Map getBeanMap() {
        Map map = new HashMap();
        map.put(OBJECT_NAME, new JmxTestBean());

        return map;
    }

    private boolean beanExists(MBeanServer server, ObjectName objectName)
            throws Exception {
        ObjectInstance inst = server.getObjectInstance(objectName);
        return (inst != null);
    }
}