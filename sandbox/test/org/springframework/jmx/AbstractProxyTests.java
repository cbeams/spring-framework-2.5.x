/*
 * Created on Jul 13, 2004
 */
package org.springframework.jmx;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Descriptor;

import org.springframework.jmx.proxy.InvalidInvocationException;
import org.springframework.jmx.proxy.JmxObjectProxyFactory;
import org.springframework.jmx.assemblers.AbstractReflectionBasedModelMBeanInfoAssembler;
import junit.framework.TestCase;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.beans.PropertyDescriptor;

/**
 * @author robh
 */
public abstract class AbstractProxyTests extends TestCase {

    protected static final String OBJECT_NAME = "spring:test=proxy";

    protected MBeanServer server;

    protected JmxTestBean target;

    public void setUp() throws Exception{
        server = MBeanServerFactory.createMBeanServer();

        target = new JmxTestBean();
        target.setAge(100);
        target.setName("Rob Harrop");

        JmxMBeanAdapter adapter = new JmxMBeanAdapter();
        Map beans = new HashMap();
        beans.put(OBJECT_NAME, target);
        adapter.setServer(server);
        adapter.setBeans(beans);
        adapter.setAssembler(new ProxyTestAssembler());
        adapter.registerBeans();

    }

    public void tearDown() throws Exception{
        server = null;
    }

    protected abstract JmxObjectProxyFactory getProxyFactory() throws Exception;
    
    protected abstract MBeanServerConnection getServerConnection() throws Exception;

    protected IJmxTestBean getProxy() throws Exception {
        return getProxy(getProxyFactory());
    }

    protected IJmxTestBean getProxy(JmxObjectProxyFactory factory)
            throws Exception {
        ObjectName objectName = ObjectNameManager.getInstance(OBJECT_NAME);
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
        try {
        proxy.setName("Rob Harrop");
        } catch(Exception t) {
            t.printStackTrace();
            t.getCause().printStackTrace();
            throw t;
        }

        assertEquals("The name of the bean should have been updated", "Rob Harrop", target.getName());
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

    private static class ProxyTestAssembler extends AbstractReflectionBasedModelMBeanInfoAssembler {
        protected boolean includeReadAttribute(Method method) {
            return true;
        }

        protected boolean includeWriteAttribute(Method method) {
            if("setAge".equals(method.getName())) {
                return false;
            }
            return true;
        }

        protected boolean includeOperation(Method method) {
            if("dontExposeMe".equals(method.getName())) {
                return false;
            }
            return true;
        }

        protected String getOperationDescription(Method method) {
            return method.getName();
        }

        protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
            return propertyDescriptor.getDisplayName();
        }

        protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {

        }

        protected void populateOperationDescriptor(Descriptor descriptor, Method method) {

        }

        protected String getDescription(Object bean) {
            return "";
        }

        protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, Object bean) {

        }
    }
}