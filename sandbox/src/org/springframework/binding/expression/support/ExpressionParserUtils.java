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
package org.springframework.binding.expression.support;

import org.springframework.binding.expression.ExpressionParser;

/**
 * Static utilities dealing with <code>ExpressionParser</code>s.
 * 
 * @see org.springframework.binding.expression.ExpressionParser
 * 
 * @author Erwin Vervaet
 * @author Keith Donald
 */
public class ExpressionParserUtils {

	/**
	 * Utility method that checks which expression parsers are available
	 * on the classpath and returns the appropriate default one.
	 */
	public static ExpressionParser getDefaultExpressionParser() {
		try {
			Class.forName("ognl.Ognl");
			// OGNL available
			return new OgnlExpressionParser();
		}
		catch (ClassNotFoundException e) {
			// just use spring's own bean wrapper
			return new BeanWrapperExpressionParser();
		}
	}	
}