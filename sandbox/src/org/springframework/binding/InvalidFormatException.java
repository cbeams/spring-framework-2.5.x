/*
 * Copyright 2004-2005 the original author or authors.
 */
package org.springframework.binding;

/**
 * Thrown when a formatted value is of the wrong form.
 * @author Keith Donald
 */
public class InvalidFormatException extends Exception {

	private String invalidValue;

	private String expectedFormat;

	public InvalidFormatException(String invalidValue) {
		this.invalidValue = invalidValue;
	}

	public InvalidFormatException(String invalidValue, Throwable cause) {
		super(cause);
		this.invalidValue = invalidValue;
	}

	public InvalidFormatException(String invalidValue, String expectedFormat) {
		this.invalidValue = invalidValue;
		this.expectedFormat = expectedFormat;
	}

	public InvalidFormatException(String invalidValue, String expectedFormat, String message) {
		super(message);
		this.invalidValue = invalidValue;
		this.expectedFormat = expectedFormat;
	}

	public InvalidFormatException(String invalidValue, String expectedFormat, Throwable cause) {
		super(cause);
		this.invalidValue = invalidValue;
		this.expectedFormat = expectedFormat;
	}

	public InvalidFormatException(String invalidValue, String expectedFormat, String message, Throwable cause) {
		super(message, cause);
		this.invalidValue = invalidValue;
		this.expectedFormat = expectedFormat;
	}

}