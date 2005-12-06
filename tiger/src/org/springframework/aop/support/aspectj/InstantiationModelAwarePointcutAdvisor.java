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
import org.springframework.aop.support.Pointcuts;

/**
 * Internal implementation of AspectJPointcutAdvisor
 * Note that there will be one instance of this advisor for each target.
 * @author Rod Johnson
 * @since 1.3
 */
class InstantiationModelAwarePointcutAdvisor implements PointcutAdvisor {

	private final Pointcut pointcut;
	
	private Pointcut preInstantiationPointcut;
	
	private final MetadataAwareAspectInstanceFactory aif;
	
	private final Method method;
	
	private final AtAspectJAdvisorFactory atAspectJAdvisorFactory;
	
	private Advice instantiatedAdvice;
	
	public InstantiationModelAwarePointcutAdvisor(AtAspectJAdvisorFactory af, AspectJExpressionPointcut ajexp, 
			MetadataAwareAspectInstanceFactory aif, Method method) {
		this.pointcut = ajexp;
		this.method = method;
		this.atAspectJAdvisorFactory = af;
		this.aif = aif;
		// TODO or perthis
		if (aif.getAspectMetadata().getAjType().getPerClause().getKind() == PerClauseKind.PERTARGET) {
			this.preInstantiationPointcut = Pointcuts.intersection(aif.getAspectMetadata().getPerClausePointcut(), this.pointcut);
		}
		else {
			this.instantiatedAdvice = instantiateAdvice();
		}
	}
	
	/**
	 * The pointcut for Spring AOP to use
	 */
	public Pointcut getPointcut() {
		if (instantiatedAdvice == null) {
			return preInstantiationPointcut;
		}
		else {
			return this.pointcut;
		}
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

}
