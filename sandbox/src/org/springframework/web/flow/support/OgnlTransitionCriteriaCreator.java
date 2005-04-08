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
import org.springframework.web.flow.config.FlowBuilderException;
import org.springframework.web.flow.config.SimpleTransitionCriteriaCreator;

/**
 * When presented with expressions in the format <code>${expr}</code>, creates instances
 * of <code>OgnlTransitionCriteria</code> that will evaluate <code>expr</code> against
 * the <code>RequestContext</code> using OGNL during <code>test(RequestContext)</code>.
 *
 * If the <code>encodedCriteria</code> value passed to <code>create(String)</code> is not an
 * expression this class delegates to the super class to get an instance of the default
 * <code>TransitionCriteria</code> for the event name.
 * 
 * @see OgnlTransitionCriteriaCreator
 * @see org.springframework.web.flow.config.SimpleTransitionCriteriaCreator
 * 
 * @author Rob Harrop
 */
public class OgnlTransitionCriteriaCreator extends SimpleTransitionCriteriaCreator {

	/**
	 * If the supplied <code>encodedCriteria</code> value is an expression, then an instance
	 * of <code>OgnlTransitionCriteria</code> is created. Otherwise, the super
	 * class is asked to provide the default implementation of <code>TransitionCriteria</code>.
	 * @param encodedCriteria an event name or expression
	 */
	public TransitionCriteria create(String encodedCriteria) {
		if (isExpression(encodedCriteria)) {
			try {
				return new OgnlTransitionCriteria(cutExpression(encodedCriteria));
			}
			catch (ExpressionSyntaxException ex) {
				throw new FlowBuilderException("The expression [" + encodedCriteria + "] has a syntax error.", ex);
			}
			catch (OgnlException ex) {
				throw new FlowBuilderException("Unable to evaluate syntactically correct OGNL expression.", ex);
			}
		}
		else {
			return super.create(encodedCriteria);
		}
	}

	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	private boolean isExpression(String encodedCriteria) {
		return (encodedCriteria.startsWith("${") && encodedCriteria.endsWith("}"));
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 */
	private String cutExpression(String encodedCriteria) {
		return encodedCriteria.substring(2, encodedCriteria.length() - 1);
	}


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
	public static class OgnlTransitionCriteria implements TransitionCriteria {

		/**
		 * Stores the pre-parsed OGNL abstract syntax tree.
		 */
		private Object expression;

		/**
		 * Create a new OGNL based transition criteria object.
		 *
		 * @param expressionString the OGNL expression testing the criteria, this
		 *        expression should be a condition that returns a Boolean value
		 */
		public OgnlTransitionCriteria(String expressionString) throws OgnlException {
			Assert.hasText(expressionString);

			// is is *possible* to check that the expression can only return
			// a boolean in most cases but it is not foolproof so we don't
			this.expression = Ognl.parseExpression(expressionString);
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
}
