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

package org.springframework.aop.support;

/**
 * Abstract superclass for expression pointcuts
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractExpressionPointcut implements ExpressionPointcut {

	private String expression;
	
	private String location;

	/**
	 * Return location information about the pointcut expression
	 * if available. This is useful in debugging.
	 * @return location information as a human-readable String,
	 * or null if none is available
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Set the location for debugging
	 * @param location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
		try {
			onSetExpression(expression);
		}
		catch (IllegalArgumentException ex) {
			// Fill in location information if possible
			if (location != null) {
				String msg = ex.getMessage();
				msg += "; Location is '" + location + "'";
				throw new IllegalArgumentException(msg, ex.getCause());
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Set the pointcut expression. The expression should be parsed
	 * at this point if possible.
	 * @param expression expression to set
	 * @throws IllegalArgumentException if the expression is invalid
	 */
	protected abstract void onSetExpression(String expression) throws IllegalArgumentException;
}
