/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.binding.format;

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