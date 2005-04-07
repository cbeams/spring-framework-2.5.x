
package org.springframework.web.flow.support;

import ognl.ExpressionSyntaxException;
import ognl.Ognl;
import ognl.OgnlException;

import org.springframework.util.Assert;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.TransitionCriteria;
import org.springframework.web.flow.config.SimpleTransitionCriteriaCreator;

/**
 * @author robh
 */
public class OgnlTransitionCriteriaCreator extends SimpleTransitionCriteriaCreator {

	public TransitionCriteria create(String on) {

		if (isExpression(on)) {
			return new OgnlTransitionCriteria(on.substring(2, on.length() - 1));
		}
		else {
			return super.create(on);
		}
	}

	private boolean isExpression(String on) {
		return (on.startsWith("${") && on.endsWith("}"));
	}

	public static class OgnlTransitionCriteria implements TransitionCriteria {

		/**
		 * Stores the pre-parsed OGNL abstract syntax tree.
		 */
		private Object expression;

		/**
		 * Create a new OGNL based transition criteria object.
		 *
		 * @param expressionString the OGNL expression testing the criteria, this
		 * expression should be a condition that returns a Boolean value
		 */
		public OgnlTransitionCriteria(String expressionString) {
			Assert.hasText(expressionString);

			try {
				// is is *possible* to check that the expression can only return
				// a boolean in most cases but it is not foolproof so we don't
				this.expression = Ognl.parseExpression(expressionString);
			}
			catch (ExpressionSyntaxException ex) {
				throw new IllegalArgumentException("The expression [" + expressionString + "] has a syntax error: " + ex);
			}
			catch (OgnlException ex) {
				throw new IllegalStateException("Unable to evaluate syntactically correct OGNL expression.", ex);
			}
		}

		public boolean test(RequestContext context) {
			try {
				Object result = Ognl.getValue(this.expression, context);
				Assert.isInstanceOf(Boolean.class, result);
				return ((Boolean) result).booleanValue();
			}
			catch (OgnlException e) {
				throw new IllegalArgumentException("Invalid transition expression '" + this.expression + "':" + e);
			}
		}
	}
}
