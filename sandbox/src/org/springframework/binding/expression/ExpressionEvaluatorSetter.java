package org.springframework.binding.expression;

import java.util.Map;

/**
 * An evaluator that is capable of setting a value on a target object at 
 * the value path defined by the evaluated expression. 
 * @author Keith Donald
 */
public interface ExpressionEvaluatorSetter extends ExpressionEvaluator {
	
	/**
	 * Set the value of the target object in the specified context at the path
	 * defined by the encapsulated expression.
	 * @param target the target object
	 * @param value the value
	 * @param context the expression setter context
	 */
	public void set(Object target, Object value, Map context) throws EvaluationException;
}
