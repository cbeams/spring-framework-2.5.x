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

package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.AspectJPrecedenceInformation;
import org.springframework.aop.aspectj.InstantiationModelAwarePointcutAdvisor;
import org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.Pointcuts;
import org.springframework.core.Ordered;

/**
 * Internal implementation of AspectJPointcutAdvisor
 * Note that there will be one instance of this advisor for each target method.
 *
 * @author Rod Johnson
 * @since 2.0
 */
class InstantiationModelAwarePointcutAdvisorImpl
		implements InstantiationModelAwarePointcutAdvisor, Ordered, AspectJPrecedenceInformation {

	private final AspectJExpressionPointcut declaredPointcut;
	
	private Pointcut pointcut;
	
	private final MetadataAwareAspectInstanceFactory aif;
	
	private final Method method;
	
	private final boolean lazy;
	
	private final AspectJAdvisorFactory atAspectJAdvisorFactory;
	
	private Advice instantiatedAdvice;

	private int declarationOrder;
	
	private String aspectName;
	
	private int aspectOrder = Ordered.LOWEST_PRECEDENCE;
	
	private Boolean isBeforeAdvice = null;

	private Boolean isAfterAdvice = null;


	public InstantiationModelAwarePointcutAdvisorImpl(
			AspectJAdvisorFactory af, 
			AspectJExpressionPointcut ajexp, 
			MetadataAwareAspectInstanceFactory aif, 
			Method method,
			int declarationOrderInAspect,
			String aspectName) {

		this.declaredPointcut = ajexp;
		this.method = method;
		this.atAspectJAdvisorFactory = af;
		this.aif = aif;
		this.declarationOrder = declarationOrderInAspect;
		this.aspectName = aspectName;
		
		if (aif.getAspectMetadata().isLazilyInstantiated()) {
			// Static part of the pointcut is a lazy type
			final Pointcut preInstantiationORPointcut =
					Pointcuts.union(aif.getAspectMetadata().getPerClausePointcut(), this.declaredPointcut);
			
			// Make it dynamic: must mutate from pre-instantiation to post-instantiation state.
			// If it's not a dynamic pointcut, it may be optimized out 
			// by the Spring AOP infrastructure after the first evaluation
			this.pointcut = new PerTargetInstantiationModelPointcut(declaredPointcut, preInstantiationORPointcut, aif);
			this.lazy = true;
		}
		else {
			// A singleton aspect.
			this.instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
			this.pointcut = declaredPointcut;
			this.lazy = false;
		}
	}


	/**
	 * The pointcut for Spring AOP to use. Actual behaviour of the pointcut will change
	 * depending on the state of the advice.
	 */
	public Pointcut getPointcut() {
		return pointcut;
	}

	/**
	 * This is only of interest for Spring AOP:
	 * AspectJ instantiation semantics are much richer.
	 * In AspectJ terminology, all a return of true means here is that the
	 * aspect is not a SINGLETON. 
	 */
	public boolean isPerInstance() {
		return getAspectMetadata().getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON;
	}
	
	public AspectMetadata getAspectMetadata() {
		return this.aif.getAspectMetadata();
	}

	/**
	 * Lazily instantiate advice if necessary
	 */
	public synchronized Advice getAdvice() {
		if (instantiatedAdvice == null) {
			instantiatedAdvice = instantiateAdvice(this.declaredPointcut);
		}
		return instantiatedAdvice;
	}
	
	public boolean isLazy() {
		return this.lazy;
	}

	public synchronized boolean isAdviceInstantiated() {
		return instantiatedAdvice != null;
	}


	private Advice instantiateAdvice(AspectJExpressionPointcut pcut) {
		return this.atAspectJAdvisorFactory.getAdvice(method, pcut, aif, declarationOrder, aspectName);
	}
	
	public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
		return this.aif;
	}

	public AspectJExpressionPointcut getDeclaredPointcut() {
		return this.declaredPointcut;
	}
	
	@Override
	public String toString() {
		return "InstantiationModelAwarePointcutAdvisor: expr='" + getDeclaredPointcut().getExpression() +
			"' advice method=" + this.method + "; perClauseKind=" +
			aif.getAspectMetadata().getAjType().getPerClause().getKind() +
			"; instantiationCount=" + aif.getInstantiationCount();
			                                                 
	}


	/**
	 * Pointcut implementation that changes its behaviour when the advice is instantiated.
	 * Note that this is a <i>dynamic</i> pointcut. Otherwise it might
	 * be optimized out if it does not at first match satically.
	 */
	private class PerTargetInstantiationModelPointcut extends DynamicMethodMatcherPointcut {

		private final AspectJExpressionPointcut declaredPointcut;

		private final Pointcut preInstantiationORPointcut;

		private final MetadataAwareAspectInstanceFactory aif;

		private PerTargetInstantiationModelPointcut(
				AspectJExpressionPointcut declaredPointcut, Pointcut preInstantiationORPointcut, MetadataAwareAspectInstanceFactory aif) {
			super();
			this.declaredPointcut = declaredPointcut;
			this.preInstantiationORPointcut = preInstantiationORPointcut;
			this.aif = aif;
		}

		@Override
		public boolean matches(Method method, Class targetClass) {
			// We're either instantiated, matching on declared pointcut, or uninstantiated matching on either pointcut
			return (aif.getInstantiationCount() > 0 && declaredPointcut.matches(method, targetClass)) ||
				preInstantiationORPointcut.getMethodMatcher().matches(method, targetClass);
		}

		public boolean matches(Method method, Class targetClass, Object[] args) {
			// This can match only on declared pointcut
			return aif.getInstantiationCount() > 0 && declaredPointcut.matches(method, targetClass);
		}
	}

	public void setOrder(int order) {
		this.aspectOrder = order;
	}
	
	public int getOrder() {
		return this.aspectOrder;
	}

	public String getAspectName() {
		return this.aspectName;
	}

	public int getDeclarationOrder() {
		return this.declarationOrder;
	}

	public boolean isBeforeAdvice() {
		if (isBeforeAdvice == null) {
			determineAdviceType();
		}
		return isBeforeAdvice;
	}

	public boolean isAfterAdvice() {
		if (isAfterAdvice == null) {
			determineAdviceType();
		}
		return isAfterAdvice;
	}

	/**
	 * Duplicates some logic from getAdvice, but importantly does not force
	 * creation of the advice.
	 */
	private void determineAdviceType() {
		Class<?> candidateAspectClass = aif.getAspectMetadata().getAspectClass();
		AspectJAnnotation<?> aspectJAnnotation =
				AbstractAspectJAdvisorFactory.findAspectJAnnotationOnMethod(this.method);
		if (aspectJAnnotation == null) {
			isBeforeAdvice = false;
			isAfterAdvice = false;
		}
		else {
			switch (aspectJAnnotation.getAnnotationType()) {
				case AtAfter:
				case AtAfterReturning:
				case AtAfterThrowing:
					isAfterAdvice = true;
					isBeforeAdvice = false;
					break;
				case AtAround:
				case AtPointcut:
					isAfterAdvice = false;
					isBeforeAdvice = false;
					break;
				case AtBefore:
					isAfterAdvice = false;
					isBeforeAdvice = true;
			}
		}
	}

}
