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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.aspectj.weaver.tools.ShadowMatch;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAwareMethodMatcher;
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
public class AspectJExpressionPointcut extends AbstractExpressionPointcut implements ClassFilter, IntroductionAwareMethodMatcher {

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
			buildPointcutExpression();
		}
	}

	private void buildPointcutExpression() {
		PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
		for (int i = 0; i < pointcutParameters.length; i++) {
			pointcutParameters[i] = this.pointcutParser.createPointcutParameter(
					this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
		}
		this.pointcutExpression =
			this.pointcutParser.parsePointcutExpression(
					replaceBooleanOperators(getExpression()), pointcutDeclarationScope, pointcutParameters);
	}

	public boolean matches(Class targetClass) {
		checkReadyToMatch();
		return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
	}

	public boolean isRuntime() {
		checkReadyToMatch();
		return this.pointcutExpression.mayNeedDynamicTest();
	}

	public boolean matches(Method method, Class targetClass, boolean beanHasIntroductions) {
		checkReadyToMatch();
		Method methodToMatch = findMethodToMatchAgainst(method,targetClass);
		ShadowMatch shadowMatch = getShadowMatch(methodToMatch);
		// special handling for this, target, @this, @target, @annotation
		// in Spring - we can optimize since we know we have exactly this class,
		// and there will never be matching subclass at runtime.
		if (shadowMatch.alwaysMatches()) {
			return true;
		}
		else if (shadowMatch.neverMatches()) {
			return false;
		}
		else {
		  // the maybe case
		  if (!beanHasIntroductions) {
			  return matchesIgnoringSubtypes(shadowMatch);
		  }
		  else {
			  return true;
		  }
		}
	}

	public boolean matches(Method method, Class targetClass) {
		return matches(method,targetClass,false);
	}

	/**
	 * If we are proxying an interface, the declaring type of the
	 * method may not match the declaring type of the targetClass -
	 * because we got the method from a proxy. We need to find the
	 * corresponding targetClass method and match on that instead.
	 * @param method
	 * @param targetClass
	 * @return
	 */
	private Method findMethodToMatchAgainst(Method method, Class targetClass) {
		Class originalTargetClass = targetClass;
		Class declaredClass = method.getDeclaringClass();
		if (declaredClass.isInterface()) {
			// find the *implementing* method and match on that instead.
			Class[] ptypes = method.getParameterTypes();
			String name = method.getName();
			do {
				try {
					return targetClass.getDeclaredMethod(name, ptypes);
				}
				catch (NoSuchMethodException ex) {
					targetClass = targetClass.getSuperclass();
				}
			}
			while (targetClass != null);
			// this odd situation can arise as a result of declare parents
			// statements.
			return method;
		} else {
			return method;
		}
	}

	/**
	 * A match test returned maybe - if there are any subtype sensitive variables
	 * involved in the test (this, target, at_this, at_target, at_annotation) then
	 * we say this is not a match as in Spring there will never be a different 
	 * runtime subtype.
	 * @return
	 */
	private boolean matchesIgnoringSubtypes(ShadowMatch shadowMatch) {
		return !(new RuntimeTestWalker(shadowMatch).testsSubtypeSensitiveVars());
	}

	public boolean matches(Method method, Class targetClass, Object[] args) {
		checkReadyToMatch();
		Method methodToMatch = findMethodToMatchAgainst(method,targetClass);
		ShadowMatch shadowMatch = getShadowMatch(methodToMatch);

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
		catch (IllegalStateException ex) {
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

	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((this.pointcutDeclarationScope == null) ? 0 : this.pointcutDeclarationScope.hashCode());
		result = PRIME * result + Arrays.hashCode(this.pointcutParameterNames);
		result = PRIME * result + Arrays.hashCode(this.pointcutParameterTypes);
		result = PRIME * result + ((this.getExpression() == null) ? 0 : this.getExpression().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AspectJExpressionPointcut other = (AspectJExpressionPointcut) obj;
		if (this.pointcutDeclarationScope == null) {
			if (other.pointcutDeclarationScope != null) {
				return false;
			}
		}
		else if (!this.pointcutDeclarationScope.equals(other.pointcutDeclarationScope)) {
			return false;
		}
		if (this.getExpression() == null) {
			if (other.getExpression() != null) {
				return false;
			}
		}
		else if (!this.getExpression().equals(other.getExpression())) {
			return false;
		}
		if (!Arrays.equals(this.pointcutParameterNames, other.pointcutParameterNames)) {
			return false;
		}
		if (!Arrays.equals(this.pointcutParameterTypes, other.pointcutParameterTypes)) {
			return false;
		}
		return true;
	}

}
