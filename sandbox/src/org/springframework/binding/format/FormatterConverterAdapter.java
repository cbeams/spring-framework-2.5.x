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
package org.springframework.binding.format;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.Converter;
import org.springframework.util.closure.Closure;

public class FormatterConverterAdapter implements Converter, Closure {
	private Formatter formatter;

	public FormatterConverterAdapter(Formatter formatter) {
		this.formatter = formatter;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class, formatter.getValueClass() };
	}

	public Class[] getTargetClasses() {
		return new Class[] { String.class, formatter.getValueClass() };
	}

	public Object convert(Object source, Class targetClass) throws ConversionException {
		if (String.class.isInstance(source)) {
			try {
				return formatter.parseValue((String)source);
			}
			catch (InvalidFormatException e) {
				throw new ConversionException(source, null, e);
			}
		}
		else {
			return formatter.formatValue(source);
		}
	}

	public Object call(Object source) {
		return convert(source, null);
	}
}