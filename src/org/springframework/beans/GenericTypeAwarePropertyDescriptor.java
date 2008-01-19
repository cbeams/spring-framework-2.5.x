/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.core.GenericTypeResolver;

/**
 * @author Juergen Hoeller
 * @since 2.5.2
 */
class GenericTypeAwarePropertyDescriptor extends PropertyDescriptor {

	private final Class beanClass;

	private Class resolvedPropertyType;

	private MethodParameter writeMethodParameter;


	public GenericTypeAwarePropertyDescriptor(
			Class beanClass, String propertyName, Method readMethod, Method writeMethod)
			throws IntrospectionException {
		super(propertyName, readMethod, writeMethod);
		this.beanClass = beanClass;
	}


	public synchronized Class getPropertyType() {
		if (this.resolvedPropertyType == null) {
			Class propertyType = super.getPropertyType();
			Method readMethod = getReadMethod();
			if (readMethod != null) {
				this.resolvedPropertyType = GenericTypeResolver.resolveReturnType(readMethod, this.beanClass);
			}
			else {
				MethodParameter writeMethodParam = getWriteMethodParameter();
				if (writeMethodParam != null) {
					this.resolvedPropertyType = writeMethodParam.getParameterType();
				}
				else {
					this.resolvedPropertyType = propertyType;
				}
			}
		}
		return this.resolvedPropertyType;
	}

	public synchronized MethodParameter getWriteMethodParameter() {
		Method writeMethod = getWriteMethod();
		if (writeMethod == null) {
			return null;
		}
		if (this.writeMethodParameter == null) {
			this.writeMethodParameter = new MethodParameter(writeMethod, 0);
			GenericTypeResolver.resolveParameterType(this.writeMethodParameter, this.beanClass);
		}
		return this.writeMethodParameter;
	}

}
