package org.springframework.web.flow.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParseException;
import org.springframework.binding.expression.support.OgnlExpressionParser;
import org.springframework.util.Assert;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.TransitionCriteriaFactory;

public class TextToTransitionCriteria extends AbstractConverter {

	private ExpressionParser expressionParser = new OgnlExpressionParser();
	
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
		} else {
			return TransitionCriteriaFactory.eventId(encodedCriteria);
		}
	}

	/**
	 * Factory method overridable by subclasses to customize expression-based transition criteria.
	 * @param expression the expression
	 * @return the criteria
	 * @throws ConversionException
	 */
	protected TransitionCriteria createExpressionTransitionCriteria(String expression) throws ConversionException {
		try {
			return new ExpressionTransitionCriteria(expressionParser.parseExpression(expression));
		} catch (ParseException e) {
			throw new ConversionException(expression, ExpressionTransitionCriteria.class, e);
		}
	}
	
	/**
	 * Transtition criteria that tests the value of a OGNL expression.
	 * <a href="http://www.ognl.org">OGNL</a> is the Object Graph Navigation
	 * Language: an expression language for getting and setting the properties
	 * of Java objects. In this case, it is used to express a condition that
	 * guards transition execution in a web flow.
	 * 
	 * @author Keith Donald
	 * @author Erwin Vervaet
	 * @author Rob Harrop
	 */
	public static class ExpressionTransitionCriteria implements TransitionCriteria {

		/**
		 * Stores the pre-parsed OGNL abstract syntax tree.
		 */
		private ExpressionEvaluator evaluator;

		/**
		 * Create a new OGNL based transition criteria object.
		 * @param expressionString the OGNL expression testing the criteria,
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
		 * Setup a map with a few aliased values to make writing OGNL based
		 * transition conditions a bit easier.
		 */
		protected Map getEvaluationContext(RequestContext context) {
			Map evalContext = new HashMap();
			// ${#result == lastEvent.id}
			if (context.getLastEvent()!=null) {
				evalContext.put("result", context.getLastEvent().getId());
			}
			return evalContext;
		}

		public String toString() {
			return evaluator.toString();
		}
	}	
}