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
package org.springframework.binding.format.support;

import java.text.ParseException;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.InvalidFormatException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Base template class for all formatters (also implements type converter for
 * those who need general type conversion.)
 * @author Keith Donald
 */
public abstract class AbstractFormatter implements Formatter {

	/**
	 * The type of value to be formatted.
	 */
	private Class valueClass;

	/**
	 * Does this formatter allow empty values?
	 */
	private boolean allowEmpty;

	/**
	 * Constructs a formatted for the specified target <code>valueClass</code>
	 * @param valueClass the value class
	 */
	protected AbstractFormatter(Class valueClass) {
		this(valueClass, false);
	}

	/**
	 * Constructs a formatted for the specified target <code>valueClass</code>
	 * @param valueClass the value class
	 * @param allowEmpty allow formatting of empty values
	 */
	protected AbstractFormatter(Class valueClass, boolean allowEmpty) {
		Assert.notNull(valueClass, "The class of value to format is requred");
		this.valueClass = valueClass;
		this.allowEmpty = allowEmpty;
	}

	public Class getValueClass() {
		return valueClass;
	}

	public final String formatValue(Object value) {
		if (allowEmpty && isEmpty(value)) {
			return getEmptyFormattedValue();
		}
		Assert.isTrue(!isEmpty(value), "Object to format cannot be empty");
		return doFormatValue(value);
	}

	/**
	 * Template method subclasses should override to encapsulate formatting
	 * logic.
	 * @param value the value to format
	 * @return the formatted string representation
	 */
	protected abstract String doFormatValue(Object value);

	protected String getEmptyFormattedValue() {
		return "";
	}

	/**
	 * Template method subclasses should override to encapsulate parsing logic.
	 * @param value the value to parse
	 * @return the parsed value
	 * @throws InvalidFormatException an exception occured parsing
	 */
	public final Object parseValue(String formattedString) throws InvalidFormatException {
		try {
			if (allowEmpty && isEmpty(formattedString)) {
				return getEmptyValue();
			}
			return doParseValue(formattedString);
		}
		catch (ParseException ex) {
			throw new InvalidFormatException(formattedString, ex);
		}
	}

	protected Object getEmptyValue() {
		return null;
	}
	protected abstract Object doParseValue(String formattedString) throws InvalidFormatException, ParseException;

	protected boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		else if (o instanceof String) {
			return !StringUtils.hasText((String)o);
		}
		else {
			return false;
		}
	}

	public boolean isAllowEmpty() {
		return allowEmpty;
	}
}