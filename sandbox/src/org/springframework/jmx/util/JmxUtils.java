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
package org.springframework.jmx.util;

import java.lang.reflect.Method;
import java.util.List;

import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.jmx.exceptions.MethodNameTooShortException;
import org.springframework.jmx.exceptions.MBeanServerNotFoundException;
import org.springframework.util.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generic utility methods to support Spring JMX.
 * 
 * @author Rob Harrop
 */
public class JmxUtils {

    private static final Log log = LogFactory.getLog(JmxUtils.class);

    private static final String GET = "get";

    private static final String SET = "set";

    /**
     * Determines whether the supplied method is actually the getter or setter
     * for a JavaBean style property.
     * 
     * @param method
     * @return
     */
    public static boolean isProperty(Method method) {

        if (!meetsPropertyCriteria(method)) {
            return false;
        }

        // check if this method is a getter or a setter
        if (isGetterInternal(method)) {
            return true;
        } else if (isSetterInternal(method)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check to see if the supplied <tt>Method</tt> is a JavaBean getter
     * 
     * @param method
     *            The <tt>Method</tt> to check
     * @return True if the <tt>Method</tt> is a getter, otherwise false.
     */
    public static boolean isGetter(Method method) {
        if (!meetsPropertyCriteria(method)) {
            return false;
        }

        return isGetterInternal(method);
    }

    /**
     * Check to see if the supplied Method is a JavaBean setter
     * 
     * @param method
     *            The <tt>Method</tt> to check
     * @return True if the <tt>Method</tt> is a setter, otherwise false.
     */
    public static boolean isSetter(Method method) {
        if (!meetsPropertyCriteria(method)) {
            return false;
        }

        return isSetterInternal(method);
    }

    /**
     * Checks to see if the specified method is a JavaBean property "getter"
     * 
     * @param method
     * @return
     */
    private static boolean isGetterInternal(Method method) {
        if (method.getName().startsWith(GET)) {
            return ((method.getReturnType() != void.class) && (method
                    .getParameterTypes().length == 0));
        } else {
            return false;
        }
    }

    /**
     * Checks to see if the specified method is a JavaBean property "setter"
     * 
     * @param method
     * @return
     */
    private static boolean isSetterInternal(Method method) {
        if (method.getName().startsWith(SET)) {
            return ((method.getReturnType() == void.class) && (method
                    .getParameterTypes().length == 1));
        } else {
            return false;
        }
    }

    /**
     * Checks to see if the specified method meets the criteria to be a
     * property. That is the method name is at least 4 characters long and the 4
     * character is uppercase.
     * 
     * @param method
     * @return
     */
    private static boolean meetsPropertyCriteria(Method method) {
        String name = method.getName();

        // name should be at least four chars to be a property
        if (name.length() < 4) {
            return false;
        }

        // the fourth character should be uppercase
        if (!Character.isUpperCase(name.charAt(3))) {
            return false;
        }

        return true;
    }

    /**
     * Shrinks an array of ModelMBeanOperationInfo objects to the specified
     * size.
     * 
     * @param source
     * @param count
     * @return
     */
    public static ModelMBeanOperationInfo[] shrink(
            ModelMBeanOperationInfo[] source, int count) {
        ModelMBeanOperationInfo[] dest = new ModelMBeanOperationInfo[count];
        for (int x = 0; x < count; x++) {
            dest[x] = source[x];
        }
        return dest;
    }

    /**
     * Shrinks an array of ModelMBeanAttributeInfo objects to the specified size
     * 
     * @param source
     * @param count
     * @return
     */
    public static ModelMBeanAttributeInfo[] shrink(
            ModelMBeanAttributeInfo[] source, int count) {
        ModelMBeanAttributeInfo[] dest = new ModelMBeanAttributeInfo[count];
        for (int x = 0; x < count; x++) {
            dest[x] = source[x];
        }
        return dest;
    }

    /**
     * Given a getter/setter this method returns the name of the attribute. Does
     * not check that the method is actually a getter/setter
     * 
     * @param method
     *            The method to retrieve the attribute name from.
     * @return The attribute name
     */
    public static String getAttributeName(Method method) {

        int length = method.getName().length();

        if (length <= 3) {
            throw new MethodNameTooShortException("Method name "
                    + method.getName()
                    + " is too short to be an attribute name");
        }

        char[] attributeName = new char[length - 3];
        method.getName().getChars(3, length, attributeName, 0);
        attributeName[0] = Character.toLowerCase(attributeName[0]);
        return new String(attributeName);
    }

    /**
     * Converts an array of type names into an array of <tt>Class</tt>
     * instances.
     * 
     * @param typeNames
     *            The names of the types to obtain
     * @return An array of <tt>Class</tt>
     */
    public static Class[] typeNamesToTypes(String[] typeNames)
            throws ClassNotFoundException {
        Class[] types = null;

        if ((typeNames != null) && (typeNames.length > 0)) {
            types = new Class[typeNames.length];

            for (int x = 0; x < typeNames.length; x++) {
                types[x] = ClassUtils.forName(typeNames[x]);
            }
        }

        return types;
    }

    /**
     * Converts an array of MBeanParameterInfo into an array of <tt>Class</tt>
     * instances corresponding to the parameters.
     * @param paramInfo
     * @return
     * @throws ClassNotFoundException
     */
    public static Class[] parameterInfoToTypes(MBeanParameterInfo[] paramInfo)
            throws ClassNotFoundException {
        Class[] types = null;

        if ((paramInfo != null) && (paramInfo.length > 0)) {
            types = new Class[paramInfo.length];

            for (int x = 0; x < paramInfo.length; x++) {
                types[x] = ClassUtils.forName(paramInfo[x].getType());
            }
        }

        return types;
    }
    
    /**
     * Creates a String[] representing the signature of a
     * method. Each element in the array is the fully qualified class
     * name of the corresponding argument in the methods signature.
     * @param method
     * @return
     */
    public static String[] getMethodSignature(Method method) {
        Class[] types = method.getParameterTypes();
        String[] signature = new String[types.length];

        for (int x = 0; x < types.length; x++) {
            signature[x] = types[x].getName();
        }

        return signature;
    }

    public static MBeanServer locateMBeanServer() {
        List servers = MBeanServerFactory.findMBeanServer(null);

        // check to see if an MBeanServer is registered
        if ((servers == null) || (servers.size() == 0)) {
            throw new MBeanServerNotFoundException(
                    "Unable to locate an MBeanServer instance");
        }

        //TODO: Throw exception if more than one exists

        MBeanServer server = (MBeanServer)servers.get(0);

        if (log.isDebugEnabled()) {
            log.debug("Found MBeanServer: " + server.toString());
        }

        return server;
    }

}