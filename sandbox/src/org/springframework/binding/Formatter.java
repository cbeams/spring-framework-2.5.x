/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

public interface Formatter {
	public String formatValue(Object value) throws IllegalArgumentException;

	public Object parseValue(String formattedValue) throws InvalidFormatException;
}
