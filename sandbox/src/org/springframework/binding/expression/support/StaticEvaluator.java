package org.springframework.binding.expression.support;

import java.util.Map;

import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.ExpressionEvaluator;

public class StaticEvaluator implements ExpressionEvaluator {
	private Object value;

	public StaticEvaluator(Object value) {
		this.value = value;
	}

	public Object evaluate(Object object, Map context) throws EvaluationException {
		return value;
	}
}
