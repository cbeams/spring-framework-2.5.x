
package org.springframework.aop.framework.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.Pointcut;

/**
 * Utility methods used by the AOP framework.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AopUtils.java,v 1.6 2003-11-15 15:30:14 johnsonr Exp $
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
	 * @param targetClass class we're testing
	 * @param proxyInterfaces proxy interfaces. If null, all methods
	 * on class may be proxied
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(Pointcut pc, Class targetClass, Class[] proxyInterfaces) {
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}
		
		// It may apply to the class
		// Check whether it can apply on any method
		// Checks public methods, including inherited methods
		Method[] methods = targetClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			// If we're looking only at interfaces and this method
			// isn't on any of them, skip it
			if (proxyInterfaces != null && !methodIsOnOneOfTheseInterfaces(m, proxyInterfaces)) {
				continue;
			}
			if (pc.getMethodMatcher().matches(m, targetClass))
				return true;
		}
		return false;
	}
	
	public static boolean canApply(Advisor advice, Class targetClass, Class[] proxyInterfaces) {
		if (advice instanceof InterceptionIntroductionAdvisor) {
			return ((InterceptionIntroductionAdvisor) advice).getClassFilter().matches(targetClass);
		}
		
		InterceptionAroundAdvisor interceptionAdvice = (InterceptionAroundAdvisor) advice;
		return canApply(interceptionAdvice.getPointcut(), targetClass, proxyInterfaces);
	}

}
