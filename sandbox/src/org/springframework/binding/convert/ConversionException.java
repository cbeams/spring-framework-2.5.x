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
package org.springframework.binding.convert;

/**
 * Base class for exceptions thrown by the type conversion system.
 * @author Keith Donald
 */
public class ConversionException extends RuntimeException {
	private Object value;

	private Class convertToClass;

	private Class convertFromClass;

	public ConversionException(Object value, Class convertToClass) {
		super("Unable to convert value '" + value + "' of type '" + value.getClass().getName() + "' to class '"
				+ convertToClass.getName() + "'");
		this.value = value;
		this.convertToClass = convertToClass;
	}

	public ConversionException(Object value, Class convertToClass, Throwable cause) {
		super("Unable to convert value '" + value + "' of type '" + value.getClass().getName() + "' to class '"
				+ convertToClass.getName() + "'", cause);
		this.value = value;
		this.convertToClass = convertToClass;
	}

	public ConversionException(Object value, Class convertToClass, Throwable cause, String message) {
		this(value, convertToClass, null, cause, message);
	}

	public ConversionException(Object value, Class convertToClass, Class convertFromClass) {
		this(value, convertToClass, convertFromClass, null, null);
	}

	public ConversionException(Object value, Class convertToClass, Class convertFromClass, Throwable cause,
			String message) {
		super(message, cause);
		this.value = value;
		this.convertFromClass = convertFromClass;
		this.convertToClass = convertToClass;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * @return Returns the convertFromClass.
	 */
	public Class getConvertFromClass() {
		if (convertFromClass == null && value != null) {
			return value.getClass();
		}
		return convertFromClass;
	}

	/**
	 * @return Returns the convertFromClass.
	 */
	public Class getConvertToClass() {
		return convertFromClass;
	}
}