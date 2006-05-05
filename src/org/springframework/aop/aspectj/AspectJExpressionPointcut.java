/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.aspectj;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.AspectException;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AbstractExpressionPointcut;
import org.springframework.util.StringUtils;

/**
 * Spring pointcut that uses the AspectJ weaver.
 *
 * <p>The pointcut expression value is an AspectJ string. This can reference
 * other pointcuts and use composition and other operations.
 *
 * <p>Naturally, as this is to be processed by Spring AOP's
 * proxy-based model, only method execution pointcuts are supported.
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 * @since 2.0
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut implements ClassFilter, MethodMatcher {

	private static final Set DEFAULT_SUPPORTED_PRIMITIVES = new HashSet();

	static {
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
		DEFAULT_SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
	}


	private final Map shadowMapCache = new HashMap();

	private PointcutParser pointcutParser;

	private Class pointcutDeclarationScope;

	private String[] pointcutParameterNames = new String[0];

	private Class[] pointcutParameterTypes = new Class[0];

	private PointcutExpression pointcutExpression;


	public AspectJExpressionPointcut() {
		this.pointcutParser =
				PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(
						getSupportedPrimitives());
	}

	public AspectJExpressionPointcut(Class scope, String[] paramNames, Class[] paramTypes) {
		this();
		this.pointcutDeclarationScope = scope;
		if (paramNames.length != paramTypes.length) {
			throw new IllegalStateException(
					"Number of pointcut parameter names must match number of pointcut parameter types");
		}
		this.pointcutParameterNames = paramNames;
		this.pointcutParameterTypes = paramTypes;
	}


	public ClassFilter getClassFilter() {
		checkReadyToMatch();
		return this;
	}

	public MethodMatcher getMethodMatcher() {
		checkReadyToMatch();
		return this;
	}

	public void setParameterNames(String[] names) {
		this.pointcutParameterNames = names;
	}
	
	public void setParameterTypes(Class[] types) {
		this.pointcutParameterTypes = types;
	}
	
	public void onSetExpression(String expression) {
		// doing this now is too early - we may need to discover and set pointcut
		// parameters from the associated advice
		// we now do it lazily in checkReadyToMatch instead...

//		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
//		for (int i = 0; i < pointcutParameters.length; i++) {
//			pointcutParameters[i] = this.pointcutParser.createPointcutParameter(
//					this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
//		}
//		this.pointcutExpression =
//				this.pointcutParser.parsePointcutExpression(
//						replaceBooleanOperators(expression), pointcutDeclarationScope, pointcutParameters);
	}
	
	/**
	 * If a pointcut expression has been specified in xml, the user can't 
	 * write and as "&&" (though &amp;&amp; will work). We also allow
	 * ' and ' between two pointcut sub-expressions. This method converts
	 * and back to && for the AspectJ pointcut parser.
	 */
	private String replaceBooleanOperators(String pcExpr) {
		pcExpr = StringUtils.replace(pcExpr," and "," && ");
		pcExpr = StringUtils.replace(pcExpr, " or ", " || ");
		pcExpr = StringUtils.replace(pcExpr, " not ", " ! ");
		return pcExpr;
	}

	public PointcutExpression getPointcutExpression() {
		checkReadyToMatch();
		return this.pointcutExpression;
	}

	protected Set getSupportedPrimitives() {
		return DEFAULT_SUPPORTED_PRIMITIVES;
	}

	private void checkReadyToMatch() {
		if (getExpression() == null) {
			throw new IllegalStateException("Must set property [expression] before attempting to match.");
		}
		if (this.pointcutExpression == null) {
			// we need to build it now...
			PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
			for (int i = 0; i < pointcutParameters.length; i++) {
				pointcutParameters[i] = this.pointcutParser.createPointcutParameter(
						this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
			}
			this.pointcutExpression =
				this.pointcutParser.parsePointcutExpression(
						replaceBooleanOperators(getExpression()), pointcutDeclarationScope, pointcutParameters);
		}
	}


	public boolean matches(Class targetClass) {
		checkReadyToMatch();
		return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
	}

	public boolean isRuntime() {
		checkReadyToMatch();
		return this.pointcutExpression.mayNeedDynamicTest();
	}

	public boolean matches(Method method, Class targetClass) {
		checkReadyToMatch();
		ShadowMatch shadowMatch = getShadowMatch(method);
		return shadowMatch.maybeMatches();
	}

	public boolean matches(Method method, Class targetClass, Object[] args) {
		checkReadyToMatch();
		ShadowMatch shadowMatch = getShadowMatch(method);

		// Bind Spring AOP proxy to AspectJ "this" and Spring AOP target to AspectJ target,
		// consistent with return of MethodInvocationProceedingJoinPoint
		ReflectiveMethodInvocation invocation;
		Object targetObject;
		Object thisObject;
		try {
			invocation = (ReflectiveMethodInvocation) ExposeInvocationInterceptor.currentInvocation();
			targetObject = invocation.getThis();
			thisObject = invocation.getProxy();
		}
		catch (AspectException ex) {
			// No current invocation
			// TODO do we want to allow this?
			targetObject = null;
			thisObject = null;
			invocation = null;
		}

		JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(thisObject, targetObject, args);
		if (joinPointMatch.matches() && invocation != null) {
			bindParameters(invocation, joinPointMatch);
		}
		return joinPointMatch.matches();
	}

	private void bindParameters(ReflectiveMethodInvocation invocation, JoinPointMatch jpm) {
		Map userAttributes = invocation.getUserAttributes();
		// note - can't use JoinPointMatch.getClass().getName() as the key, since
		// Spring AOP does all the matching at a join point, and then all the invocations
		// under this scenario, if we just use JoinPointMatch as the key, then
		// 'last man wins' which is not what we want at all.
		// Using the expression is guaranteed to be safe, since 2 identical expressions
		// are guaranteed to bind in exactly the same way.
		userAttributes.put(getExpression(),jpm);
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

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("AspectJExpressionPointcut: ");
		if (this.pointcutParameterNames != null && this.pointcutParameterTypes != null) {
			sb.append("(");
			for (int i = 0; i < this.pointcutParameterTypes.length; i++) {
				sb.append(this.pointcutParameterTypes[i].getName());
				sb.append(" ");
				sb.append(this.pointcutParameterNames[i]);
				if ((i+1) < this.pointcutParameterTypes.length) {
					sb.append(", ");
				}
			}
			sb.append(")");
		}
		sb.append(" ");
		if (getExpression() != null) {
			sb.append(getExpression());
		} 
		else {
			sb.append("<pointcut expression not set>");
		}
		return sb.toString();
	}
}
