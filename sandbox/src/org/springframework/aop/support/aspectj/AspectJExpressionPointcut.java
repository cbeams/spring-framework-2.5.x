
package org.springframework.aop.support.aspectj;

import org.aspectj.weaver.tools.*;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.AbstractExpressionPointcut;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author robh
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut {

	private ClassFilter classFilter;

	private MethodMatcher methodMatcher;

	private PointcutParser pointcutParser;

	private PointcutExpression pointcutExpression;

	private static final Set DEFAULT_SUPPORTED_PRIMITIVES = new HashSet();

	static {
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
	}

	public AspectJExpressionPointcut() {
		pointcutParser = new PointcutParser(getSupportedPrimitives());
	}
	
	public AspectJExpressionPointcut(PointcutParser pointcutParser) {
		this.pointcutParser = pointcutParser;
	}

	public ClassFilter getClassFilter() {
		checkReadyToMatch();
		return this.classFilter;
	}

	public MethodMatcher getMethodMatcher() {
		checkReadyToMatch();
		return this.methodMatcher;
	}

	public void onSetExpression(String expression) {
		this.pointcutExpression = this.pointcutParser.parsePointcutExpression(expression);


		this.classFilter = new AspectJExpressionClassFilter(this.pointcutExpression, expression);
		this.methodMatcher = new AspectJExpressionMethodMatcher(this.pointcutExpression, expression);
	}
	
	public PointcutExpression getPointcutExpression() {
		return this.pointcutExpression;
	}

	protected Set getSupportedPrimitives() {
		return DEFAULT_SUPPORTED_PRIMITIVES;
	}

	private void checkReadyToMatch() {
		if (this.pointcutExpression == null) {
			throw new IllegalStateException("Must set property [expression] before attempting to match.");
		}
	}

	private static class AspectJExpressionClassFilter implements ClassFilter {

		private PointcutExpression pointcutExpression;

		private String expression;

		public AspectJExpressionClassFilter(PointcutExpression pointcutExpression, String expression) {
			this.pointcutExpression = pointcutExpression;
			this.expression = expression;
		}

		public boolean matches(Class targetClass) {
			return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
		}
	}

	private static class AspectJExpressionMethodMatcher implements MethodMatcher {

		private PointcutExpression pointcutExpression;

		private String expression;

		public AspectJExpressionMethodMatcher(PointcutExpression pointcutExpression, String expression) {
			this.pointcutExpression = pointcutExpression;
			this.expression = expression;
		}

		public boolean matches(Method method, Class targetClass) {
			FuzzyBoolean result = this.pointcutExpression.matchesMethodExecution(method, targetClass);


			return (result != FuzzyBoolean.NO);
		}

		public boolean isRuntime() {
			return this.pointcutExpression.mayNeedDynamicTest();
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			FuzzyBoolean staticMatch = this.pointcutExpression.matchesMethodExecution(method, targetClass);

			if(staticMatch == FuzzyBoolean.MAYBE) {
               return this.pointcutExpression.matchesDynamically(null, null, args);
			} else {
				return (staticMatch == FuzzyBoolean.YES);
			}
		}
	}
}

