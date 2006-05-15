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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link BridgeMethodResolver} that uses reflection to resolve the bridged {@link Method}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public final class ReflectionBasedBridgeMethodResolver implements BridgeMethodResolver {

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
			if (isBridgedCandidateFor(candidateMethod, bridgeMethod)) {
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
		Map typeParameterMap = createTypeVariableMap(bridgeMethod.getDeclaringClass());
		for (int i = 0; i < candidateMethods.size(); i++) {
			Method candidateMethod = (Method) candidateMethods.get(i);
			if (isBridgeMethodFor(bridgeMethod, candidateMethod, typeParameterMap)) {
				return candidateMethod;
			}
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the supplied '<code>candidateMethod</code>' can be consider a validate
	 * candidate for the {@link Method} that is {@link Method#isBridge() bridged} by the supploed
	 * {@link Method bridge Method}. This method performs inexpensive checks and can be used quickly filter
	 * for a set of possible matches.
	 */
	private boolean isBridgedCandidateFor(Method candidateMethod, Method bridgeMethod) {
		return !candidateMethod.equals(bridgeMethod) && candidateMethod.getName().equals(bridgeMethod.getName())
						&& candidateMethod.getParameterTypes().length == bridgeMethod.getParameterTypes().length;
	}

	/**
	 * Determines whether or not the bridge {@link Method} is the bridge for the supplied candidate
	 * {@link Method}.
	 */
	protected boolean isBridgeMethodFor(Method bridgeMethod, Method candidateMethod, Map typeVariableMap) {
		Method m = findGenericDeclaration(bridgeMethod);
		return m == null ? false : isResolvedTypeMatch(m, candidateMethod, typeVariableMap);

	}

	/**
	 * Searches for the generic {@link Method} declaration whose erased signature matches that of the
	 * supplied bridge method.
	 * @throws IllegalStateException if the generic declaration cannot be found.
	 */
	protected Method findGenericDeclaration(Method bridgeMethod) {
		// search parent types for method that has same signature as bridge
		Class superclass = bridgeMethod.getDeclaringClass().getSuperclass();
		while (superclass != Object.class) {
			Method m = searchForMatch(superclass, bridgeMethod);
			if (m != null) {
				return m;
			}
			superclass = superclass.getSuperclass();
		}

		// search interfaces
		Class[] interfaces = bridgeMethod.getDeclaringClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			Class anInterface = interfaces[i];
			Method m = searchForMatch(anInterface, bridgeMethod);
			if (m != null) {
				return m;
			}
		}

		throw new IllegalStateException("Unable to locate generic definition for bridge method: '" + bridgeMethod + "'.");
	}

	/**
	 * Returns <code>true</code> if the {@link Type} signature of both the supplied
	 * {@link Method#getGenericParameterTypes() generic Method} and concrete {@link Method} are equal
	 * after resolving all {@link TypeVariable TypeVariables} using the supplied
	 * {@link #createTypeVariableMap TypeVariable Map}, otherwise returns <code>false</code>.
	 */
	private boolean isResolvedTypeMatch(Method genericMethod, Method candidateMethod, Map typeVariableMap) {
		Type[] genericParameters = genericMethod.getGenericParameterTypes();
		Class[] resolvedTypes = new Class[genericParameters.length];
		for (int i = 0; i < genericParameters.length; i++) {
			Type genericParameter = genericParameters[i];
			if (genericParameter instanceof TypeVariable) {
				TypeVariable tv = (TypeVariable) genericParameter;
				resolvedTypes[i] = (Class) typeVariableMap.get(tv.getName());
			}
			else {
				resolvedTypes[i] = (Class) genericParameter;
			}
		}
		return Arrays.equals(resolvedTypes, candidateMethod.getParameterTypes());
	}

	/**
	 * If the supplied {@link Class} has a declared {@link Method} whose signature matches that of
	 * the supplied {@link Method}, then this matching {@link Method} is returned, otherwise <code>null</code>
	 * is returned.
	 */
	private Method searchForMatch(Class type, Method bridgeMethod) {
		try {
			return type.getDeclaredMethod(bridgeMethod.getName(), bridgeMethod.getParameterTypes());
		}
		catch (NoSuchMethodException e) {
			return null;
		}
	}


	/**
	 * Builds a mapping of {@link TypeVariable#getName TypeVariable names} to concrete {@link Class} for the
	 * specified {@link Class}. Searches all super types, enclosing types and interfaces.
	 * @see #populateTypeMapFromParameterizedType(Map, ParameterizedType)
	 */
	protected Map createTypeVariableMap(Class cls) {
		Map typeVariableMap = new HashMap();

		// super class
		Type genericType = cls.getGenericSuperclass();
		Class type = cls.getSuperclass();
		while (type != Object.class) {
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt1 = (ParameterizedType) genericType;
				populateTypeMapFromParameterizedType(typeVariableMap, pt1);
			}
			genericType = type.getGenericSuperclass();
			type = type.getSuperclass();
		}

		// enclosing class
		type = cls;
		while (type.isMemberClass()) {
			genericType = type.getGenericSuperclass();
			if (genericType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericType;
				populateTypeMapFromParameterizedType(typeVariableMap, pt);
			}
			type = type.getEnclosingClass();
		}

		// interfaces
		Type[] genericInterfaces = cls.getGenericInterfaces();
		for (int i = 0; i < genericInterfaces.length; i++) {
			Type genericInterface = genericInterfaces[i];
			if (genericInterface instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) genericInterface;
				populateTypeMapFromParameterizedType(typeVariableMap, pt);
			}
		}

		return typeVariableMap;
	}

	/**
	 * Reads the {@link TypeVariable TypeVariables} from the supplied {@link ParameterizedType} and adds mappings
	 * corresponding to the {@link TypeVariable#getName TypeVariable name} -> concrete type to the supplied
	 * {@link Map}. Consider this case:
	 * <pre class="code>
	 * public interface Foo<S, T> {
	 *  ..
	 * }
	 *
	 * public class FooImpl implements Foo<String, Integer> {
	 *  ..
	 * }
	 * </pre>
	 * For '<code>FooImpl</code>' the following mappings would be added to the {@link Map}:
	 * {S=java.lang.String, T=java.lang.Integer}.
	 */
	private void populateTypeMapFromParameterizedType(Map typeVariableMap, ParameterizedType type) {
		Type[] actualTypeArguments = type.getActualTypeArguments();
		TypeVariable[] typeVariables = ((Class) type.getRawType()).getTypeParameters();
		for (int i = 0; i < actualTypeArguments.length; i++) {
			Type actualTypeArgument = actualTypeArguments[i];
			if (actualTypeArgument instanceof Class) {
				typeVariableMap.put(typeVariables[i].getName(), (Class) actualTypeArgument);
			}
		}
	}
}
