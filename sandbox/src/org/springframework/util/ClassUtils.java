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
     * @throws IllegalArgumentException
     *             if the class is null
     */
    public static String getShortName(Class clazz) {
        Assert.notNull(clazz);
        return getShortName(clazz.getName());
    }

    /**
     * Returns the uncaptilized short string name of a java class.
     * 
     * @param clazz The class
     * @return The short name rendered as a property.
     */
    public static String getShortNameAsProperty(Class clazz) {
        return StringUtils.uncapitalize(getShortName(clazz));
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
        char[] charArray = className.toCharArray();
        int lastDot = 0;
        for (int i = 0; i < charArray.length; i++) {
            if (charArray[i] == PACKAGE_SEPARATOR_CHAR) {
                lastDot = i + 1;
            } else if (charArray[i] == INNER_CLASS_SEPARATOR_CHAR) {
                charArray[i] = PACKAGE_SEPARATOR_CHAR;
            }
        }
        return new String(charArray, lastDot, charArray.length - lastDot);
    }

    /**
     * Returns a static method of a class.
     * 
     * @param methodName
     *            The static method name.
     * @param clazz
     *            The class which defines the method.
     * @param args
     *            The parameter types to the method.
     * @return The static method, or <code>null</code> if no static method
     *         was found.
     * @throws IllegalArgumentException
     *             if the method name is blank or the clazz is null.
     */
    public static Method getStaticMethod(
        String methodName,
        Class clazz,
        Class[] args) {
        Assert.isTrue(StringUtils.hasText(methodName));
        Assert.notNull(clazz);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Attempting to get static method '"
                        + methodName
                        + "' on class "
                        + clazz
                        + " with arguments '"
                        + ArrayUtils.toString(args)
                        + "'");
            }
            Method method = clazz.getDeclaredMethod(methodName, args);
            if ((method.getModifiers() & Modifier.STATIC) != 0) {
                return method;
            } else {
                logger.warn(
                    "Found method '" + methodName + "', but it is not static.");
                return null;
            }
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Return a path suitable for use with ClassLoader.getResource (also
     * suitable for use with Class.getResource by prepending a slash ('/') to
     * the return value. Built by taking the package of the specified class
     * file, converting all dots ('.') to slashes ('/'), adding a trailing
     * slash if necesssary, and concatenating the specified resource name to
     * this. <br/>As such, this function may be used to build a path suitable
     * for loading a resource file that is in the same package as a class file,
     * although {link org.springframework.core.io.ClassPathResource} is usually
     * even more convenient.
     * 
     * @param clazz
     *            the Class whose package will be used as the base.
     * @param resourceName
     *            the resource name to append. A leading slash is optional.
     * @return the built-up resource path
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Class#getResource(String)
     */
    public static String addResourcePathToPackagePath(
        Class clazz,
        String resourceName) {
        if (!resourceName.startsWith("/"))
            return classPackageAsResourcePath(clazz) + "/" + resourceName;
        else
            return classPackageAsResourcePath(clazz) + resourceName;
    }

    /**
     * Given an input class object, returns a string which consists of the
     * class's package name as a pathname, i.e., all dots ('.') are replaced by
     * slashes ('/'). Neither a leading nor trailing slash is added. The result
     * could be concatenated with a slash and the name of a resource, and fed
     * directly to ClassLoader.getResource(). For it to be fed to
     * Class.getResource, a leading slash would also have to be prepended to
     * the return value.
     * 
     * @param clazz
     *            the input class. A null value or the default (empty) package
     *            will result in an empty string ("") being returned.
     * @return a path which represents the package name
     * @see java.lang.ClassLoader#getResource(String)
     * @see java.lang.Class#getResource(String)
     */
    public static String classPackageAsResourcePath(Class clazz) {
        if (clazz == null || clazz.getPackage() == null) {
            return "";
        }
        return clazz.getPackage().getName().replace('.', '/');
    }

}
