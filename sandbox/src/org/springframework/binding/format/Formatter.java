/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding.format;

/**
 * A lightweight interface for formatting a value and parsing a value from its
 * formatted form.
 * @author Keith Donald
 */
public interface Formatter {

	/**
	 * Return the intrinsic value type formatted by this formatter.
	 * @return The value class
	 */
	public Class getValueClass();

	/**
	 * Format the value.
	 * @param value the value to format
	 * @return The formatted string, fit for display in a UI
	 * @throws IllegalArgumentException The value could not be formatted.
	 */
	public String formatValue(Object value) throws IllegalArgumentException;

	/**
	 * Parse the formatted string representation of a value, restoring the
	 * value.
	 * @param formattedValue The formatted string representation
	 * @return The value
	 * @throws InvalidFormatException The string was in an invalid form
	 */
	public Object parseValue(String formattedValue) throws InvalidFormatException;

}