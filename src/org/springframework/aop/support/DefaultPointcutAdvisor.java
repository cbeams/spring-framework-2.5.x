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

import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * Convenient superclass for pointcut-driven advisors, implementing
 * the getPointcut() and isPerInstance() methods.
 * @author Rod Johnson
 * @version $Id: DefaultPointcutAdvisor.java,v 1.4 2004-03-18 02:46:11 trisberg Exp $
 */
public class DefaultPointcutAdvisor implements PointcutAdvisor {
	
	private Pointcut pointcut;
	
	private Object advice;
	
	public DefaultPointcutAdvisor() {
	}
	
	public DefaultPointcutAdvisor(Object advice) {
		this(Pointcut.TRUE, advice);
	}
	
	public DefaultPointcutAdvisor(Pointcut pointcut, Object advice) {
		this.pointcut = pointcut;
		this.advice = advice;
	}

	public void setAdvice(Object object) {
		advice = object;
	}

	public Object getAdvice() {
		return advice;
	}

	public Pointcut getPointcut() {
		return pointcut;
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
