/*
 * Created on Aug 6, 2004
 */
package org.springframework.jmx.proxy;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author robh
 */
public class JmxProxyFactoryBean implements FactoryBean, InitializingBean {

    private String objectName;

    private ObjectName name;

    private Object proxy;

    private boolean useCglib = true;

    private MBeanServer mbeanServer;

    private JmxObjectProxyFactory factory;

    public void setMBeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setUseCglib(boolean useCglib) {
        this.useCglib = useCglib;
    }

    public Object getObject() throws Exception {
        return proxy;
    }

    public Class getObjectType() {
        return proxy.getClass();
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        name = ObjectName.getInstance(objectName);

        if (useCglib) {
            factory = new CglibJmxObjectProxyFactory();
        } else {
            factory = new JdkJmxObjectProxyFactory();
        }

        proxy = factory.createProxy(mbeanServer, name);
    }

}