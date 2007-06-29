/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Utility to work with generic type parameters. Mainly for internal use within
 * the framework.
 * 
 * <p>
 * Only usable on JDK 1.5 and higher. Use an appropriate {@link JdkVersion}
 * check before calling this class, if a fallback for JDK 1.4 is desirable.
 * 
 * @author Ramnivas Laddad
 * @since 2.1
 */
public abstract class GenericUtils {
	/**
	 * Check if the return value of the right hand side method may be assigned
	 * to the left hand side type following the Java generics rules.
	 * 
	 * This is a convenience method that extracts the return type from a method
	 * that will be on the right hand side of an assignment. It also accepts
	 * lhsType as Object to allow working with Java version prior to Java 5.
	 * 
	 * @param lhsType left hand side of assignement
	 * @param rhsType right hand side of assingment
	 * @return true if rhs is assiganble to lhs
	 */
	public static boolean isAssignable(Object lhsType, Method rhsMethod) {
		return isAssignable((Type) lhsType, rhsMethod.getGenericReturnType());
	}

	/**
	 * Check if the right hand side type may be assigned to the left hand side
	 * type following the Java generics rules.
	 * 
	 * @param lhsType left hand side of assignement
	 * @param rhsType right hand side of assingment
	 * @return true if rhs is assiganble to lhs
	 */
	public static boolean isAssignable(Type lhsType, Type rhsType) {
		if (lhsType == rhsType) {
			return true;
		}

		if ((lhsType instanceof Class) && (rhsType instanceof Class)) {
			return ClassUtils.isAssignable((Class) lhsType, (Class) rhsType);
		}

		if ((lhsType instanceof ParameterizedType) && (rhsType instanceof ParameterizedType)) {
			return isAssignable((ParameterizedType) lhsType, (ParameterizedType) rhsType);
		}

		if (lhsType instanceof WildcardType) {
			return isAssignable((WildcardType) lhsType, rhsType);
		}
		return true;
	}

	/**
	 * Get generic parameter types.
	 * 
	 * This method's sole purpose is to make client code work with Java versions
	 * prior to Java 5.
	 * 
	 * @param method method for which generic parameters are sought
	 * @return array containing generic paramters types
	 */
	public static Object[] getGenericParameterTypes(Method method) {
		return method.getGenericParameterTypes();
	}

	private static boolean isAssignable(ParameterizedType lhsType, ParameterizedType rhsType) {
		Type[] lhsTypeArguments = lhsType.getActualTypeArguments();
		Type[] rhsTypeArguments = rhsType.getActualTypeArguments();

		if (lhsTypeArguments.length != rhsTypeArguments.length) {
			return false;
		}

		for (int size = lhsTypeArguments.length, i = 0; i < size; ++i) {
			if (lhsTypeArguments != rhsTypeArguments) {
				if (!isAssignable(lhsTypeArguments[i], rhsTypeArguments[i])) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isAssignable(WildcardType lhsType, Type rhsType) {
		Type[] upperBounds = lhsType.getUpperBounds();
		Type[] lowerBounds = lhsType.getLowerBounds();

		for (int size = upperBounds.length, i = 0; i < size; ++i) {
			if (!ClassUtils.isAssignable((Class) upperBounds[i], (Class) rhsType)) {
				return false;
			}
		}

		for (int size = lowerBounds.length, i = 0; i < size; ++i) {
			if (!ClassUtils.isAssignable((Class) rhsType, (Class) lowerBounds[i])) {
				return false;
			}
		}
		return true;
	}
}
