/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.springframework.jmx.proxy.JmxObjectProxyFactory;

/**
 * @author robh
 */
public abstract class AbstractProxyTests extends AbstractJmxTests {

    public AbstractProxyTests(String name) {
        super(name);
    }

    protected abstract ObjectName getObjectNameForProxy() throws Exception;

    protected abstract JmxObjectProxyFactory getProxyFactory() throws Exception;
    
    protected abstract MBeanServerConnection getServerConnection() throws Exception;

    protected IJmxTestBean getProxy() throws Exception {
        return getProxy(getProxyFactory());
    }

    protected IJmxTestBean getProxy(JmxObjectProxyFactory factory)
            throws Exception {
        ObjectName objectName = getObjectNameForProxy();
        factory.setProxyInterfaces(new Class[] { IJmxTestBean.class });
        return (IJmxTestBean) factory.createProxy(getServerConnection(), objectName);
    }

    public void testProxyClassIsDifferent() throws Exception {
        IJmxTestBean proxy = getProxy();
        assertTrue("The proxy class should be different than the base class",
                (proxy.getClass() != IJmxTestBean.class));
    }

    public void testDifferentProxiesSameClass() throws Exception {
        IJmxTestBean proxy1 = getProxy();
        IJmxTestBean proxy2 = getProxy();

        assertNotSame("The proxies should NOT be the same", proxy1, proxy2);
        assertSame("The proxy classes should be the same", proxy1.getClass(),
                proxy2.getClass());
    }

    public void testGetAttributeValue() throws Exception {
        IJmxTestBean proxy1 = getProxy();
        int age = proxy1.getAge();

        assertEquals("The age should be 100", 100, age);
    }
    
    public void testSetAttributeValue() throws Exception {
        IJmxTestBean proxy = getProxy();
        proxy.setName("Rob Harrop");
        
        JmxTestBean bean = (JmxTestBean)getContext().getBean("testBean");
        assertEquals("The name of the bean should have been updated", "Rob Harrop", bean.getName());
    }
    
    public void testSetReadOnlyAttribute() throws Exception {
        IJmxTestBean proxy = getProxy();
        try {
            proxy.setAge(900);
            fail("Should not be able to write to a read only attribute");
        } catch(InvalidInvocationException ex) {
            // success
        }
    }

    public void testInvokeNoArgs() throws Exception {
        IJmxTestBean proxy = getProxy();
        long result = proxy.myOperation();
        assertEquals("The operation should return 1", 1, result);
    }

    public void testInvokeArgs() throws Exception {
        IJmxTestBean proxy = getProxy();
        int result = proxy.add(1, 2);

        assertEquals("The operation should return 3", 3, result);
    }

    public void testInvokeUnexposedMethodWithException() throws Exception {
        JmxObjectProxyFactory factory = getProxyFactory();
        factory.setIgnoreInvalidInvocations(false);

        IJmxTestBean bean = getProxy(factory);

        try {
            bean.dontExposeMe();
            fail("Method dontExposeMe should throw an exception");
        } catch (InvalidInvocationException desired) {
            // success
        }
    }

    public void testInvokeUnexposedMethodWithNoOp() throws Exception {
        IJmxTestBean proxy = getProxy();
        // dontExposeMe throws RuntimeException, but the proxy should
        // execute a no-op because the method is not exposed by the metadata
        // assembler
        proxy.dontExposeMe();
    }
}