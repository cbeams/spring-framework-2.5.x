/*
 * Created on Aug 10, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

import org.springframework.jmx.proxy.JmxProxyFactoryBean;

/**
 * @author robh
 */
public class JmxProxyFactoryBeanTests extends AbstractJmxTests {

    private static final String OBJECT_NAME = "bean:name=testBean1";
    
    public JmxProxyFactoryBeanTests(String name) {
        super(name);
    }
    
    protected ObjectName getObjectName() throws Exception{
        return ObjectName.getInstance(OBJECT_NAME);
    }
    
    public void testWithJdkProxyFactory() throws Exception {
        JmxProxyFactoryBean fb = new JmxProxyFactoryBean();
        fb.setUseCglib(false);
        fb.setMBeanServer(server);
        fb.setObjectName(OBJECT_NAME);

    }

}
