package org.springframework.binding.expression.support;

import java.util.Map;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.ExpressionEvaluatorSetter;

/**
 * An expression evaluator that uses the spring bean wrapper.
 * @author Keith Donald
 */
public class BeanWrapperEvaluator implements ExpressionEvaluatorSetter {

	/**
	 * The expression.
	 */
	private String expression;
	
	public BeanWrapperEvaluator(String expression) {
		this.expression = expression;
	}
	
	public Object evaluate(Object target, Map context) throws EvaluationException {
		return new BeanWrapperImpl(target).getPropertyValue(expression);
	}

	public void set(Object target, Object value, Map context) throws EvaluationException {
		new BeanWrapperImpl(target).setPropertyValue(expression, value);
	}
}