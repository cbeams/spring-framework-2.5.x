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

package org.springframework.beans;

import java.beans.PropertyChangeEvent;

/**
 * Exception thrown on a type mismatch when trying to set a bean property.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class TypeMismatchException extends PropertyAccessException {

	/**
	 * Error code that a type mismatch error will be registered with.
	 */
	public static final String ERROR_CODE = "typeMismatch";

	private final Class requiredType;

	/**
	 * Create a new TypeMismatchException.
	 * @param propertyChangeEvent the PropertyChangeEvent that resulted in the problem
	 * @param requiredType the required target type
	 */
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType) {
		this(propertyChangeEvent, requiredType, null);
	}

	/**
	 * Create a new TypeMismatchException.
	 * @param propertyChangeEvent the PropertyChangeEvent that resulted in the problem
	 * @param requiredType the required target type (or null if not known)
	 * @param ex the root cause
	 */
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType, Throwable ex) {
		super(propertyChangeEvent,
				"Failed to convert property value of type [" +
				(propertyChangeEvent.getNewValue() != null ?
				propertyChangeEvent.getNewValue().getClass().getName() : null) + "]" +
				(requiredType != null ? " to required type [" + requiredType.getName() + "]" : "")+
				(propertyChangeEvent.getPropertyName() != null ?
				" for property '" + propertyChangeEvent.getPropertyName() + "'" : ""),
				ex);
		this.requiredType = requiredType;
	}

	/**
	 * Return the required target type.
	 */
	public Class getRequiredType() {
		return requiredType;
	}

	public String getErrorCode() {
		return ERROR_CODE;
	}

}
