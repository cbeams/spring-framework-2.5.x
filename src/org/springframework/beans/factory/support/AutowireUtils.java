/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.springframework.util.ClassUtils;

/**
 * Utility class that contains various methods useful for
 * the implementation of autowire-capable bean factories.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see AbstractAutowireCapableBeanFactory
 */
abstract class AutowireUtils {

	/**
	 * Sort the given constructors, preferring public constructors and "greedy" ones
	 * with a maximum of arguments. The result will contain public constructors first,
	 * with decreasing number of arguments, then non-public constructors, again with
	 * decreasing number of arguments.
	 * @param constructors the constructor array to sort
	 */
	public static void sortConstructors(Constructor[] constructors) {
		Arrays.sort(constructors, new Comparator() {
			public int compare(Object o1, Object o2) {
				Constructor c1 = (Constructor) o1;
				Constructor c2 = (Constructor) o2;
				boolean p1 = Modifier.isPublic(c1.getModifiers());
				boolean p2 = Modifier.isPublic(c2.getModifiers());
				if (p1 != p2) {
					return (p1 ? -1 : 1);
				}
				int c1pl = c1.getParameterTypes().length;
				int c2pl = c2.getParameterTypes().length;
				return (new Integer(c1pl)).compareTo(new Integer(c2pl)) * -1;
			}
		});
	}

	/**
	 * Determine a weight that represents the class hierarchy difference between types and
	 * arguments. A direct match, i.e. type Integer -> arg of class Integer, does not increase
	 * the result - all direct matches means weight 0. A match between type Object and arg of
	 * class Integer would increase the weight by 2, due to the superclass 2 steps up in the
	 * hierarchy (i.e. Object) being the last one that still matches the required type Object.
	 * Type Number and class Integer would increase the weight by 1 accordingly, due to the
	 * superclass 1 step up the hierarchy (i.e. Number) still matching the required type Number.
	 * Therefore, with an arg of type Integer, a constructor (Integer) would be preferred to a
	 * constructor (Number) which would in turn be preferred to a constructor (Object).
	 * All argument weights get accumulated.
	 * @param paramTypes the parameter types to match
	 * @param args the arguments to match
	 * @return the accumulated weight for all arguments
	 */
	public static int getTypeDifferenceWeight(Class[] paramTypes, Object[] args) {
		int result = 0;
		for (int i = 0; i < paramTypes.length; i++) {
			if (!ClassUtils.isAssignableValue(paramTypes[i], args[i])) {
				return Integer.MAX_VALUE;
			}
			if (args[i] != null) {
				Class superClass = args[i].getClass().getSuperclass();
				while (superClass != null) {
					if (ClassUtils.isAssignable(paramTypes[i], superClass)) {
						result++;
						superClass = superClass.getSuperclass();
					}
					else {
						superClass = null;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB.
	 * @param pd the PropertyDescriptor of the bean property
	 * @return whether the bean property is excluded
	 */
	public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		Method wm = pd.getWriteMethod();
		if (wm == null) {
			return false;
		}
		if (wm.getDeclaringClass().getName().indexOf("$$") == -1) {
			// Not a CGLIB method so it's OK.
			return false;
		}
		// It was declared by CGLIB, but we might still want to autowire it
		// if it was actually declared by the superclass.
		Class superclass = wm.getDeclaringClass().getSuperclass();
		return !ClassUtils.hasMethod(superclass, wm.getName(), wm.getParameterTypes());
	}

	/**
	 * Return whether the setter method of the given bean property is defined
	 * in any of the given interfaces.
	 * @param pd the PropertyDescriptor of the bean property
	 * @param interfaces the Set of interfaces (Class objects)
	 * @return whether the setter method is defined by an interface
	 */
	public static boolean isSetterDefinedInInterface(PropertyDescriptor pd, Set interfaces) {
		Method setter = pd.getWriteMethod();
		if (setter != null) {
			Class targetClass = setter.getDeclaringClass();
			for (Iterator it = interfaces.iterator(); it.hasNext();) {
				Class ifc = (Class) it.next();
				if (ifc.isAssignableFrom(targetClass) &&
						ClassUtils.hasMethod(ifc, setter.getName(), setter.getParameterTypes())) {
					return true;
				}
			}
		}
		return false;
	}

}
