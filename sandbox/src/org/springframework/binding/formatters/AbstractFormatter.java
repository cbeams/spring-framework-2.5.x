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
package org.springframework.binding.formatters;

import java.util.Collection;
import java.util.Map;

import org.springframework.binding.Formatter;
import org.springframework.binding.InvalidFormatException;
import org.springframework.binding.TypeConversionException;
import org.springframework.binding.TypeConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.closure.Closure;

/**
 * Base template class for all formatters (also implements type converter for
 * those who need general type conversion.)
 * @author Keith Donald
 */
public abstract class AbstractFormatter implements Formatter, TypeConverter, Closure {

	private Class valueClass;

	private boolean allowEmpty;

	protected AbstractFormatter(Class valueClass) {
		this(valueClass, false);
	}

	protected AbstractFormatter(Class valueClass, boolean allowEmpty) {
		Assert.notNull(valueClass, "The class of value to format is requred");
		this.valueClass = valueClass;
		this.allowEmpty = allowEmpty;
	}

	public Class getValueClass() {
		return valueClass;
	}

	public Object convert(Object o) throws TypeConversionException {
		if (allowEmpty && isEmpty(o)) {
			return "";
		}
		Assert.notNull(o, "Object to convert from '" + getValueClass().getName() + "' to '" + String.class
				+ "' (or vice versa) cannot be null");
		if (getValueClass().isInstance(o)) {
			return formatValue(o);
		}
		else if (String.class.isInstance(o)) {
			try {
				return parseValue((String)o);
			}
			catch (InvalidFormatException e) {
				throw new TypeConversionException(o, String.class, e);
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported object " + o
					+ "'; I do not know how to convert objects of this type");
		}
	}

	protected boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		else if (o instanceof String) {
			return StringUtils.hasText((String)o);
		}
		else if (o instanceof Collection) {
			return ((Collection)o).isEmpty();
		}
		else if (o instanceof Map) {
			return ((Map)o).isEmpty();
		}
		else {
			return false;
		}
	}

	public Object call(Object argument) {
		return convert(argument);
	}
}