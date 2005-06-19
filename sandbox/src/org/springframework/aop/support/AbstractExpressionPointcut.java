
package org.springframework.aop.support;

/**
 * @author robh
 */
public abstract class AbstractExpressionPointcut implements ExpressionPointcut {

	private String expression;

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
		onSetExpression(expression);
	}

	protected abstract void onSetExpression(String expression);
}
