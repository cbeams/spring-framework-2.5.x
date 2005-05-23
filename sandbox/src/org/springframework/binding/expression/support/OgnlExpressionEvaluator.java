package org.springframework.binding.expression.support;

import java.util.Collections;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.ExpressionEvaluatorSetter;
import org.springframework.util.Assert;

/**
 * Evaluates parsed ognl expressions.
 * @author Keith Donald
 */
class OgnlExpressionEvaluator implements ExpressionEvaluatorSetter {
	private Object expression;
	
	public OgnlExpressionEvaluator(Object expression) {
		this.expression = expression;
	}
	
	public Object evaluate(Object target, Map context) throws EvaluationException {
		try {
			Assert.notNull(target, "The target object is required");
			if (context == null) {
				context = Collections.EMPTY_MAP;
			}
			return Ognl.getValue(expression, context, target);
		} catch (OgnlException e) {
			throw new EvaluationException(expression, e);
		}
	}
	
	public void set(Object target, Object value, Map context) {
		try {
			Assert.notNull(target, "The target object is required");
			if (context == null) {
				context = Collections.EMPTY_MAP;
			}
			Ognl.setValue(expression, context, target, value);
		} catch (OgnlException e) {
			throw new EvaluationException(expression, e);
		}
	}

	
	public String toString() {
		return String.valueOf(expression);
	}
}