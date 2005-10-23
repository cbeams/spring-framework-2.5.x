package org.springframework.aop.support.aspectj;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.AbstractExpressionPointcut;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author robh
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut implements ClassFilter, MethodMatcher {

	private static final Set DEFAULT_SUPPORTED_PRIMITIVES = new HashSet();

	private final Map shadowMapCache = new HashMap();

	private PointcutParser pointcutParser;

	private PointcutExpression pointcutExpression;

	static {
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
	}

	public AspectJExpressionPointcut() {
		this.pointcutParser = new PointcutParser(getSupportedPrimitives());
	}

	public ClassFilter getClassFilter() {
		checkReadyToMatch();
		return this;
	}

	public MethodMatcher getMethodMatcher() {
		checkReadyToMatch();
		return this;
	}

	public void onSetExpression(String expression) {
		this.pointcutExpression = this.pointcutParser.parsePointcutExpression(expression);
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


	public boolean matches(Class targetClass) {
		return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
	}

	public boolean isRuntime() {
		return this.pointcutExpression.mayNeedDynamicTest();
	}

	public boolean matches(Method method, Class targetClass) {
		ShadowMatch shadowMatch = this.pointcutExpression.matchesMethodExecution(method);
		return shadowMatch.maybeMatches();
	}

	public boolean matches(Method method, Class targetClass, Object[] args) {
		ShadowMatch shadowMatch = this.pointcutExpression.matchesMethodExecution(method);

		if (shadowMatch.alwaysMatches()) {
			return true;
		}
		else {
			Object target = null; // soon to be something other than null
			JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(target, target, args);
			return joinPointMatch.matches();
		}
	}

	private ShadowMatch getShadowMatch(Method method) {
		synchronized (shadowMapCache) {
			ShadowMatch shadowMatch = (ShadowMatch) shadowMapCache.get(method);

			if (shadowMatch == null) {
				shadowMatch = this.pointcutExpression.matchesMethodExecution(method);
				shadowMapCache.put(method, shadowMatch);
			}
			return shadowMatch;
		}
	}
}

