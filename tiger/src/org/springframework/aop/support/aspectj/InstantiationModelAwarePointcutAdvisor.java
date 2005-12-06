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

package org.springframework.aop.support.aspectj;

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.reflect.PerClauseKind;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.DynamicMethodMatcherPointcut;
import org.springframework.aop.support.Pointcuts;

/**
 * Internal implementation of AspectJPointcutAdvisor
 * Note that there will be one instance of this advisor for each target.
 * @author Rod Johnson
 * @since 1.3
 */
class InstantiationModelAwarePointcutAdvisor implements PointcutAdvisor {

	private final AspectJExpressionPointcut declaredPointcut;
	
	private Pointcut pointcut;
	
	private final MetadataAwareAspectInstanceFactory aif;
	
	private final Method method;
	
	private final AtAspectJAdvisorFactory atAspectJAdvisorFactory;
	
	private Advice instantiatedAdvice;
	
	public InstantiationModelAwarePointcutAdvisor(AtAspectJAdvisorFactory af, AspectJExpressionPointcut ajexp, 
				final MetadataAwareAspectInstanceFactory aif, Method method) {
		this.declaredPointcut = ajexp;
		this.method = method;
		this.atAspectJAdvisorFactory = af;
		this.aif = aif;
		
		if (isPerInstance()) {
			// Static part of the pointcut is either pertarget or this
			final Pointcut preInstantiationORPointcut = Pointcuts.union(aif.getAspectMetadata().getPerClausePointcut(), this.declaredPointcut);
			
			// Make it dynamic: must mutate from pre-instantiation to post-instantiation state.
			// If it's not a dynamic pointcut, it may be optimized out 
			// by the Spring AOP infrastructure after the first evaluation
			this.pointcut = new DynamicMethodMatcherPointcut() {
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
			};
		}
		else {
			// A singleton aspect.
			this.instantiatedAdvice = instantiateAdvice();
			this.pointcut = declaredPointcut;
		}
	}
	
	/**
	 * The pointcut for Spring AOP to use. Actual behaviour of the pointcut will change
	 * depending on the state of the advice.
	 */
	public Pointcut getPointcut() {
		return pointcut;
	}
	

	public boolean isPerInstance() {
		// TODO or perthis etc.
		return getAspectMetadata().getAjType().getPerClause().getKind() == PerClauseKind.PERTARGET;
	}
	
	public AspectMetadata getAspectMetadata() {
		return this.aif.getAspectMetadata();
	}

	public synchronized Advice getAdvice() {
		if (instantiatedAdvice == null) {
			instantiatedAdvice = instantiateAdvice();
		}
		return instantiatedAdvice;
	}

	private Advice instantiateAdvice() {
		return this.atAspectJAdvisorFactory.getAdvice(method, aif);
	}
	
	public MetadataAwareAspectInstanceFactory getAspectInstanceFactory() {
		return this.aif;
	}

	public AspectJExpressionPointcut getDeclaredPointcut() {
		return this.declaredPointcut;
	}

}
