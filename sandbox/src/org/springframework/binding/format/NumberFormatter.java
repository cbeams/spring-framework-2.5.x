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

import java.text.NumberFormat;

import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

/**
 * Converts from various
 * <code>Number<code> specializations to <code>String</code> and back.
 * @author Keith Donald
 */
public class NumberFormatter extends AbstractFormatter {

	private NumberFormat numberFormat;

	public NumberFormatter(Class numberClass) {
		super(numberClass);
		assertNumberClass(numberClass);
	}

	public NumberFormatter(Class numberClass, NumberFormat numberFormat) {
		super(numberClass);
		assertNumberClass(numberClass);
		this.numberFormat = numberFormat;
	}

	public NumberFormatter(Class numberClass, boolean allowEmpty) {
		super(numberClass, allowEmpty);
		assertNumberClass(numberClass);
	}

	public NumberFormatter(Class numberClass, NumberFormat numberFormat, boolean allowEmpty) {
		super(numberClass, allowEmpty);
		assertNumberClass(numberClass);
		this.numberFormat = numberFormat;
	}

	private void assertNumberClass(Class numberClass) {
		Assert.isTrue(Number.class.isAssignableFrom(numberClass), "Property class must be a subclass of Number");
	}

	protected String doFormatValue(Object number) {
		if (this.numberFormat != null) {
			// use NumberFormat for rendering value
			return this.numberFormat.format((Number)number);
		}
		else {
			// use toString method for rendering value
			return number.toString();
		}
	}

	protected Object doParseValue(String text) throws IllegalArgumentException {
		// use given NumberFormat for parsing text
		if (this.numberFormat != null) {
			return NumberUtils.parseNumber((String)text, getValueClass(), this.numberFormat);
		}
		// use default valueOf methods for parsing text
		else {
			return NumberUtils.parseNumber((String)text, getValueClass());
		}
	}
}