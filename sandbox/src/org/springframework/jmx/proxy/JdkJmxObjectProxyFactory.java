/*
 * Created on Jul 22, 2004
 */
package org.springframework.jmx.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.InvalidInvocationException;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.exceptions.ProxyCreationException;

/**
 * @author robh
 */
public class JdkJmxObjectProxyFactory extends AbstractJmxObjectProxyFactory
        implements InitializingBean {

    /**
     * Builds the JDK proxy
     */
    public Object createProxy(MBeanServer server, ObjectName objectName) {
        return Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), proxyInterfaces,
                new JmxInvocationHandler(server, objectName));
    }

    /**
     * Checks to see if the proxy interfaces have been set
     */
    public void afterPropertiesSet() throws Exception {
        if ((this.proxyInterfaces == null)
                || (this.proxyInterfaces.length == 0)) {
            throw new IllegalArgumentException(
                    "You must specify the interface of interfaces to proxy.");
        }
    }

    private class JmxInvocationHandler implements InvocationHandler {

        private MBeanServer server;

        private ObjectName objectName;

        private Map allowedAttributes;

        private Map allowedOperations;
        
        private Map signatureCache = new HashMap();
        
        public JmxInvocationHandler(MBeanServer server, ObjectName objectName) {
            this.server = server;
            this.objectName = objectName;

            queryMetadata();
        }

        public Object invoke(Object target, Method method, Object[] args)
                throws Throwable {

            if (JmxUtils.isGetter(method)) {
                return invokeGetter(method);
            } else if (JmxUtils.isSetter(method)) {
                return invokeSetter(method, args[0]);
            } else {
                return invokeMethod(method, args);
            }
        }

        private Object invokeSetter(Method method, Object arg) throws Exception {
            String attributeName = JmxUtils.getAttributeName(method);
            MBeanAttributeInfo inf = (MBeanAttributeInfo) allowedAttributes
                    .get(attributeName);

            // if no attribute is returned we know
            // that it is not defined in the mgmt interface
            if (inf == null) {
                return handleUndefined(method);
            }

            if (inf.isWritable()) {
                server.setAttribute(objectName, new Attribute(attributeName,
                        arg));
                return null;

            } else {
                throw new InvalidInvocationException("Attribute: "
                        + attributeName + " is not writable.");
            }
        }

        private Object invokeGetter(Method method) throws Exception {

            String attributeName = JmxUtils.getAttributeName(method);
            MBeanAttributeInfo inf = (MBeanAttributeInfo) allowedAttributes
                    .get(attributeName);

            // if no attribute is returned we know
            // that it is not defined in the mgmt interface
            if (inf == null) {
                return handleUndefined(method);
            }

            if (inf.isReadable()) {
                return server.getAttribute(objectName, attributeName);

            } else {
                throw new InvalidInvocationException("Attribute: "
                        + attributeName + " is not readable.");
            }
        }

        private Object invokeMethod(Method method, Object[] args) throws Exception{
            InternalMethodCacheKey key = new InternalMethodCacheKey(method.getName(), method.getParameterTypes());
            MBeanOperationInfo info = (MBeanOperationInfo) allowedOperations.get(key);
            
            if(info == null) {
                return handleUndefined(method);
            } else {
                String[] signature = (String[])signatureCache.get(method);
                
                if(signature == null) {
                    signature = JmxUtils.getMethodSignature(method);
                    
                    synchronized(signatureCache) {
                        signatureCache.put(method, signature);
                    }
                }
                return server.invoke(objectName, method.getName(), args, signature);
            }

        }

        /**
         * Handles any invocations on attributes or operations that are not
         * defined in the management interface of the MBean
         * 
         * @param method
         * @return Will return null if invalid invocations are being ignored.
         */

        private Object handleUndefined(Method method) {
            if (JdkJmxObjectProxyFactory.this.ignoreInvalidInvocations) {
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

                        allowedOperations.put(new InternalMethodCacheKey(oi
                                .getName(), JmxUtils.parameterInfoToTypes(oi
                                .getSignature())), oi);
                    }
                } catch (ClassNotFoundException ex) {
                    throw new ProxyCreationException(
                            "Unable to locate class specified in method signature",
                            ex);
                }

            } catch (IntrospectionException ex) {
                throw new ProxyCreationException(
                        "Unable to obtain MBean metadata for bean: "
                                + objectName, ex);
            } catch (InstanceNotFoundException ex) {
                // if we are this far this shouldn't happen, but...
                throw new ProxyCreationException(
                        "Unable to obtain MBean metadata for bean: "
                                + objectName
                                + ". It is likely that this bean was unregistered during the proxy creation process.",
                        ex);
            } catch (ReflectionException ex) {
                throw new ProxyCreationException(
                        "Unable to read MBean metadata for bean: " + objectName,
                        ex);
            }
        }
    }

    private static class InternalMethodCacheKey {
        private String name;

        private Class[] params;
        
        public InternalMethodCacheKey(String name, Class[] params) {
            this.name = name;
            
            if(params == null) {
                this.params = new Class[]{};
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