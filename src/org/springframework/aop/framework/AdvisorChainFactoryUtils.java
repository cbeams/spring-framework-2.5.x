/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.MethodMatcher;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AdvisorChainFactoryUtils.java,v 1.1 2003-11-28 11:17:17 johnsonr Exp $
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
			if (advisor instanceof InterceptionAroundAdvisor) {
				InterceptionAroundAdvisor ia = (InterceptionAroundAdvisor) advisor;
				if (ia.getPointcut().getClassFilter().matches(targetClass)) {
					MethodMatcher mm = ia.getPointcut().getMethodMatcher();
					if (mm.matches(method, targetClass)) {
						if (mm.isRuntime()) {
							interceptors.add(new InterceptorAndDynamicMethodMatcher((MethodInterceptor) ia.getInterceptor(), mm) );
						}
						else {							
							interceptors.add(ia.getInterceptor());
						}
					}
				}
			}
			else if (advisor instanceof InterceptionIntroductionAdvisor) {
				InterceptionIntroductionAdvisor ia = (InterceptionIntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(targetClass)) {
					interceptors.add(ia.getIntroductionInterceptor());
				}
			}
		}	// for
		return interceptors;
	}
	
	
	
	public static AdvisorChainFactory SIMPLE_ADVISOR_CHAIN_FACTORY = new AdvisorChainFactory() {
		public void refresh(Advised config) {
		}

		public List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
			return AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
		}
	};
	
}
