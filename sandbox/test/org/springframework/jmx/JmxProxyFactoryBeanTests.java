/*
 * Created on Aug 10, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jmx.exceptions.ProxyCreationException;
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

    protected ObjectName getObjectName() throws Exception {
        return ObjectNameManager.getInstance(OBJECT_NAME);
    }

    public void testWithJdkProxyFactory() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        fb.setImplementationClass(JdkJmxObjectProxyFactory.class);
        fb.setProxyInterfaces(new Class[] { IJmxTestBean.class });
        fb.afterPropertiesSet();

        IJmxTestBean bean = (IJmxTestBean) fb.getObject();
        assertNotNull("Proxy should not be null", bean);
    }

    public void testInvalidJdkProxy() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        fb.setImplementationClass(JdkJmxObjectProxyFactory.class);
        fb.afterPropertiesSet();

        try {
            fb.getObject();
            fail("Should not be able to create JDK proxy with no proxy interfaces");
        } catch (Exception ex) { // good
        }
    }

    public void testWithCglibProxyFactory() throws Exception {
        JmxProxyFactoryBean fb = getProxyFactory();
        fb.afterPropertiesSet();
        JmxTestBean bean = (JmxTestBean) fb.getObject();
        assertNotNull("Proxy should not be null");
    }

    public void testWithLocatedMBeanServer() throws Exception {
        JmxProxyFactoryBean fb = new JmxProxyFactoryBean();
        fb.setObjectName(OBJECT_NAME);
        fb.afterPropertiesSet();
        Object proxy = fb.getObject();
        assertNotNull(proxy);
    }

    public void testProxyFactoryBeanWithAutodetect() throws Exception {
        try {
            ApplicationContext context = new FileSystemXmlApplicationContext(
                    "./sandbox/test/org/springframework/jmx/proxyFactoryBean.xml");
        } catch (BeanCreationException ex) {
            if (ex.getCause().getClass() == ProxyCreationException.class) {
                fail("JmxProxyFactoryBean should be ignored by JmxMBeanAdapter when running autodetect process");
            } else {
                throw ex;
            }
        }

    }

    private JmxProxyFactoryBean getProxyFactory() {
        JmxProxyFactoryBean fb = new JmxProxyFactoryBean();
        fb.setServer(server);
        fb.setObjectName(OBJECT_NAME);
        return fb;
    }

}
