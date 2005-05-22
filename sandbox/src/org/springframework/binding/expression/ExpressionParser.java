package org.springframework.binding.expression;

public interface ExpressionParser {
	public boolean isExpression(String expressionString);
	
	public ExpressionEvaluator parseExpression(String expressionString) throws ParseException;
}
