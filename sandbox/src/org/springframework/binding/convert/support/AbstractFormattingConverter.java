/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.convert.support;

import org.springframework.binding.format.FormatterLocator;

public abstract class AbstractFormattingConverter extends AbstractConverter {
	private FormatterLocator formatterSource;

	protected FormatterLocator getFormatterSource() {
		return this.formatterSource;
	}

	public void setFormatterSource(FormatterLocator formatterSource) {
		this.formatterSource = formatterSource;
	}
}
