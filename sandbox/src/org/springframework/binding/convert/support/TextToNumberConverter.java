/*
 * Copyright 2004-2005 the original author or authors.
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