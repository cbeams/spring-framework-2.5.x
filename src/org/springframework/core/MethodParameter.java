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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.util.Assert;

/**
 * Helper class that encapsulates the specification of a method parameter,
 * that is, a Method or Constructor plus a parameter index.
 * Useful as a specification object to pass along.
 *
 * <p>Used by GenericCollectionTypeResolver, BeanWrapperImpl and AbstractBeanFactory.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see GenericCollectionTypeResolver
 * @see org.springframework.beans.BeanWrapperImpl
 * @see org.springframework.beans.factory.support.AbstractBeanFactory
 */
public class MethodParameter {

	private Method method;

	private Constructor constructor;

	private final int parameterIndex;

	private int nestingLevel;


	/**
	 * Create a new MethodParameter for the given method, with nesting level 1.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	public MethodParameter(Method method, int parameterIndex) {
		this(method, parameterIndex, 1);
	}

	/**
	 * Create a new MethodParameter for the given method.
	 * @param method the Method to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	public MethodParameter(Method method, int parameterIndex, int nestingLevel) {
		Assert.notNull(method, "Method must not be null");
		Assert.isTrue(parameterIndex >= 0, "Parameter index must not be negative");
		Assert.isTrue(parameterIndex < method.getParameterTypes().length,
				"Parameter index must not exceed " + (method.getParameterTypes().length - 1));
		this.method = method;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
	}

	/**
	 * Create a new MethodParameter for the given constructor, with nesting level 1.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	public MethodParameter(Constructor constructor, int parameterIndex) {
		this(constructor, parameterIndex, 1);
	}

	/**
	 * Create a new MethodParameter for the given constructor.
	 * @param constructor the Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 * @param nestingLevel the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List)
	 */
	public MethodParameter(Constructor constructor, int parameterIndex, int nestingLevel) {
		Assert.notNull(constructor, "Constructor must not be null");
		Assert.isTrue(parameterIndex >= 0, "Parameter index must not be negative");
		Assert.isTrue(parameterIndex < constructor.getParameterTypes().length,
				"Parameter index must not exceed " + (constructor.getParameterTypes().length - 1));
		this.constructor = constructor;
		this.parameterIndex = parameterIndex;
		this.nestingLevel = nestingLevel;
	}


	/**
	 * Return the Method held, if any.
	 * <p>Note: Either Method or Constructor is available.
	 * @return the Method, or <code>null</code> if none
	 */
	public Method getMethod() {
		return this.method;
	}

	/**
	 * Return the Constructor held, if any.
	 * <p>Note: Either Method or Constructor is available.
	 * @return the Constructor, or <code>null</code> if none
	 */
	public Constructor getConstructor() {
		return this.constructor;
	}

	/**
	 * Return the index of the method/constructor parameter.
	 * @return the parameter index (never negative)
	 */
	public int getParameterIndex() {
		return this.parameterIndex;
	}

	/**
	 * Return the nesting level of the target type
	 * (typically 1; e.g. in case of a List of Lists, 1 would indicate the
	 * nested List, whereas 2 would indicate the element of the nested List).
	 */
	public int getNestingLevel() {
		return this.nestingLevel;
	}

	/**
	 * Increase this parameter's nesting level.
	 * @see #getNestingLevel()
	 */
	public void increaseNestingLevel() {
		this.nestingLevel++;
	}

	/**
	 * Decrease this parameter's nesting level.
	 * @see #getNestingLevel()
	 */
	public void decreaseNestingLevel() {
		this.nestingLevel--;
	}


	/**
	 * Create a new MethodParameter for the given method or constructor.
	 * <p>This is a convenience constructor for scenarios where a
	 * Method or Constructor reference is treated in a generic fashion.
	 * @param methodOrConstructor the Method or Constructor to specify a parameter for
	 * @param parameterIndex the index of the parameter
	 */
	public static MethodParameter forMethodOrConstructor(Object methodOrConstructor, int parameterIndex) {
		if (methodOrConstructor instanceof Method) {
			return new MethodParameter((Method) methodOrConstructor, parameterIndex);
		}
		else if (methodOrConstructor instanceof Constructor) {
			return new MethodParameter((Constructor) methodOrConstructor, parameterIndex);
		}
		else {
			throw new IllegalArgumentException(
					"Given object [" + methodOrConstructor + "] is neither a Method nor a Constructor");
		}
	}

}
