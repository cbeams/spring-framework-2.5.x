/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.springframework.jmx.proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.jmx.exceptions.ProxyCreationException;
import org.springframework.util.StringUtils;

/**
 * @author Rob Harrop
 *  
 */
public class CglibJmxObjectProxyFactory implements JmxObjectProxyFactory {

    /**
     * The MBeanServer instance to redirect calls to
     */
    private MBeanServer server;

    /**
     * The ObjectName to point all calls at.
     */
    private ObjectName objectName;

    private static final int READ_ATTRIBUTE_INTERCEPTOR = 0;

    private static final int WRITE_ATTRIBUTE_INTERCEPTOR = 1;

    private static final int OPERATION_INTERCEPTOR = 2;

    private static final int NON_EXPOSED_INTERCEPTOR = 3;

    public Object createProxy(final MBeanServer server,
            final ObjectName objectName) {

        // store server and name
        this.server = server;
        this.objectName = objectName;
        
        ObjectInstance instance = null;
        MBeanInfo info = null;

        try {
            // get the object instance info
            instance = server.getObjectInstance(objectName);
            info = server.getMBeanInfo(objectName);

            // get the class represented by this object
            Class instanceClass = Class.forName(instance.getClassName());

            // now make the proxy
            Enhancer e = new Enhancer();
            e.setSuperclass(instanceClass);
            e.setCallbackFilter(new JmxProxyCallbackFilter(info));
            
            Callback[] callbacks = new Callback[4];
            callbacks[0] = new ReadAttributeInterceptor();
            callbacks[1] = new WriteAttributeInterceptor();
            callbacks[2] = new OperationInvokeInterceptor();
            callbacks[3] = new NoOpInterceptor();
            
            e.setCallbacks(callbacks);
            
            return e.create();
            
        } catch (InstanceNotFoundException ex) {
            // invalid ObjectName provided
            throw new ProxyCreationException(
                    "Unable to locate object specified by object name:"
                            + objectName
                            + ". Check the supplied ObjectName is valid");
        } catch (ClassNotFoundException ex) {
            // Unable to load class
            throw new ProxyCreationException("Unable to load class ["
                    + instance.getClassName() + "] for MBean [" + objectName
                    + "]. Ensure that this class is on the classpath.");
        } catch (JMException ex) {
            // a problem occured - most likely at the server
            throw new ProxyCreationException(
                    "A problem occured at the JMX Server. Check the root cause for more details.",
                    ex);
        }
    }

    /**
     * Given a get/set method this will return the appropriate
     * property/attribute name
     * 
     * @param method
     *            The Method instance representing the attribute
     * @return The attribute name
     */
    private String getAttributeName(Method method) {
        String attribName = StringUtils.delete(method.getName(), "get");
        attribName = StringUtils.delete(attribName, "set");

        return StringUtils.uncapitalize(attribName);
    }

    private class JmxProxyCallbackFilter implements CallbackFilter {

        /**
         * The MBeanInfo about the managed resource we are proxying.
         */
        private MBeanInfo info;

        /**
         * Creates a new instance of JmxProxyCallbackFilter for a managed
         * resource with the supplied setup.
         * 
         * @param info
         *            The MBeanInfo for the managed resource we are proxying
         */
        public JmxProxyCallbackFilter(MBeanInfo info) {
            this.info = info;
        }

        /**
         * Java Bean style properties are mapped to specific interceptors, one
         * for get and one for set. Methods are mapped to a third interceptors
         * that uses MBeanServer.invoke().
         * 
         * All methods/properties that are not exposed as JMX
         * operations/attributes are sent to a fourth callback. This callback
         * can be configured to ignore nonexposed methods (perform a no-op) or
         * to throw an exception.
         * 
         * @param method
         *            The Method object to test for a callback
         */
        public int accept(Method method) {

            String methodName = method.getName();

            if ((method.getParameterTypes().length == 0 && (method.getReturnType() != void.class))
                    && (methodName.startsWith("get"))) {
                return acceptGetter(method);
            } else if ((method.getParameterTypes().length == 1)
                    && (method.getReturnType() == void.class)
                    && (methodName.startsWith("set"))) {
                return acceptSetter(method);
            } else {
                return acceptMethod(method);
            }
        }

