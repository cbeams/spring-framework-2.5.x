/*
 * Copyright 2002-2004 the original author or authors.
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
 
package org.springframework.jms.converter;

/**
 * Thrown by JmsConverter when it can not convert an object to/from a JMS Message.
 *
 * @author Mark Pollack
 */
public class ConversionException extends org.springframework.core.NestedRuntimeException {

	/**
	 * Constructor for ConversionException.
	 *
	 * @param s Custom message string
	 * @param ex Original exception
	 */
	public ConversionException(final String s, final Throwable ex) {
		super(s, ex);
	}

	/**
	 * Constructor for ConversionException.
	 *
	 * @param s Custom message string.
	 */
	public ConversionException(final String s) {
		super(s);
	}

}
