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
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Helper class for determining element types of collections and maps.
 *
 * <p>Mainly intended for usage within the framework, determining the
 * target type of values to be added to a collection or map
 * (to be able to attempt type conversion if appropriate).
 *
 * <p>Only usable on Java 5. Use an appropriate JdkVersion check before
 * calling this class, if a fallback for JDK 1.3/1.4 is desirable.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.beans.BeanWrapperImpl
 * @see JdkVersion
 */
public abstract class GenericCollectionTypeResolver {

	/**
	 * Determine the generic element type of the given Collection parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, Collection.class, 0);
	}

	/**
	 * Determine the generic key type of the given Map parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, Map.class, 0);
	}

	/**
	 * Determine the generic value type of the given Map parameter.
	 * @param methodParam the method parameter specification
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueParameterType(MethodParameter methodParam) {
		return getGenericParameterType(methodParam, Map.class, 1);
	}

	/**
	 * Determine the generic element type of the given Collection return type.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionReturnType(Method method) {
		return getGenericReturnType(method, Collection.class, 0);
	}

	/**
	 * Determine the generic key type of the given Map parameter.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyReturnType(Method method) {
		return getGenericReturnType(method, Map.class, 0);
	}

	/**
	 * Determine the generic value type of the given Map parameter.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueReturnType(Method method) {
		return getGenericReturnType(method, Map.class, 1);
	}


	/**
	 * Extract the generic parameter type from the given method or constructor.
	 * @param methodParam the method parameter specification
	 * @param source the source class/interface defining the generic parameter types
	 * @param typeIndex the index of the type (e.g. 0 for Collections,
	 * 0 for Map keys, 1 for Map values)
	 * @return the generic type, or <code>null</code> if none
	 */
	private static Class getGenericParameterType(MethodParameter methodParam, Class source, int typeIndex) {
		Assert.notNull(methodParam, "MethodParameter must not be null");
		int idx = methodParam.getParameterIndex();
		if (methodParam.getConstructor() != null) {
			return extractType(methodParam.getConstructor().getGenericParameterTypes()[idx], source, typeIndex);
		}
		else {
			return extractType(methodParam.getMethod().getGenericParameterTypes()[idx], source, typeIndex);
		}
	}

	/**
	 * Extract the generic return type from the given method.
	 * @param method the method to check the return type for
	 * @param source the source class/interface defining the generic parameter types
	 * @param typeIndex the index of the type (e.g. 0 for Collections,
	 * 0 for Map keys, 1 for Map values)
	 * @return the generic type, or <code>null</code> if none
	 */
	private static Class getGenericReturnType(Method method, Class source, int typeIndex) {
		Assert.notNull(method, "Method must not be null");
		return extractType(method.getGenericReturnType(), source, typeIndex);
	}


	/**
	 * Extract the generic type from the given Type object.
	 * @param type the Type to check
	 * @param typeIndex the index of the actual type argument
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractType(Type type, Class source, int typeIndex) {
		if (type instanceof ParameterizedType) {
			return extractTypeFromParameterizedType((ParameterizedType) type, source, typeIndex);
		}
		if (type instanceof Class) {
			return extractTypeFromClass((Class) type, source, typeIndex);
		}
		return null;
	}

	/**
	 * Extract the generic type from the given ParameterizedType object.
	 * @param ptype the ParameterizedType to check
	 * @param typeIndex the index of the actual type argument
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractTypeFromParameterizedType(ParameterizedType ptype, Class source, int typeIndex) {
		if (!(ptype.getRawType() instanceof Class)) {
			return null;
		}
		Class rawType = (Class) ptype.getRawType();
		if (!source.isAssignableFrom(rawType)) {
			return null;
		}
		Class fromSuperclassOrInterface = extractType(rawType, source, typeIndex);
		if (fromSuperclassOrInterface != null) {
			return fromSuperclassOrInterface;
		}
		Type[] paramTypes = ptype.getActualTypeArguments();
		if (paramTypes == null || typeIndex >= paramTypes.length) {
			return null;
		}
		Type paramType = paramTypes[typeIndex];
		if (paramType instanceof WildcardType) {
			Type[] lowerBounds = ((WildcardType) paramType).getLowerBounds();
			if (lowerBounds != null && lowerBounds.length > 0) {
				paramType = lowerBounds[0];
			}
		}
		if (paramType instanceof ParameterizedType) {
			paramType = ((ParameterizedType) paramType).getRawType();
		}
		if (paramType instanceof Class) {
			return (Class) paramType;
		}
		return null;
	}

	/**
	 * Extract the generic type from the given Class object.
	 * @param clazz the Class to check
	 * @param typeIndex the index of the actual type argument
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractTypeFromClass(Class clazz, Class source, int typeIndex) {
		if (clazz.getSuperclass() != null && source.isAssignableFrom(clazz.getSuperclass())) {
			return extractType(clazz.getGenericSuperclass(), source, typeIndex);
		}
		Type[] ifcs = clazz.getGenericInterfaces();
		if (ifcs != null) {
			for (int i = 0; i < ifcs.length; i++) {
				Type ifc = ifcs[i];
				Type rawType = ifc;
				if (ifc instanceof ParameterizedType) {
					rawType = ((ParameterizedType) ifc).getRawType();
				}
				if (rawType instanceof Class && source.isAssignableFrom((Class) rawType)) {
					return extractType(ifc, source, typeIndex);
				}
			}
		}
		return null;
	}

}
