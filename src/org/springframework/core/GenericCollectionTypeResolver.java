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

package org.springframework.core;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

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
	 * Determine the generic element type of the given Collection class
	 * (if it declares one through a generic superclass or generic interface).
	 * @param collectionClass the collection class to introspect
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionType(Class collectionClass) {
		return extractTypeFromClass(collectionClass, Collection.class, 0);
	}

	/**
	 * Determine the generic key type of the given Map class
	 * (if it declares one through a generic superclass or generic interface).
	 * @param mapClass the map class to introspect
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyType(Class mapClass) {
		return extractTypeFromClass(mapClass, Map.class, 0);
	}

	/**
	 * Determine the generic value type of the given Map class
	 * (if it declares one through a generic superclass or generic interface).
	 * @param mapClass the map class to introspect
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueType(Class mapClass) {
		return extractTypeFromClass(mapClass, Map.class, 1);
	}

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
		return getGenericReturnType(method, Collection.class, 0, 1);
	}

	/**
	 * Determine the generic element type of the given Collection return type.
	 * <p>If the specified nesting level is higher than 1, the element type of
	 * a nested Collection/Map will be analyzed.
	 * @param method the method to check the return type for
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getCollectionReturnType(Method method, int nestingLevel) {
		return getGenericReturnType(method, Collection.class, 0, nestingLevel);
	}

	/**
	 * Determine the generic key type of the given Map return type.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyReturnType(Method method) {
		return getGenericReturnType(method, Map.class, 0, 1);
	}

	/**
	 * Determine the generic key type of the given Map return type.
	 * @param method the method to check the return type for
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapKeyReturnType(Method method, int nestingLevel) {
		return getGenericReturnType(method, Map.class, 0, nestingLevel);
	}

	/**
	 * Determine the generic value type of the given Map return type.
	 * @param method the method to check the return type for
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueReturnType(Method method) {
		return getGenericReturnType(method, Map.class, 1, 1);
	}

	/**
	 * Determine the generic value type of the given Map return type.
	 * @param method the method to check the return type for
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 * @return the generic type, or <code>null</code> if none
	 */
	public static Class getMapValueReturnType(Method method, int nestingLevel) {
		return getGenericReturnType(method, Map.class, 1, nestingLevel);
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
		return extractType(methodParam, getTargetType(methodParam), source, typeIndex, methodParam.getNestingLevel(), 1);
	}

	/**
	 * Determine the target type for the given parameter specification.
	 * @param methodParam the method parameter specification
	 * @return the corresponding generic parameter or return type
	 */
	private static Type getTargetType(MethodParameter methodParam) {
		if (methodParam.getConstructor() != null) {
			return methodParam.getConstructor().getGenericParameterTypes()[methodParam.getParameterIndex()];
		}
		else {
			if (methodParam.getParameterIndex() >= 0) {
				return methodParam.getMethod().getGenericParameterTypes()[methodParam.getParameterIndex()];
			}
			else {
				return methodParam.getMethod().getGenericReturnType();
			}
		}
	}

	/**
	 * Extract the generic return type from the given method.
	 * @param method the method to check the return type for
	 * @param source the source class/interface defining the generic parameter types
	 * @param typeIndex the index of the type (e.g. 0 for Collections,
	 * 0 for Map keys, 1 for Map values)
	 * @param nestingLevel the nesting level of the target type
	 * @return the generic type, or <code>null</code> if none
	 */
	private static Class getGenericReturnType(Method method, Class source, int typeIndex, int nestingLevel) {
		return extractType(null, method.getGenericReturnType(), source, typeIndex, nestingLevel, 1);
	}

	/**
	 * Extract the generic type from the given Type object.
	 * @param methodParam the method parameter specification
	 * @param type the Type to check
	 * @param source the source collection/map Class that we check
	 * @param typeIndex the index of the actual type argument
	 * @param nestingLevel the nesting level of the target type
	 * @param currentLevel the current nested level
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractType(
			MethodParameter methodParam, Type type, Class source, int typeIndex, int nestingLevel, int currentLevel) {

		if (type instanceof ParameterizedType) {
			return extractTypeFromParameterizedType(
					methodParam, (ParameterizedType) type, source, typeIndex, nestingLevel, currentLevel);
		}
		if (type instanceof Class) {
			return extractTypeFromClass(methodParam, (Class) type, source, typeIndex, nestingLevel, currentLevel);
		}
		return null;
	}

	/**
	 * Extract the generic type from the given ParameterizedType object.
	 * @param methodParam the method parameter specification
	 * @param ptype the ParameterizedType to check
	 * @param source the expected raw source type (can be <code>null</code>)
	 * @param typeIndex the index of the actual type argument
	 * @param nestingLevel the nesting level of the target type
	 * @param currentLevel the current nested level
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractTypeFromParameterizedType(MethodParameter methodParam,
			ParameterizedType ptype, Class source, int typeIndex, int nestingLevel, int currentLevel) {

		if (!(ptype.getRawType() instanceof Class)) {
			return null;
		}
		Class rawType = (Class) ptype.getRawType();
		Type[] paramTypes = ptype.getActualTypeArguments();
		if (nestingLevel - currentLevel > 0) {
			int nextLevel = currentLevel + 1;
			Integer currentTypeIndex = (methodParam != null ? methodParam.getTypeIndexForLevel(nextLevel) : null);
			// Default is last parameter type: Collection element or Map value.
			int indexToUse = (currentTypeIndex != null ? currentTypeIndex.intValue() : paramTypes.length - 1);
			Type paramType = paramTypes[indexToUse];
			return extractType(methodParam, paramType, source, typeIndex, nestingLevel, nextLevel);
		}
		if (source != null && !source.isAssignableFrom(rawType)) {
			return null;
		}
		Class fromSuperclassOrInterface =
				extractTypeFromClass(methodParam, rawType, source, typeIndex, nestingLevel, currentLevel);
		if (fromSuperclassOrInterface != null) {
			return fromSuperclassOrInterface;
		}
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
		if (paramType instanceof GenericArrayType) {
			// A generic array type... Let's turn it into a straight array type if possible.
			Type compType = ((GenericArrayType) paramType).getGenericComponentType();
			if (compType instanceof Class) {
				return Array.newInstance((Class) compType, 0).getClass();
			}
		}
		else if (paramType instanceof Class) {
			// We finally got a straight Class...
			return (Class) paramType;
		}
		return null;
	}

	/**
	 * Extract the generic type from the given Class object.
	 * @param clazz the Class to check
	 * @param source the expected raw source type (can be <code>null</code>)
	 * @param typeIndex the index of the actual type argument
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractTypeFromClass(Class clazz, Class source, int typeIndex) {
		return extractTypeFromClass(null, clazz, source, typeIndex, 1, 1);
	}

	/**
	 * Extract the generic type from the given Class object.
	 * @param methodParam the method parameter specification
	 * @param clazz the Class to check
	 * @param source the expected raw source type (can be <code>null</code>)
	 * @param typeIndex the index of the actual type argument
	 * @param nestingLevel the nesting level of the target type
	 * @param currentLevel the current nested level
	 * @return the generic type as Class, or <code>null</code> if none
	 */
	private static Class extractTypeFromClass(
			MethodParameter methodParam, Class clazz, Class source, int typeIndex, int nestingLevel, int currentLevel) {

		if (clazz.getSuperclass() != null && isIntrospectionCandidate(clazz.getSuperclass())) {
			return extractType(methodParam, clazz.getGenericSuperclass(), source, typeIndex, nestingLevel, currentLevel);
		}
		Type[] ifcs = clazz.getGenericInterfaces();
		if (ifcs != null) {
			for (int i = 0; i < ifcs.length; i++) {
				Type ifc = ifcs[i];
				Type rawType = ifc;
				if (ifc instanceof ParameterizedType) {
					rawType = ((ParameterizedType) ifc).getRawType();
				}
				if (rawType instanceof Class && isIntrospectionCandidate((Class) rawType)) {
					return extractType(methodParam, ifc, source, typeIndex, nestingLevel, currentLevel);
				}
			}
		}
		return null;
	}

	/**
	 * Determine whether the given class is a potential candidate
	 * that defines generic collection or map types.
	 * @param clazz the class to check
	 * @return whethe the given class is assignable to Collection or Map
	 */
	private static boolean isIntrospectionCandidate(Class clazz) {
		return (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz));
	}

}
