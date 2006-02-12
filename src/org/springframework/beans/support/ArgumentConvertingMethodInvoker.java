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

package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.TypeMismatchException;
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
 * @see org.springframework.beans.BeanWrapperImpl#doTypeConversionIfNecessary
 */
public class ArgumentConvertingMethodInvoker extends MethodInvoker {

	private final BeanWrapperImpl beanWrapper = new BeanWrapperImpl();
	

	/**
	 * Register the given custom property editor for all properties
	 * of the given type.
	 * @param requiredType type of the property
	 * @param propertyEditor editor to register
	 * @see org.springframework.beans.BeanWrapper#registerCustomEditor
	 */
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.beanWrapper.registerCustomEditor(requiredType, propertyEditor);
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
						if (BeanUtils.isAssignable(paramTypes[j], arguments[j])) {
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
							argumentsToUse[j] = this.beanWrapper.doTypeConversionIfNecessary(arguments[j], paramTypes[j]);
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

		return null;
	}

}
