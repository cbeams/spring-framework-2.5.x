/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

/**
 * @author robh
 */
public abstract class AbstractProxyTests extends AbstractJmxTests {

    public AbstractProxyTests(String name) {
        super(name);
    }

    protected abstract JmxTestBean getProxy() throws Exception;

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
    
    // TODO: public void testInvokeUnexposedMethod() throws Exception
}