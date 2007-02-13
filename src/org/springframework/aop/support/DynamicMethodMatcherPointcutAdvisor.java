/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.support;

import java.io.Serializable;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * Convenient superclass for Advisors that are also dynamic pointcuts.
 * Serializable if both Advice and Advisor subclass are.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @deprecated since 2.0, in favor of using {@link DefaultPointcutAdvisor}
 * with a runtime {@link DynamicMethodMatcherPointcut}
 */
public abstract class DynamicMethodMatcherPointcutAdvisor extends DynamicMethodMatcherPointcut
    implements PointcutAdvisor, Ordered, Serializable {

	private int order = Integer.MAX_VALUE;

	private Advice advice;


	/**
	 * Create a new DynamicMethodMatcherPointcutAdvisor,
	 * expecting bean-style configuration.
	 * @see #setAdvice
	 */
	protected DynamicMethodMatcherPointcutAdvisor() {
	}

	/**
	 * Create a new DynamicMethodMatcherPointcutAdvisor for the given advice.
	 * @param advice the Advice to use
	 */
	protected DynamicMethodMatcherPointcutAdvisor(Advice advice) {
		Assert.notNull(advice, "Advice must not be null");
		this.advice = advice;
	}


	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return this.order;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public Advice getAdvice() {
		return this.advice;
	}

	public boolean isPerInstance() {
		return true;
	}

	public final Pointcut getPointcut() {
		return this;
	}

}
