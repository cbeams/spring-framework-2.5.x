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

import org.springframework.binding.TypeConversionException;
import org.springframework.binding.TypeConverter;
import org.springframework.util.closure.Closure;

public class TypeConverterFormatterAdapter implements TypeConverter, Closure {
	private Formatter formatter;

	public TypeConverterFormatterAdapter(Formatter formatter) {
		this.formatter = formatter;
	}

	public Object convert(Object o) throws TypeConversionException {
		if (String.class.isInstance(o)) {
			try {
				return formatter.parseValue((String)o);
			}
			catch (InvalidFormatException e) {
				throw new TypeConversionException(o, null, e);
			}
		}
		else {
			return formatter.formatValue(o);
		}
	}

	public Object call(Object o) {
		return convert(o);
	}

}