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
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.jmx.InvalidInvocationException;
import org.springframework.jmx.JmxUtils;
import org.springframework.jmx.exceptions.ProxyCreationException;

/**
 * Proxy to a JMX managed resource. Uses CGLIB to capture invocations and
 * reroutes them to the managed resource via the MBeanServer. By default ths
 * proxy will obtain the Class of the resource and attempt to proxy that.
 * However, if you want to use a different interface or set of interfaces then
 * set the proxyInterfaces property and CGLIB will generate a proxy that
 * implements those interfaces.
 * 
 * Attempting to invoke a method on a proxy that is not exposed by the managed
 * resources remote interface will by default, result in NoOp. However setting
 * the ignoreInvalidInvocations property to false will cause the proxy to throw
 * an Exception.
 * 
 * @author Rob Harrop
 */
public class CglibJmxObjectProxyFactory extends AbstractJmxObjectProxyFactory {

    /**
     * The MBeanServer instance to redirect calls to
     */
    private MBeanServer server;

    /**
     * The ObjectName to point all calls at.
     */
    private ObjectName objectName;

    /**
     * Index for the ReadAttributeInterceptor
     */
    private static final int READ_ATTRIBUTE_INTERCEPTOR = 0;

    /**
     * Index for the WriteAttributeInterceptor
     */
    private static final int WRITE_ATTRIBUTE_INTERCEPTOR = 1;

    /**
     * Index for the OperationInterceptor
     */
    private static final int OPERATION_INTERCEPTOR = 2;

    /**
     * Index for the NoOpInterceptor
     */
    private static final int NO_OP_INTERCEPTOR = 3;

    /**
     * Index for the InvalidInvocationInterceptor
     */
    private static final int INVALID_INVOCATION_INTERCEPTOR = 4;

    /**
     * Creates a proxy for the managed resource specified by objectName.
     * 
     * @param objectName
     *            The ObjectName of the resource to proxy
     * @param server
     *            The MBeanServer that the resource is registered with
     */
    public Object createProxy(final MBeanServer server,
            final ObjectName objectName) {

        // store server and name
        this.server = server;
        this.objectName = objectName;

        MBeanInfo info = null;

        try {

            info = server.getMBeanInfo(objectName);

            // now make the proxy
            Enhancer e = new Enhancer();

            if (proxyInterfaces != null) {
                e.setInterfaces(proxyInterfaces);
            } else {
                //  get the class represented by this object
                Class instanceClass = getClassForInstance(objectName, server);
                e.setSuperclass(instanceClass);
            }

            e.setCallbackFilter(new JmxProxyCallbackFilter(info));

            Callback[] callbacks = new Callback[4];
            callbacks[0] = new ReadAttributeInterceptor();
            callbacks[1] = new WriteAttributeInterceptor();
            callbacks[2] = new OperationInvokeInterceptor();
            callbacks[3] = new NoOpInterceptor();
            callbacks[4] = new InvalidInvocationInterceptor();

            e.setCallbacks(callbacks);

            return e.create();

        } catch (JMException ex) {
            // a problem occured - most likely at the server
            throw new ProxyCreationException(
                    "A problem occured at the JMX Server. Check the root cause for more details.",
                    ex);
        }
    }

    /**
     * CallbackFilter to assign Interceptors to methods. JMX attributes are
     * assigned a Read/Write AttributeInterceptor as appropriate and operations
     * are assigned an OperationInterceptor. All invalid invocations are
     * assigned either NoOpInterceptor or InvalidInvocationInterceptor dependent
     * on the setting of the ignoreInvalidInvocations flag.
     * 
     * @author robh
     */
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

            if (JmxUtils.isGetter(method)) {
                return acceptGetter(method);
            } else if (JmxUtils.isSetter(method)) {
                return acceptSetter(method);
            } else {
                return acceptMethod(method);
            }
        }

        /**
         * Checks to see if the setter method is exposed on the managed
         * resource's management interface and if the attribute is readable. If
         * the attribute is not exposed then either NO_OP_INTERCEPTOR or
         * INVALID_INVOCATION_INTERCEPTOR is used, dependent on the setting of
         * ignoreInvalidInvocations. If the attribute is write only uses
         * INVALID_INVOCATION_INTERCEPTOR
         * 
         * @param method
         * @return
         */
        private int acceptGetter(Method method) {
            String attribName = JmxUtils.getAttributeName(method);

            MBeanAttributeInfo[] inf = info.getAttributes();

            for (int x = 0; x < inf.length; x++) {

                if (inf[x].getName().equals(attribName)) {
                    if (inf[x].isReadable()) {
                        return READ_ATTRIBUTE_INTERCEPTOR;
                    } else {
                        return INVALID_INVOCATION_INTERCEPTOR;
                    }
                }
            }

            return getNonExposedInterceptor();
        }

        /**
         * Checks to see if the setter method is exposed on the managed
         * resource's management interface and if the attribute is writable. If
         * the attribute is not exposed then either NO_OP_INTERCEPTOR or
         * INVALID_INVOCATION_INTERCEPTOR is used, dependent on the setting of
         * ignoreInvalidInvocations. If the attribute is read only uses
         * INVALID_INVOCATION_INTERCEPTOR
         * 
         * @param method
         * @return
         */
        private int acceptSetter(Method method) {
            String attribName = JmxUtils.getAttributeName(method);

            MBeanAttributeInfo[] inf = info.getAttributes();

            for (int x = 0; x < inf.length; x++) {

                if (inf[x].getName().equals(attribName)) {
                    if (inf[x].isWritable()) {
                        return WRITE_ATTRIBUTE_INTERCEPTOR;
                    } else {
                        return INVALID_INVOCATION_INTERCEPTOR;
                    }
                }
            }

            return getNonExposedInterceptor();
        }

        private int acceptMethod(Method method) {

            MBeanOperationInfo[] inf = info.getOperations();

            for (int x = 0; x < inf.length; x++) {
                if (inf[x].getName().equals(method.getName())) {
                    return OPERATION_INTERCEPTOR;
                }
            }

            return getNonExposedInterceptor();
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

        public int getNonExposedInterceptor() {
            if (CglibJmxObjectProxyFactory.this.ignoreInvalidInvocations) {
                return NO_OP_INTERCEPTOR;
            } else {
                return INVALID_INVOCATION_INTERCEPTOR;
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

            String attribName = JmxUtils.getAttributeName(method);
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

            String attribName = JmxUtils.getAttributeName(method);
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
                signature = JmxUtils.getMethodSignature(method);

                synchronized (signatureCache) {
                    signatureCache.put(method, signature);
                }
            }

            return server.invoke(objectName, method.getName(), args, signature);

        }
    }

    /**
     * Interceptor used to ignore invalid invocations.
     * 
     * @author Rob Harrop
     */
    private class NoOpInterceptor implements MethodInterceptor {
        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            return null;
        }
    }

    /**
     * Interceptor used to handle invalid invocations.
     * 
     * @author robh
     */
    private class InvalidInvocationInterceptor implements MethodInterceptor {

        public Object intercept(Object target, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            throw new InvalidInvocationException("Operation/Attribute "
                    + method.getName()
                    + " is not exposed on the management interface");
        }
    }
}