/*
 * The Spring Framework is published under the terms of the Apache Software
 * License.
 */
package org.springframework.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Static utility functions dealing with java class objects.
 * 
 * @author Keith Donald, adapted from jakarta-commons-lang's ClassUtils
 */
public class ClassUtils {

    /**
     * The package separator character '.'
     */
    private static final char PACKAGE_SEPARATOR_CHAR = '.';

    /**
     * The inner class separator character '$'
     */
    private static final char INNER_CLASS_SEPARATOR_CHAR = '$';

    private static final Log logger = LogFactory.getLog(ClassUtils.class);

    // static utility class
    private ClassUtils() {
    }

    /**
     * Gets the class name without the qualified package name.
     * 
     * @param clazz
     *            the class to get the short name for, must not be <code>null</code>
     * @return the class name of the class without the package name
     */
    public static String getShortName(Class clazz) {
        Assert.notNull(clazz);
        return getShortName(clazz.getName());
    }

    /**
     * Gets the class name without the qualified package name.
     * 
     * @param className
     *            the className to get the short name for, must not be empty or
     *            <code>null</code>
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException
     *             if the className is empty
     */
    public static String getShortName(String className) {
        Assert.isTrue(StringUtils.hasText(className));
        char[] chars = className.toCharArray();
        int lastDot = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == PACKAGE_SEPARATOR_CHAR) {
                lastDot = i + 1;
            } else if (chars[i] == INNER_CLASS_SEPARATOR_CHAR) {
                chars[i] = PACKAGE_SEPARATOR_CHAR;
            }
        }
        return new String(chars, lastDot, chars.length - lastDot);
    }

    /**
     * Returns a static method of a class.
     * 
     * @param name
     *            The static method name.
     * @param clazz
     *            The class which defines the method.
     * @param args
     *            The parameter types to the method.
     * @return The static method, or <code>null</code> if no static method
     *         was found.
     */
    public static Method getStaticMethod(String name, Class clazz, Class[] args) {
        Assert.isTrue(StringUtils.hasText(name));
        Assert.notNull(clazz);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Attempting to get static method '" + name
                        + "' on class " + clazz + " with arguments '"
                        + ArrayUtils.toString(args) + "'");
            }
            Method method = clazz.getDeclaredMethod(name, args);
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                return method;
            } else {
                logger.warn("Found method '" + name
                        + "', but it is not static.");
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
