/*
 * Created on Aug 10, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.jmx.proxy.JdkJmxObjectProxyFactory;
import org.springframework.jmx.proxy.JmxObjectProxyFactory;

/**
 * @author robh
 */
public class JdkProxyTests extends AbstractProxyTests {


    public JdkProxyTests(String name) {
        super(name);
    }

    protected ObjectName getObjectNameForProxy() throws Exception {
        return ObjectName.getInstance("bean:name=testBean3");
    }


    protected JmxObjectProxyFactory getProxyFactory() throws Exception {
        return new JdkJmxObjectProxyFactory();
    }

	protected MBeanServerConnection getServerConnection() throws Exception {
		return server;
	}

}
