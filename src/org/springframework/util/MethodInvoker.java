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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Helper class that allows to specify a method to invoke in a
 * declarative fashion, be it static or non-static.
 *
 * <p>Usage: Specify targetClass/targetMethod respectively
 * targetObject/targetMethod, optionally specify arguments,
 * prepare the invoker. Afterwards, you can invoke the method
 * any number of times.
 *
 * <p>Typically not used directly but via its subclasses
 * MethodInvokingFactoryBean and MethodInvokingJobDetailFactoryBean.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 19.02.2004
 * @see #prepare
 * @see #invoke
 * @see org.springframework.beans.factory.config.MethodInvokingFactoryBean
 * @see org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean
 */
public class MethodInvoker {

	private Class targetClass;

	private Object targetObject;

	private String targetMethod;

	private String staticMethod;

	private Object[] arguments;

	/** The method we will call */
	private Method methodObject;


	/**
	 * Set the target class on which to call the target method.
	 * Only necessary when the target method is static; else,
	 * a target object needs to be specified anyway.
	 * @see #setTargetObject
	 * @see #setTargetMethod
	 */
	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * Return the target class on which to call the target method.
	 */
	public Class getTargetClass() {
		return targetClass;
	}

	/**
	 * Set the target object on which to call the target method.
	 * Only necessary when the target method is not static;
	 * else, a target class is sufficient.
	 * @see #setTargetClass
	 * @see #setTargetMethod
	 */
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
		if (targetObject != null) {
			this.targetClass = targetObject.getClass();
		}
	}

	/**
	 * Return the target object on which to call the target method.
	 */
	public Object getTargetObject() {
		return targetObject;
	}

	/**
	 * Set the name of the method to be invoked.
	 * Refers to either a static method or a non-static method,
	 * depending on a target object being set.
	 * @see #setTargetClass
	 * @see #setTargetObject
	 */
	public void setTargetMethod(String targetMethod) {
		this.targetMethod = targetMethod;
	}

	/**
	 * Return the name of the method to be invoked.
	 */
	public String getTargetMethod() {
		return targetMethod;
	}

	/**
	 * Set a fully qualified static method name to invoke,
	 * e.g. "example.MyExampleClass.myExampleMethod".
	 * Convenient alternative to specifying targetClass and targetMethod.
	 * @see #setTargetClass
	 * @see #setTargetMethod
	 */
	public void setStaticMethod(String staticMethod) {
		this.staticMethod = staticMethod;
	}

	/**
	 * Set arguments for the method invocation. If this property is not set,
	 * or the Object array is of length 0, a method with no arguments is assumed.
	 */
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	/**
	 * Retrun the arguments for the method invocation.
	 */
	public Object[] getArguments() {
		return arguments;
	}


	/**
	 * Prepare the specified method.
	 * The method can be invoked any number of times afterwards.
	 * @see #getPreparedMethod
	 * @see #invoke
	 */
	public void prepare() throws ClassNotFoundException, NoSuchMethodException {
		if (this.staticMethod != null) {
			int lastDotIndex = this.staticMethod.lastIndexOf('.');
			if (lastDotIndex == -1 || lastDotIndex == this.staticMethod.length()) {
				throw new IllegalArgumentException(
						"staticMethod must be a fully qualified class plus method name: " +
						"e.g. 'example.MyExampleClass.myExampleMethod'");
			}
			String className = this.staticMethod.substring(0, lastDotIndex);
			String methodName = this.staticMethod.substring(lastDotIndex + 1);
			this.targetClass = resolveClassName(className);
			this.targetMethod = methodName;
		}

		if (this.targetClass == null) {
			throw new IllegalArgumentException("Either targetClass or targetObject is required");
		}
		if (this.targetMethod == null) {
			throw new IllegalArgumentException("targetMethod is required");
		}
		if (this.arguments == null) {
			this.arguments = new Object[0];
		}

		Class[] argTypes = new Class[this.arguments.length];
		for (int i = 0; i < this.arguments.length; ++i) {
			argTypes[i] = (this.arguments[i] != null ? this.arguments[i].getClass() : Object.class);
		}

		// Try to get the exact method first.
		try {
			this.methodObject = this.targetClass.getMethod(this.targetMethod, argTypes);
		}
		catch (NoSuchMethodException ex) {
			// Just rethrow exception if we can't get any match.
			this.methodObject = findMatchingMethod();
			if (this.methodObject == null) {
				throw ex;
			}
		}

		if (this.targetObject == null && !Modifier.isStatic(this.methodObject.getModifiers())) {
			throw new IllegalArgumentException("Target method must not be non-static without a target");
		}
	}

	/**
	 * Resolve the given class name into a Class.
	 * <p>The default implementations uses <code>ClassUtils.forName</code>,
	 * using the thread context class loader.
	 * @param className the class name to resolve
	 * @return the resolved Class
	 * @throws ClassNotFoundException if the class name was invalid
	 */
	protected Class resolveClassName(String className) throws ClassNotFoundException {
		return ClassUtils.forName(className);
	}

	/**
	 * Find a matching method with the specified name for the specified arguments.
	 * @return a matching method, or <code>null</code> if none
	 * @see #getTargetClass()
	 * @see #getTargetMethod()
	 * @see #getArguments()
	 */
	protected Method findMatchingMethod() {
		String targetMethod = getTargetMethod();
		Object[] arguments = getArguments();
		int argCount = arguments.length;

		Method[] candidates = getTargetClass().getMethods();
		Method matchingMethod = null;
		int numberOfMatchingMethods = 0;

		for (int i = 0; i < candidates.length; i++) {
			Method candidate = candidates[i];
			Class[] paramTypes = candidate.getParameterTypes();
			int paramCount = paramTypes.length;
			if (candidate.getName().equals(targetMethod) && paramCount == argCount) {
				boolean match = true;
				for (int j = 0; j < paramCount && match; j++) {
					match = match && ClassUtils.isAssignableValue(paramTypes[j], arguments[j]);
				}
				if (match) {
					matchingMethod = candidate;
					numberOfMatchingMethods++;
				}
			}
		}

		// Only return matching method if exactly one found.
		if (numberOfMatchingMethods == 1) {
			return matchingMethod;
		}
		else {
			return null;
		}
	}

	/**
	 * Return the prepared Method object that will be invoker.
	 * Can for example be used to determine the return type.
	 * @see #prepare
	 * @see #invoke
	 */
	public Method getPreparedMethod() {
		return this.methodObject;
	}

	/**
	 * Invoke the specified method.
	 * The invoker needs to have been prepared before.
	 * @return the object (possibly null) returned by the method invocation,
	 * or <code>null</code> if the method has a void return type
	 * @see #prepare
	 */
	public Object invoke() throws InvocationTargetException, IllegalAccessException {
		if (this.methodObject == null) {
			throw new IllegalStateException("prepare() must be called prior to invoke() on MethodInvoker");
		}
		// In the static case, target will just be <code>null</code>.
		return this.methodObject.invoke(this.targetObject, this.arguments);
	}

}
