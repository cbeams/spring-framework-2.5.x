
package org.springframework.aop.framework.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.AttributeRegistry;
import org.springframework.aop.framework.StaticMethodPointcut;

/**
 * Utility methods used by the AOP framework.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AopUtils.java,v 1.3 2003-10-14 18:56:29 johnsonr Exp $
 */
public class AopUtils {

	/**
	 * Return all interfaces that the given object implements as array,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as array
	 */
	public static Class[] getAllInterfaces(Object object) {
		List interfaces = getAllInterfacesAsList(object);
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Return all interfaces that the given object implements as List,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as List
	 */
	public static List getAllInterfacesAsList(Object object) {
		List interfaces = new ArrayList();
		Class clazz = object.getClass();
		while (clazz != null) {
			for (int i = 0; i < clazz.getInterfaces().length; i++) {
				Class ifc = clazz.getInterfaces()[i];
				interfaces.add(ifc);
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}
	
	/**
	 * Is the given method declared on one of these interfaces?
	 * @param m method to check
	 * @param interfaces array of interfaces we want to check
	 * @return whether the method is declared on one of these interfaces
	 */
	public static boolean methodIsOnOneOfTheseInterfaces(Method m, Class[] interfaces) {
		if (interfaces == null)
			return false;
			
		for (int i = 0; i < interfaces.length; i++) {
			if (!interfaces[i].isInterface())
				throw new IllegalArgumentException(interfaces[i].getName() + " is not an interface");
			// TODO check that the method with this name actually comes from the interface?
			try {
				interfaces[i].getDeclaredMethod(m.getName(), m.getParameterTypes());
				return true;
			}
			catch (NoSuchMethodException ex) {
				// Didn't find it...keep going
			}
		}	// for
		return false;
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out a pointcut for a class
	 * @param pc pc static or dynamic pointcut
	 * @param ar AttributeRegistry
	 * @param clazz class we're testing
	 * @param proxyInterfaces proxy interfaces. If null, all methods
	 * on class may be proxied
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(StaticMethodPointcut pc, AttributeRegistry ar, Class clazz, Class[] proxyInterfaces) {
		// Check whether it can apply on any method
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			// If we're looking only at interfaces and this method
			// isn't on any of them, skip it
			if (proxyInterfaces != null && !methodIsOnOneOfTheseInterfaces(m, proxyInterfaces)) {
				continue;
			}
			if (pc.applies(m, ar))
				return true;
		}
		return false;
	}

}
