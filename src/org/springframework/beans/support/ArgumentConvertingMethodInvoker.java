/*
 * Copyright 2002-2004 the original author or authors.
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

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.MethodInvoker;

/**
 * Subclass of MethodInvoker that tries to convert the given arguments
 * for the actual target method via BeanWrapperImpl.
 * @author Juergen Hoeller
 * @since 09.06.2004
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

	public void prepare() throws ClassNotFoundException, NoSuchMethodException {
		super.prepare();

		// try to convert the arguments for the chosen method
		Class[] requiredTypes = getPreparedMethod().getParameterTypes();
		Object[] arguments = getArguments();
		Object[] convertedArguments = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			convertedArguments[i] = this.beanWrapper.doTypeConversionIfNecessary(arguments[i], requiredTypes[i]);
		}
		setArguments(convertedArguments);
	}

}
