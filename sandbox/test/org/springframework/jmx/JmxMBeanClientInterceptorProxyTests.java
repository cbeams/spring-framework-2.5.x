/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.jmx.proxy.JmxMBeanClientInterceptor;
import org.springframework.jmx.proxy.JmxObjectProxyFactory;

/**
 * @author robh
 */
public class JmxMBeanClientInterceptorProxyTests extends AbstractProxyTests {

    protected ObjectName getObjectNameForProxy() throws Exception {
        return ObjectNameManager.getInstance("bean:name=testBean3");
    }

    protected JmxObjectProxyFactory getProxyFactory() throws Exception {
        return new JmxMBeanClientInterceptor();
    }


    protected MBeanServerConnection getServerConnection() throws Exception {
        return server;
    }

    protected String getApplicationContextPath() {
        return "org/springframework/jmx/metadataAssembler.xml";
    }

}
