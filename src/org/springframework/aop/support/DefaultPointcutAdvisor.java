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

package org.springframework.aop.support;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.core.Ordered;

/**
 * Convenient pointcut-driven advisor implementation, implementing
 * the getPointcut() and isPerInstance() methods.
 *
 * <p>This is the most commonly used Advisor implementation. It can be
 * used with any pointcut and advice type, except for introductions.
 *
 * @author Rod Johnson
 * @version $Id: DefaultPointcutAdvisor.java,v 1.7 2004-03-23 14:29:45 jhoeller Exp $
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor, Ordered {

	private int order = Integer.MAX_VALUE;

	private Pointcut pointcut;
	
	private Advice advice;
	
	public DefaultPointcutAdvisor() {
	}
	
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}
	
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		this.advice = advice;
	}

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

	public Pointcut getPointcut() {
		return pointcut;
	}
	
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	public boolean isPerInstance() {
		throw new UnsupportedOperationException("perInstance property of Advisor is not yet supported in Spring");
	}

	public boolean equals(Object o) {
		if (!(o instanceof DefaultPointcutAdvisor)) {
			return false;
		}
		DefaultPointcutAdvisor other = (DefaultPointcutAdvisor) o;
		return other.advice.equals(this.advice) && other.pointcut.equals(this.pointcut);
	}
	
	public String toString() {
		return "DefaultPointcutAdvisor: pointcut=[" + pointcut + "] advice=[" + advice + "]";
	}

}
