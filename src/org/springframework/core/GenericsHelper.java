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

package org.springframework.core;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.util.Assert;

/**
 * Helper class for determining generic types of collections and maps.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class GenericsHelper {

	/**
	 * Determine the generic element type of the given Collection parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, 0);
	}

	/**
	 * Determine the generic key type of the given Map parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, 0);
	}

	/**
	 * Determine the generic value type of the given Map parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, 1);
	}

	/**
	 * Determine the generic element type of the given Collection return type.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionReturnType(Method method) {
		return getGenericReturnType(method, 0);
	}

	/**
	 * Determine the generic key type of the given Map parameter.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyReturnType(Method method) {
		return getGenericReturnType(method, 0);
	}

	/**
	 * Determine the generic value type of the given Map parameter.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueReturnType(Method method) {
		return getGenericReturnType(method, 1);
	}


	/**
	 * Extract the generic parameter type from the given method or constructor.
	 * @param methodParam the method parameter specification
	 * @param typeIndex the index of the type (e.g. 0 for Collections,
	 * 0 for Map keys, 1 for Map values)
	 * @return the generic type, or <code>null</code> if none
	 */
	private static Class getGenericParameterType(MethodParameter methodParam, int typeIndex) {
		Assert.notNull(methodParam, "MethodParameter must not be null");
		if (methodParam.getConstructor() != null) {
			return extractType(
					methodParam.getConstructor().getGenericParameterTypes()[methodParam.getParameterIndex()], typeIndex);
		}
		else {
			return extractType(
					methodParam.getMethod().getGenericParameterTypes()[methodParam.getParameterIndex()], typeIndex);
		}
	}

	/**
	 * Extract the generic return type from the given method.
	 * @param method the method to check the return type for
	 * @param typeIndex the index of the type (e.g. 0 for Collections,
	 * 0 for Map keys, 1 for Map values)
	 * @return the generic type, or <code>null</code> if none
	 */
	private static Class getGenericReturnType(Method method, int typeIndex) {
		Assert.notNull(method, "Method must not be null");
		return extractType(method.getGenericReturnType(), typeIndex);
	}

	/**
	 * Extract the generic type from the given Type object.
	 * @param type the Type to check
	 * @param typeIndex the index of the actual type argument
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractType(Type type, int typeIndex) {
		if (type instanceof ParameterizedType) {
			Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
			if (paramTypes[typeIndex] instanceof Class) {
				return (Class) paramTypes[typeIndex];
			}
		}
		return null;
	}

}
