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

import org.springframework.aop.Pointcut;

/**
 * Convenient pointcut-driven advisor implementation.
 *
 * <p>This is the most commonly used Advisor implementation. It can be
 * used with any pointcut and advice type, except for introductions.
 * There is normally no need to subclass this class, or to implement
 * custom Advisors.
 *
 * @author Rod Johnson
 */
public class DefaultPointcutAdvisor extends AbstractPointcutAdvisor implements Serializable {

	private Pointcut pointcut = Pointcut.TRUE;


	/**
	 * Create an empty DefaultPointcutAdvisor.
	 * Advice must be set before use using setter methods.
	 * Pointcut will normally be set also, but defaults to true.
	 */
	public DefaultPointcutAdvisor() {
	}
	
	/**
	 * Create a DefaultPointcutAdvisor that matches all methods.
	 * Pointcut.TRUE will be used as pointcut.
	 * @param advice the advice to use
	 */
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}
	
	/**
	 * Create a DefaultPointcutAdvisor, specifying pointcut
	 * and advice
	 * @param pointcut pointcut targeting the advice
	 * @param advice advice to run when pointcut matches
	 */
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		setAdvice(advice);
	}
	

	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}

	public Pointcut getPointcut() {
		return pointcut;
	}

}
