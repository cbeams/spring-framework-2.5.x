/*
 * Created on Jul 29, 2004
 */
package org.springframework.jmx;

import java.util.HashMap;
import java.util.Map;
import java.beans.PropertyEditor;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;

import junit.framework.TestCase;
import org.springframework.jmx.assemblers.ModelMBeanInfoAssembler;
import org.springframework.jmx.assemblers.AutodetectCapableModelMBeanInfoAssembler;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;

/**
 * @author robh
 */
public class JmxMBeanAdapterTests extends TestCase {

    private static final String OBJECT_NAME = "spring:test=jmxMBeanAdaptor";

    public void testWithSuppliedMBeanServer() throws Exception {
        MBeanServer server = null;
        try {
            server = MBeanServerFactory.newMBeanServer();
            JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
            adaptor.setBeans(getBeanMap());
            adaptor.setServer(server);
            adaptor.registerBeans();
            assertTrue("The bean was not registered with the MBeanServer", beanExists(server, ObjectNameManager.getInstance(OBJECT_NAME)));
        } finally {
            if (server != null)
                server.unregisterMBean(new ObjectName(OBJECT_NAME));
        }
    }

    public void testWithLocatedMBeanServer() throws Exception {
        MBeanServer server = MBeanServerFactory.createMBeanServer();
        JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
        adaptor.setBeans(getBeanMap());
        adaptor.registerBeans();
        assertTrue("The bean was not registered with the MBeanServer", beanExists(server, ObjectNameManager.getInstance(OBJECT_NAME)));
        server.unregisterMBean(new ObjectName(OBJECT_NAME));
    }

    public void testUserCreatedMBeanRegWithDynamicMBean() throws Exception {
        Map map = new HashMap();
        map.put("spring:name=dynBean", new DynamicMBeanTest());

        MBeanServer server = MBeanServerFactory.createMBeanServer();

        InvokeDetectAssembler asm = new InvokeDetectAssembler();

        JmxMBeanAdapter adaptor = new JmxMBeanAdapter();
        adaptor.setServer(server);
        adaptor.setBeans(map);
        adaptor.setAssembler(asm);
        adaptor.registerBeans();

        Object name = server.getAttribute(ObjectNameManager.getInstance("spring:name=dynBean"), "name");
        assertEquals("The name attribute is incorrect", "Rob Harrop", name);
        assertFalse("Assembler should not have been invoked", asm.invoked);
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

    private static class InvokeDetectAssembler implements ModelMBeanInfoAssembler {

        private boolean invoked = false;

        public ModelMBeanInfo getMBeanInfo(String beanKey, Class beanClass) {
            invoked = true;
            return null;
        }
    }


}