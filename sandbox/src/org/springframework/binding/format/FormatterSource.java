/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.format;

/**
 * Source for formatters - formatters are typically NOT thread safe, because
 * *Format objects generally aren't thread safe: so implementations of this
 * service that pool formatters should do so in thread local storage.
 * @author Keith Donald
 */
public interface FormatterSource {
	public DateFormatter getDateFormatter(String encodedFormat);
	
	public DateFormatter getDateFormatter();

	public DateFormatter getDateFormatter(Style style);

	public DateFormatter getDateTimeFormatter();

	public DateFormatter getDateTimeFormatter(Style dateStyle, Style timeStyle);

	public DateFormatter getTimeFormatter();

	public DateFormatter getTimeFormatter(Style style);

	public NumberFormatter getNumberFormatter(Class numberClass);

	public NumberFormatter getPercentFormatter();

	public NumberFormatter getCurrencyFormatter();
}