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
package org.springframework.binding.convert.support;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.binding.format.FormatterLocator;

/**
 * Converts textual representations of numbers to a <code>Number</code>
 * specialization. Delegates to a synchronized formatter to parse text strings.
 * @author Keith Donald
 */
public class TextToNumberConverter extends AbstractFormattingConverter {

	public TextToNumberConverter(FormatterLocator locator) {
		super(locator);
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { Short.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class,
				BigDecimal.class };
	}

	protected Object doConvert(Object o, Class targetClass) throws Exception {
		return getFormatterLocator().getNumberFormatter(targetClass).parseValue((String)o);
	}
}