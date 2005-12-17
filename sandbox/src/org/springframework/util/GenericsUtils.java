/*
 * Copyright 2002-2005 the original author or authors.
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

/**
 * @author Juergen Hoeller
 * @since 15.12.2005
 */
public abstract class GenericsUtils {

	public static Class determineGenericType(Method method, int argIndex, int typeIndex) {
		Type[] types = method.getGenericParameterTypes();
		Type type = types[argIndex];
		if (type instanceof ParameterizedType) {
			Type[] paramTypes = ((ParameterizedType) type).getActualTypeArguments();
			if (paramTypes[typeIndex] instanceof Class) {
				return (Class) paramTypes[typeIndex];
			}
		}
		return Object.class;
	}

	public static Class determineCollectionType(Method method, int argIndex) {
		return determineGenericType(method, argIndex, 0);
	}

	public static Class determineMapKeyType(Method method, int argIndex) {
		return determineGenericType(method, argIndex, 0);
	}

	public static Class determineMapValueType(Method method, int argIndex) {
		return determineGenericType(method, argIndex, 1);
	}

}
