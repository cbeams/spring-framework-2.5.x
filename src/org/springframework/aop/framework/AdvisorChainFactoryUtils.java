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

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;

/**
 * Utility methods for use by AdviceChainFactory implementations.
 * The calculateInterceptorsAndDynamicInterceptionAdvice() method is the
 * definitive way of working out an advice chain for a Method, given an
 * AdvisedSupport object.
 * @author Rod Johnson
 * @version $Id: AdvisorChainFactoryUtils.java,v 1.7 2004-03-18 02:46:05 trisberg Exp $
 */
public abstract class AdvisorChainFactoryUtils {

	/**
	 * Return the static interceptors and dynamic interception advice that may apply
	 * to this method invocation.
	 * @param config
	 * @param proxy
	 * @param method
	 * @param targetClass
	 * @return list of MethodInterceptor and InterceptionAdvice (if there's a dynamic
	 * method matcher that needs evaluation at runtime)
	 */
	public static List calculateInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		List interceptors = new ArrayList(config.getAdvisors().length);
		for (int i = 0; i < config.getAdvisors().length; i++) {
			Advisor advisor = config.getAdvisors()[i];
			if (advisor instanceof PointcutAdvisor) {
				// Add it conditionally
				PointcutAdvisor pointcutAdvisor = (PointcutAdvisor) advisor;
				if (pointcutAdvisor.getPointcut().getClassFilter().matches(targetClass)) {
					MethodInterceptor interceptor = (MethodInterceptor) GlobalAdvisorAdapterRegistry.getInstance().getInterceptor(advisor);
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					if (mm.matches(method, targetClass)) {
						if (mm.isRuntime()) {
							// Creating a new object instance in the getInterceptor() method
							// isn't a problem as we normally cache created chains
							interceptors.add(new InterceptorAndDynamicMethodMatcher(interceptor, mm) );
						}
						else {							
							interceptors.add(interceptor);
						}
					}
				}
			}
			else if (advisor instanceof IntroductionAdvisor) {
				IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(targetClass)) {
					MethodInterceptor interceptor = (MethodInterceptor) GlobalAdvisorAdapterRegistry.getInstance().getInterceptor(advisor);
					interceptors.add(interceptor);
				}
			}
		}	// for
		return interceptors;
	}	// calculateInterceptorsAndDynamicInterceptionAdvice
	
	
	
	public static AdvisorChainFactory SIMPLE_ADVISOR_CHAIN_FACTORY = new AdvisorChainFactory() {

		public List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
			return AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
		}

		public void activated(AdvisedSupport advisedSupport) {
		}

		public void adviceChanged(AdvisedSupport advisedSupport) {
		}
	};
	
}
