/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.binding.format.support;

import java.util.Locale;

import org.springframework.binding.format.Formatter;
import org.springframework.binding.format.FormatterLocator;
import org.springframework.binding.format.LocaleContext;
import org.springframework.binding.format.Style;

/**
 * FormatterLocator that caches Formatters in thread-local storage.
 * @author Keith Donald
 */
public abstract class AbstractFormatterLocator implements FormatterLocator {

	private LocaleContext localeContext = new ThreadLocaleContext();

	private Style defaultDateStyle = Style.MEDIUM;

	private Style defaultTimeStyle = Style.MEDIUM;

	public void setLocaleContext(LocaleContext localeContext) {
		this.localeContext = localeContext;
	}
	
	public void setDefaultDateStyle(Style style) {
		this.defaultDateStyle = style;
	}

	public void setDefaultTimeStyle(Style style) {
		this.defaultTimeStyle = style;
	}

	protected int getDefaultDateStyleCode() {
		return defaultDateStyle.getShortCode();
	}

	protected int getDefaultTimeStyleCode() {
		return defaultTimeStyle.getShortCode();
	}

	protected Locale getLocale() {
		return this.localeContext.getLocale();
	}

	public Formatter getDateFormatter() {
		return getDateFormatter(this.defaultDateStyle);
	}

	public Formatter getDateFormatter(String encodedFormat) {
		throw new UnsupportedOperationException();
	}

	public Formatter getDateTimeFormatter() {
		return getDateTimeFormatter(this.defaultDateStyle, this.defaultTimeStyle);
	}

	public Formatter getTimeFormatter() {
		return getTimeFormatter(this.defaultTimeStyle);
	}

	public Formatter getPercentFormatter() {
		throw new UnsupportedOperationException();
	}

	public Formatter getCurrencyFormatter() {
		throw new UnsupportedOperationException();
	}
}