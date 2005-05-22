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
package org.springframework.binding.expression;

/**
 * Base class for exceptions thrown by expression parsing system.
 * @author Keith Donald
 */
public class ParseException extends RuntimeException {
	private String expressionString;

	public ParseException(String expressionString, Throwable cause) {
		super(cause);
		this.expressionString = expressionString;
	}

	public ParseException(String expressionString, Throwable cause, String message) {
		super(message, cause);
		this.expressionString = expressionString;
	}

	public Object getExpressionString() {
		return expressionString;
	}

}