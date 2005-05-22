package org.springframework.binding.expression;

import java.util.Map;

public interface ExpressionEvaluator {
	public Object evaluate(Object object, Map context) throws EvaluationException;
}
