package org.springframework.binding.expression;

import org.springframework.binding.expression.support.ExpressionParserUtils;
import org.springframework.util.Assert;

/**
 * A static factory for producing configured expression evaluators.
 * @author Keith
 */
public class ExpressionEvaluatorFactory {
	
	/**
	 * Return the evaluator for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator
	 */
	public static ExpressionEvaluator evaluatorFor(String expressionString) {
		return ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
	}
	
	/**
	 * Retrun the evaluator/setter for the specified expression string.
	 * @param expressionString the expression string
	 * @return the evaluator setter
	 */
	public static ExpressionEvaluatorSetter setterFor(String expressionString) {
		ExpressionEvaluator evaluator = ExpressionParserUtils.getDefaultExpressionParser().parseExpression(expressionString);
		Assert.isInstanceOf(ExpressionEvaluatorSetter.class, evaluator, "The expression evaluator is not a setter");
		return (ExpressionEvaluatorSetter)evaluator;
	}
}