/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerConnection;
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
        return ObjectNameManager.getInstance("bean:name=testBean3");
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
    
    public void testDifferentProxiesForIgnoreInvocations() throws Exception {
        JmxObjectProxyFactory pf = getProxyFactory();
        pf.setIgnoreInvalidInvocations(false);
        
        IJmxTestBean proxy1 = getProxy();
        IJmxTestBean proxy2 = getProxy(pf);
        
        assertFalse("The classes should be different", proxy1.getClass().equals(proxy2.getClass()));
        
    }

	protected MBeanServerConnection getServerConnection() throws Exception{
		return server;
	}
}