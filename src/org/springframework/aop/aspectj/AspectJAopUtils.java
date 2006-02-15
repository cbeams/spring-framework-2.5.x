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
 *
 * Created on 15-Feb-2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.aop.framework.adapter.AfterReturningAdviceInterceptor;
import org.springframework.aop.framework.adapter.ThrowsAdviceInterceptor;

/**
 * @author Adrian Colyer
 * @since 2.0
 */
public abstract class AspectJAopUtils {

	private AspectJAopUtils() {};
	
	/**
	 * Return true if the advisor is a form of after advice.
	 * 
	 * TODO: listing all of these implementation types here has a bad
	 * smell to it...
	 */
	public static boolean isAfterAdvice(Advisor anAdvisor) {
		Advice advice = anAdvisor.getAdvice();
		if ((advice instanceof AfterReturningAdvice) ||
		    (advice instanceof ThrowsAdvice) ||
		    (advice instanceof AfterReturningAdviceInterceptor) ||
		    (advice instanceof AspectJAfterAdvice) ||
		    (advice instanceof AspectJAfterReturningAdvice) ||
		    (advice instanceof AspectJAfterThrowingAdvice) ||
		    (advice instanceof ThrowsAdviceInterceptor)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isBeforeAdvice(Advisor anAdvisor) {
		return (anAdvisor.getAdvice() instanceof BeforeAdvice);
	}
	
}
