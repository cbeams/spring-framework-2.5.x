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
package org.springframework.web.flow.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParseException;
import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.Assert;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.TransitionCriteriaFactory;

/**
 * Converter that takes an encoded string representation and produces
 * a corresponding <code>TransitionCriteria</code> object. It supports the following
 * encoded forms:
 * <ul>
 * <li>"*" - will result in a TransitionCriteria object that matches on everything
 * ({@link org.springframework.web.flow.TransitionCriteriaFactory#any()})
 * </li>
 * <li>"eventId" - will result in a TransitionCriteria object that matches given
 * event id ({@link org.springframework.web.flow.TransitionCriteriaFactory#eventId(String)})</li>
 * <li>"${...}" - will result in a TransitionCriteria object that evaluates given
 * condition, expressed as an expression</li>
 * </ul>
 * 
 * @see org.springframework.web.flow.TransitionCriteria
 * @see org.springframework.web.flow.TransitionCriteriaFactory
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class TextToTransitionCriteria extends AbstractConverter {

	private ExpressionParser expressionParser = ExpressionParserUtils.getDefaultExpressionParser();
	
	/**
	 * Returns the expression parser used by this converter.
	 */
	public ExpressionParser getExpressionParser() {
		return expressionParser;
	}
	
	/**
	 * Set the expression parser used by this converter.
	 */
	public void setExpressionParser(ExpressionParser expressionParser) {
		this.expressionParser = expressionParser;
	}
	
	public Class[] getSourceClasses() {
		return new Class[] { String.class } ;
	}

	public Class[] getTargetClasses() {
		return new Class[] { TransitionCriteria.class } ;
	}
	
	protected Object doConvert(Object source, Class targetClass) throws ConversionException {
		String encodedCriteria = (String)source;
		if (TransitionCriteriaFactory.WildcardTransitionCriteria.WILDCARD_EVENT_ID.equals(encodedCriteria)) {
			return TransitionCriteriaFactory.any();
		}
		else if (expressionParser.isExpression(encodedCriteria)) {
			return createExpressionTransitionCriteria(encodedCriteria);
		}
		else {
			return TransitionCriteriaFactory.eventId(encodedCriteria);
		}
	}

	/**
	 * Factory method overridable by subclasses to customize expression-based
	 * transition criteria.
	 * @param expression the expression
	 * @return the criteria
	 * @throws ConversionException when there is a problem parsing the expression
	 */
	protected TransitionCriteria createExpressionTransitionCriteria(String expression) throws ConversionException {
		try {
			return new ExpressionTransitionCriteria(expressionParser.parseExpression(expression));
		}
		catch (ParseException e) {
			throw new ConversionException(expression, ExpressionTransitionCriteria.class, e);
		}
	}
	
	/**
	 * Transtition criteria that tests the value of an expression. The
	 * expression is used to express a condition that guards transition
	 * execution in a web flow.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 * @author Rob Harrop
	 */
	public static class ExpressionTransitionCriteria implements TransitionCriteria {

		/**
		 * The expression evaluator to use.
		 */
		private ExpressionEvaluator evaluator;

		/**
		 * Create a new expression based transition criteria object.
		 * @param evaluator the expression evaluator testing the criteria,
		 *        this expression should be a condition that returns a Boolean value
		 */
		public ExpressionTransitionCriteria(ExpressionEvaluator evaluator) {
			this.evaluator = evaluator;
		}

		public boolean test(RequestContext context) {
			Object result = this.evaluator.evaluate(context, getEvaluationContext(context));
			Assert.isInstanceOf(Boolean.class, result);
			return ((Boolean)result).booleanValue();
		}

		/**
		 * Setup a map with a few aliased values to make writing expression based
		 * transition conditions a bit easier.
		 */
		protected Map getEvaluationContext(RequestContext context) {
			Map evalContext = new HashMap();
			// ${#result == lastEvent.id}
			if (context.getLastEvent() != null) {
				evalContext.put("result", context.getLastEvent().getId());
			}
			return evalContext;
		}

		public String toString() {
			return evaluator.toString();
		}
	}	
}