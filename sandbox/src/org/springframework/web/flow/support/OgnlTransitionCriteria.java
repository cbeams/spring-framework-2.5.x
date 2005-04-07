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

package org.springframework.web.flow.support;

import ognl.ExpressionSyntaxException;
import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.util.Assert;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;

/**
 * Transtition criteria that tests the value of a OGNL expression.
 * <a href="http://www.ognl.org">OGNL</a> is the Object Graph
 * Navigation Language: an expression language for getting and setting
 * the properties of Java objects. In this case, it is used to express
 * a condition that guards transition execution in a web flow.
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 * @author Rob Harrop
 */
public class OgnlTransitionCriteria implements TransitionCriteria {

	/**
	 * Stores the pre-parsed OGNL abstract syntax tree.
	 */
	private Object expression;

	/**
	 * Create a new OGNL based transition criteria object.
	 *
	 * @param expressionString the OGNL expression testing the criteria, this
	 * expression should be a condition that returns a Boolean value
	 */
	public OgnlTransitionCriteria(String expressionString) {
		Assert.hasText(expressionString);

		try {
			// is is *possible* to check that the expression can only return
			// a boolean in most cases but it is not foolproof so we don't
			this.expression = Ognl.parseExpression(expressionString);
		}
		catch (ExpressionSyntaxException ex) {
			throw new IllegalArgumentException("The expression [" + expressionString + "] has a syntax error", ex);
		}
		catch (OgnlException ex) {
			throw new IllegalStateException("Unable to evaluate syntactically correct OGNL expression.", ex);
		}
	}

	public boolean test(RequestContext context) {
		try {
			Object result = Ognl.getValue(this.expression, context);
			Assert.isInstanceOf(Boolean.class, result);
			return ((Boolean) result).booleanValue();
		}
		catch (OgnlException e) {
			throw new IllegalArgumentException("Invalid transition expression '" + this.expression + "':" + e);
		}
	}
}
