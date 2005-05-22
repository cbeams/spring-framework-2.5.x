package org.springframework.binding.expression.support;

import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.ExpressionEvaluator;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.ParseException;
import org.springframework.util.Assert;

public class OgnlExpressionParser implements ExpressionParser {

	private static final String EXPRESSION_PREFIX = "${";

	private static final String EXPRESSION_SUFFIX = "}";
	
	/**
	 * Check whether or not given criteria are expressed as an expression.
	 */
	public boolean isExpression(String encodedCriteria) {
		return (encodedCriteria.startsWith(EXPRESSION_PREFIX) && encodedCriteria.endsWith(EXPRESSION_SUFFIX));
	}

	public ExpressionEvaluator parseExpression(String expressionString) throws ParseException {
		try {
			return new OgnlExpressionEvaluator(Ognl.parseExpression(cutExpression(expressionString)));
		} catch (OgnlException e) {
			throw new ParseException(expressionString, e);
		}
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 */
	private String cutExpression(String encodedCriteria) {
		return encodedCriteria.substring(
				EXPRESSION_PREFIX.length(),
				encodedCriteria.length() - EXPRESSION_SUFFIX.length());
	}
	
	private static class OgnlExpressionEvaluator implements ExpressionEvaluator {
		private Object expression;
		
		public OgnlExpressionEvaluator(Object expression) {
			this.expression = expression;
		}
		
		public Object evaluate(Object object, Map context) throws EvaluationException {
			try {
				Assert.notNull(object, "The object is required");
				return Ognl.getValue(expression, context, object);
			} catch (OgnlException e) {
				throw new EvaluationException(expression, e);
			}
		}
		
		public String toString() {
			return String.valueOf(expression);
		}
	}
}