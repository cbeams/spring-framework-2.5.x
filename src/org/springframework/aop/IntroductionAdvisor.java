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

package org.springframework.aop;

import org.springframework.aop.framework.AopConfigException;

/**
 * Superinterface for advisors that perform one or more
 * AOP <b>introductions</b>.
 * <br>This interface cannot be implemented directly; subinterfaces
 * must provide the advice type implementing the introduction.
 * <br>
 * Introduction is the implementation of additional interfaces 
 * (not implemented by a target) via AOP advice.
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @see org.springframework.aop.IntroductionInterceptor
 * @version $Id: IntroductionAdvisor.java,v 1.3 2004-03-18 02:46:07 trisberg Exp $
 */
public interface IntroductionAdvisor extends Advisor {
	
	/**
	 * @return the filter determining which target classes this introduction
	 * should apply to. The class part of a pointcut. Note that method matching
	 * doesn't make sense to introductions.
	 */
	ClassFilter getClassFilter();
	
	/**
	 * @return the additional interfaces introduced by this Advisor
	 */
	Class[] getInterfaces();
	
	/**
	 * Can the advised interfaces be implemented by the 
	 * introduction advice? Invoked before adding an IntroductionAdvisor.
	 * @throws AopConfigException if the advised interfaces can't
	 * be implemented by the introduction advice
	 */
	void validateInterfaces() throws AopConfigException;

}
