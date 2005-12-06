/*
 * Copyright 2002-2004 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Spring pointcut that uses AspectJ weaver.
 * The pointcut expression value is an AspectJ string. This
 * can reference other pointcuts and use composition and other
 * operations.
 * <p/>
 * Naturally, as this is to be processed by Spring AOP's
 * proxy-based model, only method execution pointcuts are
 * supported.
 *
 * @author Rob Harrop
 * @author Adrian Colyer
 * @author Rod Johnson
 * @since 1.3
 */
public class AspectJExpressionPointcut extends AbstractExpressionPointcut implements ClassFilter, MethodMatcher {

	private static final Set DEFAULT_SUPPORTED_PRIMITIVES = new HashSet();

	private final Map shadowMapCache = new HashMap();

	private PointcutParser pointcutParser;

	private Class pointcutDeclarationScope;

	private String[] pointcutParameterNames = new String[0];

	private Class[] pointcutParameterTypes = new Class[0];

	private PointcutExpression pointcutExpression;

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
	}

	public AspectJExpressionPointcut() {
		this.pointcutParser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(getSupportedPrimitives());
	}

	public AspectJExpressionPointcut(Class scope, String[] paramNames, Class[] paramTypes) {
		this();
		this.pointcutDeclarationScope = scope;
		if (paramNames.length != paramTypes.length) {
			throw new IllegalStateException("Number of pointcut parameter names must match number of pointcut parameter types");
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

	public void onSetExpression(String expression) {
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = this.pointcutParser.createPointcutParameter(this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
		}
		this.pointcutExpression =
				this.pointcutParser.parsePointcutExpression(expression, pointcutDeclarationScope, pointcutParameters);
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
		checkReadyToMatch();
		return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
	}

	public boolean isRuntime() {
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

		Object target;
		ReflectiveMethodInvocation invocation;
		try {
			invocation = (ReflectiveMethodInvocation) ExposeInvocationInterceptor.currentInvocation();
			target = invocation.getThis();
		}
		catch (AspectException ex) {
			// No current invocation
			// TODO do we want to allow this?
			target = null;
			invocation = null;
		}

		JoinPointMatch joinPointMatch = shadowMatch.matchesJoinPoint(target, target, args);
		if (joinPointMatch.matches() && invocation != null) {
			bindParameters(invocation, joinPointMatch.getParameterBindings());
		}
		return joinPointMatch.matches();
	}

	public JoinPointMatch matchesWithBinding(Method method, Object targetObject, Object[] args) {
		ShadowMatch shadowMatch = this.pointcutExpression.matchesMethodExecution(method);
		return (shadowMatch.matchesJoinPoint(targetObject, targetObject, args));
	}

	private void bindParameters(ReflectiveMethodInvocation invocation, PointcutParameter[] parameters) {
		Map bindingsMap = invocation.getUserAttributes();
		for (int i = 0; i < parameters.length; i++) {
			PointcutParameter p = parameters[i];
			bindingsMap.put(p.getName(), p.getBinding());
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

