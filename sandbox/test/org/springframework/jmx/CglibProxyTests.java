/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

import org.springframework.jmx.proxy.CglibJmxObjectProxyFactory;
import org.springframework.jmx.proxy.JmxObjectProxyFactory;

/**
 * @author robh
 */
public class CglibProxyTests extends AbstractProxyTests {

    public CglibProxyTests(String name) {
        super(name);
    }

    protected ObjectName getObjectNameForProxy() throws Exception {
        return ObjectName.getInstance("bean:name=testBean3");
    }

    protected JmxObjectProxyFactory getProxyFactory() throws Exception {
        return new CglibJmxObjectProxyFactory();
    }


    protected JmxTestBean getClassProxy() throws Exception {
        return getClassProxy(getProxyFactory());
    }

    protected JmxTestBean getClassProxy(JmxObjectProxyFactory factory)
            throws Exception {
        ObjectName objectName = getObjectNameForProxy();
        return (JmxTestBean) factory.createProxy(server, objectName);
    }
}