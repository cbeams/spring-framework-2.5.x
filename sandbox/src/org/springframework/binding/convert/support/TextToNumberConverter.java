/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

public class TextToNumberConverter extends AbstractFormattingConverter {
	private Class numberClass;

	public TextToNumberConverter() {
		this.numberClass = Integer.class;
	}

	public TextToNumberConverter(Class numberClass) {
		this.numberClass = numberClass;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { numberClass };
	}

	protected Object doConvert(Object o) throws Exception {
		return getFormatterSource().getNumberFormatter(numberClass).parseValue((String)o);
	}
}
