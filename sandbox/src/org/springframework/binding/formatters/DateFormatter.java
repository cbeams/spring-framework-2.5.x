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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.springframework.binding.InvalidFormatException;

/**
 * Formatter that formats date objects.
 * @author Keith Donald
 */
public class DateFormatter extends AbstractFormatter {

	private DateFormat dateFormat;

	/**
	 * @param convertFromClass
	 */
	public DateFormatter(DateFormat dateFormat) {
		super(Date.class);
		this.dateFormat = dateFormat;
	}

	/**
	 * @param convertFromClass
	 * @param allowEmpty
	 */
	public DateFormatter(DateFormat dateFormat, boolean allowEmpty) {
		super(Date.class, allowEmpty);
		this.dateFormat = dateFormat;
	}

	// convert from date to string
	public String formatValue(Object date) {
		return dateFormat.format((Date)date);
	}

	// convert back from string to date
	public Object parseValue(String dateString) throws InvalidFormatException {
		try {
			return dateFormat.parse((String)dateString);
		}
		catch (ParseException ex) {
			throw new InvalidFormatException(dateString, dateFormat.toString(), ex);
		}
	}
}