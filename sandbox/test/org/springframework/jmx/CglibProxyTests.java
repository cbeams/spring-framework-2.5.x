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

    protected JmxTestBean getProxy() throws Exception {
        JmxObjectProxyFactory pf = new CglibJmxObjectProxyFactory();
        ObjectName objectName = ObjectName.getInstance("bean:name=testBean1");
        return (JmxTestBean) pf.createProxy(server, objectName);
    }
}