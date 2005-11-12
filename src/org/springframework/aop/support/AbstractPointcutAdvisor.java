/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;

/**
 * Abstract base class for PointcutAdvisor implementations.
 * Can be subclassed for returning a specific pointcut
 * or a freely configurable pointcut.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1.2
 */
public abstract class AbstractPointcutAdvisor implements PointcutAdvisor, Ordered, Serializable  {

	private int order = Integer.MAX_VALUE;

	private Advice advice;


	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void setAdvice(Advice advice) {
		this.advice = advice;
	}

	public Advice getAdvice() {
		return advice;
	}


	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}
	

	public boolean equals(Object other) {
		if (!(other instanceof PointcutAdvisor)) {
			return false;
		}
		PointcutAdvisor otherAdvisor = (PointcutAdvisor) other;
		return (ObjectUtils.nullSafeEquals(getPointcut(), otherAdvisor.getPointcut()) &&
				ObjectUtils.nullSafeEquals(getAdvice(), otherAdvisor.getAdvice()));
	}

	public int hashCode() {
		int hashCode = (getPointcut() == null ? 0 : getPointcut().hashCode());
		hashCode = 29 * hashCode + (getAdvice() == null ? 0 : getAdvice().hashCode());
		return hashCode;
	}

	public String toString() {
		return "PointcutAdvisor: pointcut [" + getPointcut() + "], advice [" + getAdvice() + "]";
	}

}
