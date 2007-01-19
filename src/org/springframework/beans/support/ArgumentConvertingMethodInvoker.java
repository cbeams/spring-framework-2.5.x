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

package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.ClassUtils;
import org.springframework.util.MethodInvoker;

/**
 * Subclass of MethodInvoker that tries to convert the given arguments
 * for the actual target method via BeanWrapperImpl.
 *
 * <p>Supports flexible argument conversions, in particular for
 * invoking a specific overloaded method.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.BeanWrapperImpl#convertIfNecessary
 */
public class ArgumentConvertingMethodInvoker extends MethodInvoker {

	private TypeConverter typeConverter;

	private boolean useDefaultConverter = true;


	/**
	 * Set a TypeConverter to use for argument type conversion.
	 * <p>Default is a {@link org.springframework.beans.SimpleTypeConverter}.
	 * Can be overridden with any TypeConverter implementation, typically
	 * a pre-configured SimpleTypeConverter or a BeanWrapperImpl instance.
	 * @see org.springframework.beans.SimpleTypeConverter
	 * @see org.springframework.beans.BeanWrapperImpl
	 */
	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
		this.useDefaultConverter = false;
	}

	/**
	 * Return the TypeConverter used for argument type conversion.
	 * <p>Can be cast to {@link org.springframework.beans.PropertyEditorRegistry}
	 * if direct access to the underlying PropertyEditors is desired
	 * (provided that the present TypeConverter actually implements the
	 * PropertyEditorRegistry interface).
	 */
	public TypeConverter getTypeConverter() {
		if (this.typeConverter == null && this.useDefaultConverter) {
			this.typeConverter = getDefaultTypeConverter();
		}
		return this.typeConverter;
	}

	/**
	 * Obtain the default TypeConverter for this method invoker.
	 * <p>Called if no explicit TypeConverter has been specified.
	 * The default implementation builds a
	 * {@link org.springframework.beans.SimpleTypeConverter}.
	 * Can be overridden in subclasses.
	 */
	protected TypeConverter getDefaultTypeConverter() {
		return new SimpleTypeConverter();
	}

	/**
	 * Register the given custom property editor for all properties of the given type.
	 * <p>Typically used in conjunction with the default
	 * {@link org.springframework.beans.SimpleTypeConverter}; will work with any
	 * TypeConverter that implements the PropertyEditorRegistry interface as well.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 * @see #setTypeConverter
	 * @see org.springframework.beans.PropertyEditorRegistry#registerCustomEditor
	 */
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		TypeConverter converter = getTypeConverter();
		if (!(converter instanceof PropertyEditorRegistry)) {
			throw new IllegalStateException(
					"TypeConverter does not implement PropertyEditorRegistry interface: " + converter);
		}
		((PropertyEditorRegistry) converter).registerCustomEditor(requiredType, propertyEditor);
	}


	/**
	 * This implementation looks for a method with matching parameter types:
	 * that is, where each argument value is assignable to the corresponding parameter type.
	 */
	protected Method findMatchingMethod() {
		Method[] candidates = getTargetClass().getMethods();
		Object[] arguments = getArguments();
		int argCount = arguments.length;

		// First pass: look for method with directly assignable arguments.
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].getName().equals(getTargetMethod())) {
				// Check if the inspected method has the correct number of parameters.
				Class[] paramTypes = candidates[i].getParameterTypes();
				if (paramTypes.length == argCount) {
					int numberOfCorrectArguments = 0;
					for (int j = 0; j < argCount; j++) {
						// Verify that the supplied argument is assignable to the method parameter.
						if (ClassUtils.isAssignableValue(paramTypes[j], arguments[j])) {
							numberOfCorrectArguments++;
						}
					}
					if (numberOfCorrectArguments == argCount) {
						return candidates[i];
					}
				}
			}
		}

		// Second pass: look for method where arguments can be converted to parameter types.
		TypeConverter converter = getTypeConverter();
		if (converter != null) {
			for (int i = 0; i < candidates.length; i++) {
				if (candidates[i].getName().equals(getTargetMethod())) {
					// Check if the inspected method has the correct number of parameters.
					Class[] paramTypes = candidates[i].getParameterTypes();
					if (paramTypes.length == argCount) {
						Object[] argumentsToUse = new Object[argCount];
						int numberOfCorrectArguments = 0;
						for (int j = 0; j < argCount; j++) {
							// Verify that the supplied argument is assignable to the method parameter.
							try {
								argumentsToUse[j] = converter.convertIfNecessary(arguments[j], paramTypes[j]);
								numberOfCorrectArguments++;
							}
							catch (TypeMismatchException ex) {
								// Ignore -> simply doesn't match.
							}
						}
						if (numberOfCorrectArguments == argumentsToUse.length) {
							setArguments(argumentsToUse);
							return candidates[i];
						}
					}
				}
			}
		}

		return null;
	}

}
