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
package org.springframework.binding;

/**
 * A type converter converts objects of one type to that of another and back
 * again. For example, a "DateToString" type converter would convert Date
 * objects to String (convert to), as well as properly-formatted Strings back to
 * Dates (convert from).
 * @author Keith
 */
public interface TypeConverter {

	/**
	 * Convert the provided object argument to another type.
	 * <p>
	 * A typical type converter implementation is capable of converting from one
	 * type to another and back.  For example:
	 * <pre>
	 *     TypeConverter converter = new NumberToStringTypeConverter(Long.class);
	 *     Long number = (Long)converter.convert("12356");
	 *     String numberString = (String)converter.convert(number);
	 * </pre>
	 * @throws TypeConversionException An exception occured during the
	 *         conversion.
	 */
	public Object convert(Object o) throws TypeConversionException;
}