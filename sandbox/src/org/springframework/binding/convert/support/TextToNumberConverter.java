/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

public class TextToNumberConverter extends AbstractFormattingConverter {
	private Class numberClass;

	public TextToNumberConverter(Class numberClass) {
		this.numberClass = numberClass;
	}

	protected Object doConvert(Object o) throws Exception {
		return getFormatterSource().getNumberFormatter(numberClass).parseValue((String)o);
	}
}