        /**
         * Checks to see if the getter method is exposed in the managed
         * resource's management interface and if the attribute is readable. If
         * so then this returns <tt>READ_ATTRIBUTE_INTERCEPTOR</tt> otherwise
         * it returns <tt>NON_EXPOSED_INTERCEPTOR</tt>
         * 
         * @param method
         * @return
         */
        private int acceptGetter(Method method) {
            String attribName = getAttributeName(method);

            MBeanAttributeInfo[] inf = info.getAttributes();

            for (int x = 0; x < inf.length; x++) {

                if (inf[x].getName().equals(attribName)) {
                    if (inf[x].isReadable()) {
                        return READ_ATTRIBUTE_INTERCEPTOR;
                    } else {
                        return NON_EXPOSED_INTERCEPTOR;
                    }
                }
            }

            return NON_EXPOSED_INTERCEPTOR;
        }

        /**
         * Checks to see if the setter method is exposed on the managed
         * resource's management interface and if the attribute is writable. If
         * so this returns <tt>WRITE_ATTRIBUTE_INTERCEPTOR</tt> otherwise it
         * returns <tt>NON_EXPOSED_INTERCEPTOR</tt>
         * 
         * @param method
         * @return
         */
        private int acceptSetter(Method method) {
            String attribName = getAttributeName(method);

            MBeanAttributeInfo[] inf = info.getAttributes();

            for (int x = 0; x < inf.length; x++) {

                if (inf[x].getName().equals(attribName)) {
                    if (inf[x].isWritable()) {
                        return WRITE_ATTRIBUTE_INTERCEPTOR;
                    } else {
                        return NON_EXPOSED_INTERCEPTOR;
                    }
                }
            }

            return NON_EXPOSED_INTERCEPTOR;
        }

        private int acceptMethod(Method method) {

            MBeanOperationInfo[] inf = info.getOperations();

            for (int x = 0; x < inf.length; x++) {
                if (inf[x].getName().equals(method.getName())) {
                    return OPERATION_INTERCEPTOR;
                }
            }

            return NON_EXPOSED_INTERCEPTOR;
        }

        public int hashCode() {
            return info.hashCode();
        }

        public boolean equals(Object other) {
            if (other == null)
                return false;

            if (other == this)
                return true;

            JmxProxyCallbackFilter otherFilter;

            if (other instanceof JmxProxyCallbackFilter) {
                otherFilter = (JmxProxyCallbackFilter) other;
                return info.equals(otherFilter.info);
            } else {
                return false;
            }
        }
    }

    /**
     * Interceptor used for attribute read (getAttribute) operations.
     * 
     * @author Rob Harrop
     */
    private class ReadAttributeInterceptor implements MethodInterceptor {

        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {

            String attribName = getAttributeName(method);
            return server.getAttribute(objectName, attribName);
        }
    }

    /**
     * Interceptor used for attribute write (setAttribute) operations.
     * 
     * @author Rob Harrop
     */
    private class WriteAttributeInterceptor implements MethodInterceptor {

        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {

            String attribName = getAttributeName(method);
            server.setAttribute(objectName, new Attribute(attribName, args[0]));
            return null;
        }
    }

    /**
     * Interceptor used for operation invoke operations.
     * 
     * @author Rob Harrop
     */
    private class OperationInvokeInterceptor implements MethodInterceptor {

        /**
         * Caches the method signatures.
         */
        private Map signatureCache = new HashMap();

        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {

            // try to get from cache
            String[] signature = (String[]) signatureCache.get(method);

            // if not retreived from cache then create and cache
            if (signature == null) {
                signature = getMethodSignature(method);
            }

            return server.invoke(objectName, method.getName(), args, signature);

        }

        /**
         * @param method
         * @return
         */
        private String[] getMethodSignature(Method method) {
            Class[] types = method.getParameterTypes();
            String[] signature = new String[types.length];

            for (int x = 0; x < types.length; x++) {
                signature[x] = types[x].getName();
            }

            synchronized (signatureCache) {
                signatureCache.put(method, signature);
            }

            return signature;
        }
    }

    private class NoOpInterceptor implements MethodInterceptor {
        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            return null;
        }
    }
}