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

package org.springframework.aop.support;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;

/**
 * Convenient class for name-match method pointcuts that hold an Interceptor,
 * making them an Advisor.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public class NameMatchMethodPointcutAdvisor extends NameMatchMethodPointcut
		implements PointcutAdvisor, Ordered {

	private int order = Integer.MAX_VALUE;

	private Advice advice;


	public NameMatchMethodPointcutAdvisor() {
	}

	public NameMatchMethodPointcutAdvisor(Advice advice) {
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


	public Pointcut getPointcut() {
		return this;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}
		return (other instanceof NameMatchMethodPointcutAdvisor &&
				ObjectUtils.nullSafeEquals(((NameMatchMethodPointcutAdvisor) other).advice, this.advice));
	}

	public int hashCode() {
		int code = 17;
		code = 37 * code + super.hashCode();
		if (this.advice != null) {
			code = 37 * code + this.advice.hashCode();
		}
		return code;
	}
}
