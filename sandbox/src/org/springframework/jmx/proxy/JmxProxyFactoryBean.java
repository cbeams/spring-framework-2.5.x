/*
 * Created on Aug 6, 2004
 */
package org.springframework.jmx.proxy;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.ObjectNameManager;
import org.springframework.jmx.util.JmxUtils;

/**
 * @author robh
 */
public class JmxProxyFactoryBean implements FactoryBean, InitializingBean {

    private static final String DEFAULT_IMPLEMENTATION = "org.springframework.jmx.proxy.CglibJmxObjectProxyFactory";

    private String objectName;

    private ObjectName name;

    private Object proxy = null;

    private Class implementationClass;

    private MBeanServerConnection mbeanServer;

    private JmxObjectProxyFactory factory;

    private Class[] proxyInterfaces;

    public void setServer(MBeanServerConnection mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setImplementationClass(Class cls) {
        this.implementationClass = cls;
    }

    public void setProxyInterfaces(Class[] proxyInterfaces) {
        this.proxyInterfaces = proxyInterfaces;
    }

    public Object getObject() throws Exception {

        if (proxy == null) {
            proxy = factory.createProxy(mbeanServer, name);
        }

        return proxy;
    }

    public Class getObjectType() {
        return proxy.getClass();
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        name = ObjectNameManager.getInstance(objectName);

        if (implementationClass == null) {
            implementationClass = Class.forName(DEFAULT_IMPLEMENTATION);
        } else {
            if (!(JmxObjectProxyFactory.class
                    .isAssignableFrom(implementationClass))) {
                throw new IllegalArgumentException(
                        "The implementation class must implement JmxObjectProxyFactory.");
            }
        }

        // no server specified - locate
        if (mbeanServer == null) {
            mbeanServer = JmxUtils.locateMBeanServer();
        }

        // no server found - error
        if (mbeanServer == null) {
            throw new IllegalArgumentException(
                    "The server property of "
                            + JmxObjectProxyFactory.class.getName()
                            + " is required when not running in an environment with an existing MBeanServer instance.");
        }

        factory = (JmxObjectProxyFactory) implementationClass.newInstance();

        factory.setProxyInterfaces(proxyInterfaces);
    }
}