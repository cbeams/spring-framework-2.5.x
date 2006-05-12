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

package org.springframework.util;

import org.springframework.core.JdkVersion;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public final class ReflectionBasedBridgeMethodResolver implements BridgeMethodResolver {

	/**
	 * Given a synthetic {@link java.lang.reflect.Method#isBridge bridge Method} returns the {@link java.lang.reflect.Method}
	 * being bridged. A bridge method may be created by the compiler when extending a parameterized
	 * type whose methods have parameterized arguments. During runtime invocation the bridge {@link java.lang.reflect.Method} may
	 * be invoked and/or used via reflection. When attempting to locate annotations on {@link java.lang.reflect.Method Methods} it is
	 * wise to check for bridge {@link java.lang.reflect.Method Methods} as appropriate and find the bridged {@link java.lang.reflect.Method}.
	 * <p/>See <a href="http://java.sun.com/docs/books/jls/third_edition/html/expressions.html#15.12.4.5">
	 * The Java Language Specification</a> for more details on the use of bridge methods.
	 *
	 * @return the bridged {@link java.lang.reflect.Method} if the supplied {@link java.lang.reflect.Method} is a valid bridge, otherwise the supplied {@link java.lang.reflect.Method}
	 * @throws IllegalStateException if no bridged {@link java.lang.reflect.Method} can be found.
	 */
	public Method resolveBridgeMethod(Method bridgeMethod) {
		Assert.notNull(bridgeMethod, "'bridgeMethod' cannot be null.");
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_15 || !bridgeMethod.isBridge()) {
			return bridgeMethod;
		}

		// gather all methods with matching name and parameter size
		List candidateMethods = new ArrayList();
		Method[] methods = bridgeMethod.getDeclaringClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method candidateMethod = methods[i];
			if (isCandidateFor(candidateMethod, bridgeMethod)) {
				candidateMethods.add(candidateMethod);
			}
		}

		// now perform simple quick checks
		if (candidateMethods.isEmpty()) {
			throw new IllegalStateException("Unable to locate bridged method for bridge method '" + bridgeMethod + "'");
		}
		else if (candidateMethods.size() == 1) {
			return (Method) candidateMethods.get(0);
		}
		else {
			return searchCandidates(candidateMethods, bridgeMethod);
		}

	}

	private Method searchCandidates(List candidateMethods, Method bridgeMethod) {
		for (int i = 0; i < candidateMethods.size(); i++) {
			Method candidateMethod = (Method) candidateMethods.get(i);
			if (isBridgeMethodFor(bridgeMethod, candidateMethod)) {
				return candidateMethod;
			}
		}
		return null;
	}

	private boolean isCandidateFor(Method candidateMethod, Method bridgeMethod) {
		return !candidateMethod.equals(bridgeMethod) && candidateMethod.getName().equals(bridgeMethod.getName())
						&& candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length;
	}

	protected boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod) {
		return (findGenericDefinition(candidateMethod) == null ? false : true);
	}

	protected Method findGenericDefinition(Method baseMethod) {
		Map typeVariableMap = createTypeVariableMap(baseMethod.getDeclaringClass());
		Type genericType = baseMethod.getDeclaringClass().getGenericSuperclass();
		Class candidateType = baseMethod.getDeclaringClass().getSuperclass();

		// search superclasses
		while (candidateType != Object.class) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;

				Method m = searchForGenericDefinition(pt, baseMethod, typeVariableMap);
				if (m != null) {
					return m;
				}
			}
			genericType = candidateType.getGenericSuperclass();
			candidateType = candidateType.getSuperclass();
		}

		// search interfaces
		Type[] interfaces = baseMethod.getDeclaringClass().getGenericInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			Type type = interfaces[i];
			if (type instanceof ParameterizedType) {
				Method m = searchForGenericDefinition((ParameterizedType) type, baseMethod, typeVariableMap);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}

	protected Map createTypeVariableMap(Class cls) {
		Map typeVariableMap = new HashMap();

		// super class
		Type genericType = cls.getGenericSuperclass();
		Class type = cls.getSuperclass();
		while (type != Object.class) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt1 = (ParameterizedType) genericType;
				addTypes(typeVariableMap, pt1);
			}
			genericType = type.getGenericSuperclass();
			type = type.getSuperclass();
		}

		// enclosing class
		type = cls;
		while(type.isMemberClass()) {
			genericType = type.getGenericSuperclass();
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt1 = (ParameterizedType) genericType;
				addTypes(typeVariableMap, pt1);
			}
			 type = type.getEnclosingClass();
		}

		// interfaces
		Type[] genericInterfaces = cls.getGenericInterfaces();
		for (int i = 0; i < genericInterfaces.length; i++) {
			Type genericInterface = genericInterfaces[i];
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericInterface;
				addTypes(typeVariableMap, pt);
			}
		}

		return typeVariableMap;
	}

	private void addTypes(Map typeVariableMap, ParameterizedType type) {
		Type[] actualTypeArguments = type.getActualTypeArguments();
		TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
		for (int i = 0; i < actualTypeArguments.length; i++) {
			Type actualTypeArgument = actualTypeArguments[i];
			if (actualTypeArgument instanceof Class) {
				typeVariableMap.put(typeVariables[i].getName(), (Class) actualTypeArgument);
			}
		}
	}

	protected Method searchForGenericDefinition(ParameterizedType pt, Method baseMethod, Map typeVariableMap) {
		Class candidateType = (Class) pt.getRawType();
		Method[] methods = candidateType.getDeclaredMethods();
		Type[] concreteParams = baseMethod.getGenericParameterTypes();

		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			Type[] types = method.getGenericParameterTypes();
			if (baseMethod.getName().equals(method.getName())
							&& concreteParams.length == method.getParameterTypes().length
							&& isGenericParameterSetFor(types, concreteParams, typeVariableMap)) {
				return method;
			}
		}

		return null;
	}

	protected boolean isGenericParameterSetFor(Type[] genericParameters, Type[] concreteParameters, Map typeVariableMap) {
		for (int i = 0; i < genericParameters.length; i++) {
			Type genericParameter = genericParameters[i];
			Type concreteParameter = concreteParameters[i];
			if (genericParameter instanceof TypeVariable) {
				TypeVariable typeVariable = (TypeVariable) genericParameter;
				Class declaredVariableType = (Class)typeVariableMap.get(typeVariable.getName());
				if (declaredVariableType == null || !declaredVariableType.equals(concreteParameter)) {
					return false;
				}
			}
			else if (!genericParameter.equals(concreteParameter)) {
				return false;
			}
		}

		return true;
	}
}
