package org.springframework.util;

import java.io.InputStream;

/**
 * Utility class for class loading, and for diagnostic purposes
 * to analyze the ClassLoader hierarchy for any object.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 02 April 2001
 * @see ClassLoader
 */
public abstract class ClassLoaderUtils {

	/**
	 * Load a resource from the class path, first trying the thread context
	 * class loader, then the class loader of the given class.
	 * @param clazz a class to try the class loader of
	 * @param name the resource name
	 * @return an input stream for reading the resource,
	 * or null if not found
	 * @see ClassLoader#getResourceAsStream
	 * @see Class#getResourceAsStream
	 */
	public static InputStream getResourceAsStream(Class clazz, String name) {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		InputStream in = null;
		if (ccl != null) {
			in = ccl.getResourceAsStream(name);
		}
		if (in == null) {
			in = clazz.getResourceAsStream(name);
		}
		return in;
	}

	/**
	 * Show the class loader hierarchy for this class.
	 * @param obj object to analyze loader hierarchy for
	 * @param role a description of the role of this class in the application
	 * (e.g., "servlet" or "EJB reference")
	 * @param delim line break
	 * @param tabText text to use to set tabs
	 * @return a String showing the class loader hierarchy for this class
	 */
	public static String showClassLoaderHierarchy(Object obj, String role, String delim, String tabText) {
		String s = "object of " + obj.getClass() + ": role is " + role + delim;
		return s + showClassLoaderHierarchy(obj.getClass().getClassLoader(), delim, tabText, 0);
	}

	/**
	 * Show the class loader hierarchy for this class.
	 * @param cl class loader to analyze hierarchy for
	 * @param delim line break
	 * @param tabText text to use to set tabs
	 * @param indent nesting level (from 0) of this loader; used in pretty printing
	 * @return a String showing the class loader hierarchy for this class
	 */
	public static String showClassLoaderHierarchy(ClassLoader cl, String delim, String tabText, int indent) {
		if (cl == null) {
			String s = "null classloader " + delim;
			ClassLoader ctxcl = Thread.currentThread().getContextClassLoader();
			s += "Context class loader=" + ctxcl + " hc=" + ctxcl.hashCode();
			return s;
		}
		String s = ""; //"ClassLoader: ";
		for (int i = 0; i < indent; i++)
			s += tabText;
		s += cl + " hc=" + cl.hashCode() + delim;
		ClassLoader parent = cl.getParent();
		return s + showClassLoaderHierarchy(parent, delim, tabText, indent + 1);
	}

	/**
	 * Return a path suitable for use with Class.getResource. Built by taking
	 * the package of the specified	class file,	converting all dots	('.') to slashes
	 * ('/'), adding a trailing	slash, and concatenating the specified resource	name
	 * to this.	As such, this function may be used to build	a path suitable	for	loading
	 * a resource file that	is in the same package as a	class file.
	 * @param clazz	the	Class whose	package	will be	used as	the	base.
	 * @param resourceName the resource	name to	append.	A leading slash	is optional.
	 * @return the built-up	resource path
	 * @see java.lang.Class#getResource
	 */
	public static String addResourcePathToPackagePath(Class	clazz, String resourceName)	{
		if (!resourceName.startsWith("/"))
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		else
			return classPackageAsResourcePath(clazz) + resourceName;
	}

	/**
	 * Given an	input class	object,	returns	a string which consists	of the class's package
	 * name	as a pathname, i.e., a leading '/' is added, and all dots ('.')	are	replaced by
	 * slashes ('/'). A	trailing slash is <b>not</b> added.
	 * @param clazz	the	input class. A null value will result in "/" being returned.
	 * @return a path which	represents the package name, including a leading slash
	 */
	public static String classPackageAsResourcePath(Class clazz) {
		StringBuffer retval	= new StringBuffer("/");
		if (clazz != null)
			retval.append(clazz.getPackage().getName().replace('.', '/'));
		return retval.toString();
	}
  
	/**
	 * Given a fully qualified name, return a class name:
	 * e.g. com.foo.Bar returns Bar,
	 * com.foo.Bar$Inner returns Bar$Inner.
	 * @param clazz class to find name for
	 * @return the class name without the package prefix.
	 * Will include any enclosing class name, which is also after
	 * the dot.
	 */
	public static String classNameWithoutPackagePrefix(Class clazz) {
		String name = clazz.getName();
		int lastDotIndex = name.lastIndexOf(".");
		// There must be characters after the ., so we don't
		// need to program defensively for that case
		return (lastDotIndex == -1) ?
			name :					// default package
		 	name.substring(lastDotIndex + 1);
	}

}
