/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.format.FormatterLocator;

public abstract class AbstractFormattingConverter extends AbstractConverter {
	private FormatterLocator formatterLocator;

	protected AbstractFormattingConverter() {

	}

	protected AbstractFormattingConverter(FormatterLocator formatterLocator) {
		setFormatterLocator(formatterLocator);
	}

	protected FormatterLocator getFormatterLocator() {
		return this.formatterLocator;
	}

	public void setFormatterLocator(FormatterLocator formatterSource) {
		this.formatterLocator = formatterSource;
	}
}
