/*
 * Created on 17-Nov-2004
 */
package org.springframework.jmx.proxy;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.exceptions.ProxyCreationException;
import org.springframework.jmx.util.JmxUtils;

/**
 * @author robh
 */
public class JmxMBeanClientInterceptor extends AbstractJmxObjectProxyFactory
        implements MethodInterceptor, InitializingBean {

    private MBeanServerConnection server;

    private ObjectName objectName;

    private Map allowedAttributes;

    private Map allowedOperations;

    private Map signatureCache = new HashMap();

    private Object proxy;

    /**
     * Checks to see if the proxy interfaces have been set
     */
    public void afterPropertiesSet() throws Exception {
        if ((this.proxyInterfaces == null)
                || (this.proxyInterfaces.length == 0)) {
            throw new IllegalArgumentException(
                    "You must specify the interface or interfaces to proxy.");
        }
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (JmxUtils.isGetter(invocation.getMethod())) {
            return invokeGetter(invocation.getMethod());
        } else if (JmxUtils.isSetter(invocation.getMethod())) {
            return invokeSetter(invocation.getMethod(),
                    invocation.getArguments()[0]);
        } else {
            return invokeMethod(invocation.getMethod(),
                    invocation.getArguments());
        }
    }

    private Object invokeSetter(Method method, Object arg) throws Exception {
        String attributeName = JmxUtils.getAttributeName(method);
        MBeanAttributeInfo inf = (MBeanAttributeInfo) allowedAttributes.get(attributeName);

        // if no attribute is returned we know
        // that it is not defined in the mgmt interface
        if (inf == null) {
            return handleUndefined(method);
        }

        if (inf.isWritable()) {
            server.setAttribute(objectName, new Attribute(attributeName, arg));
            return null;

        } else {
            throw new InvalidInvocationException("Attribute: " + attributeName
                    + " is not writable.");
        }
    }

    private Object invokeGetter(Method method) throws Exception {

        String attributeName = JmxUtils.getAttributeName(method);
        MBeanAttributeInfo inf = (MBeanAttributeInfo) allowedAttributes.get(attributeName);

        // if no attribute is returned we know
        // that it is not defined in the mgmt interface
        if (inf == null) {
            return handleUndefined(method);
        }

        if (inf.isReadable()) {
            return server.getAttribute(objectName, attributeName);

        } else {
            throw new InvalidInvocationException("Attribute: " + attributeName
                    + " is not readable.");
        }
    }

    private Object invokeMethod(Method method, Object[] args) throws Exception {
        InternalMethodCacheKey key = new InternalMethodCacheKey(
                method.getName(), method.getParameterTypes());
        MBeanOperationInfo info = (MBeanOperationInfo) allowedOperations.get(key);

        if (info == null) {
            return handleUndefined(method);
        } else {
            String[] signature = (String[]) signatureCache.get(method);

            if (signature == null) {
                signature = JmxUtils.getMethodSignature(method);

                synchronized (signatureCache) {
                    signatureCache.put(method, signature);
                }
            }
            return server.invoke(objectName, method.getName(), args, signature);
        }

    }

    private Object handleUndefined(Method method) {
        if (this.ignoreInvalidInvocations) {
            return null;
        } else {
            throw new InvalidInvocationException("Operation/Attribute "
                    + method.getName()
                    + " is not exposed on the management interface");
        }
    }

    private void queryMetadata() {
        try {
            MBeanInfo inf = server.getMBeanInfo(objectName);

            // get attributes
            MBeanAttributeInfo[] attributeInfo = inf.getAttributes();
            allowedAttributes = new HashMap(attributeInfo.length);

            for (int x = 0; x < attributeInfo.length; x++) {
                allowedAttributes.put(attributeInfo[x].getName(),
                        attributeInfo[x]);
            }

            // get operations
            MBeanOperationInfo[] operationInfo = inf.getOperations();
            allowedOperations = new HashMap(operationInfo.length);

            try {
                for (int x = 0; x < operationInfo.length; x++) {
                    MBeanOperationInfo oi = operationInfo[x];

                    allowedOperations.put(new InternalMethodCacheKey(
                            oi.getName(),
                            JmxUtils.parameterInfoToTypes(oi.getSignature())),
                            oi);
                }
            } catch (ClassNotFoundException ex) {
                throw new ProxyCreationException(
                        "Unable to locate class specified in method signature",
                        ex);
            }

        } catch (IntrospectionException ex) {
            throw new ProxyCreationException(
                    "Unable to obtain MBean metadata for bean: " + objectName,
                    ex);
        } catch (InstanceNotFoundException ex) {
            // if we are this far this shouldn't happen, but...
            throw new ProxyCreationException(
                    "Unable to obtain MBean metadata for bean: "
                            + objectName
                            + ". It is likely that this bean was unregistered during the proxy creation process.",
                    ex);
        } catch (ReflectionException ex) {
            throw new ProxyCreationException(
                    "Unable to read MBean metadata for bean: " + objectName, ex);
        } catch (IOException ex) {
            throw new ProxyCreationException(
                    "An IOException occurred when communicating with the MBeanServer. "
                            + "It is likely that you are communicating with a remote MBeanServer. "
                            + "Check the inner exception for exact details.",
                    ex);
        }
    }

    public Object createProxy(MBeanServerConnection server,
            ObjectName objectName) {

        this.server = server;
        this.objectName = objectName;

        if (proxy == null) {
            queryMetadata();

            ProxyFactory pf = new ProxyFactory();

            for (int x = 0; x < proxyInterfaces.length; x++) {
                pf.addInterface(proxyInterfaces[x]);
            }

            pf.addAdvice(this);

            proxy = pf.getProxy();
        }
        return proxy;
    }

    private static class InternalMethodCacheKey {
        private String name;

        private Class[] params;

        public InternalMethodCacheKey(String name, Class[] params) {
            this.name = name;

            if (params == null) {
                this.params = new Class[] {};
            } else {
                this.params = params;
            }
        }

        public int hashCode() {
            return name.hashCode();
        }

        public boolean equals(Object other) {
            if (other == null)
                return false;
            if (other == this)
                return true;

            InternalMethodCacheKey otherKey = null;

            if (other instanceof InternalMethodCacheKey) {
                otherKey = (InternalMethodCacheKey) other;

                return name.equals(otherKey.name)
                        && Arrays.equals(params, otherKey.params);
            } else {
                return false;
            }
        }
    }

}