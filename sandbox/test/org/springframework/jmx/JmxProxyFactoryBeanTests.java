/*
 * Created on Aug 10, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

import org.springframework.jmx.proxy.JdkJmxObjectProxyFactory;
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
        return ObjectNameManager.getInstance(OBJECT_NAME);
    }
    
    public void testWithJdkProxyFactory() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        fb.setImplementationClass(JdkJmxObjectProxyFactory.class);
        fb.setProxyInterfaces(new Class[]{IJmxTestBean.class});
        
        IJmxTestBean bean = (IJmxTestBean)fb.getObject();

    }
    
    public void testInvalidJdkProxy() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        fb.setImplementationClass(JdkJmxObjectProxyFactory.class);
        
        try {
            fb.afterPropertiesSet();
            fail("Should not be able to JDK proxy settings with no proxy interfaces");
        } catch(Exception ex) {
            // good
        }
    }
    
    public void testWithCglibProxyFactory() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        JmxTestBean bean = (JmxTestBean)fb.getObject();
    }

    private JmxProxyFactoryBean getProxyFactory() {
        JmxProxyFactoryBean fb = new JmxProxyFactoryBean();
        fb.setMBeanServer(server);
        fb.setObjectName(OBJECT_NAME);
        return fb;
    }

}
