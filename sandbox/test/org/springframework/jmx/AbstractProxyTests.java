/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

import javax.management.ObjectName;

import org.springframework.jmx.invokers.cglib.InvalidInvocationException;
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

    private JmxTestBean getProxy() throws Exception {
        return getProxy(getProxyFactory());
    }

    private JmxTestBean getProxy(JmxObjectProxyFactory factory)
            throws Exception {
        ObjectName objectName = getObjectNameForProxy();
        return (JmxTestBean) factory.createProxy(server, objectName);
    }

    private IJmxTestBean getProxyByInterface(JmxObjectProxyFactory factory,
            Class iface) throws Exception {
        ObjectName objectName = getObjectNameForProxy();
        factory.setProxyInterfaces(new Class[] { iface });
        return (IJmxTestBean) factory.createProxy(server, objectName);
    }

    public void testProxyClassIsDifferent() throws Exception {
        JmxTestBean proxy = getProxy();
        assertTrue("The proxy class should be different than the base class",
                (proxy.getClass() != JmxTestBean.class));
    }

    public void testDifferentProxiesSameClass() throws Exception {
        JmxTestBean proxy1 = getProxy();
        JmxTestBean proxy2 = getProxy();

        assertNotSame("The proxies should NOT be the same", proxy1, proxy2);
        assertSame("The proxy classes should be the same", proxy1.getClass(),
                proxy2.getClass());
    }

    public void testInvokeNoArgs() throws Exception {
        JmxTestBean proxy = getProxy();
        long result = proxy.myOperation();
        assertEquals("The operation should return 1", 1, result);
    }

    public void testInvokeArgs() throws Exception {
        JmxTestBean proxy = getProxy();
        int result = proxy.add(1, 2);
        assertEquals("The operation should return 3", 3, result);
    }

    public void testInvokeUnexposedMethodWithNoOp() throws Exception {
        JmxTestBean proxy = getProxy();
        // dontExposeMe throws RuntimeException, but the proxy should
        // execute a no-op because the method is not exposed by the metadata
        // assembler
        proxy.dontExposeMe();
    }

    public void testInvokeUnexposedMethodWithException() throws Exception {
        JmxObjectProxyFactory factory = getProxyFactory();
        factory.setIgnoreInvalidInvocations(false);

        JmxTestBean bean = getProxy(factory);

        try {
            bean.dontExposeMe();
            fail("Method dontExposeMe should throw an exception");
        } catch (InvalidInvocationException desired) {
            // success
        }
    }

    public void testProxyWithInterface() throws Exception {
        JmxObjectProxyFactory factory = getProxyFactory();
        IJmxTestBean bean = getProxyByInterface(factory, IJmxTestBean.class);
        
        int result = bean.add(2, 2);
        
        assertEquals("The result of the invocation should be 4", 4, result);
    }
}