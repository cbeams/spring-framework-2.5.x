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
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;

/**
 * Utility methods for use by AdviceChainFactory implementations.
 * The calculateInterceptorsAndDynamicInterceptionAdvice() method is the
 * definitive way of working out an advice chain for a Method, given an
 * AdvisedSupport object.
 * @author Rod Johnson
 * @version $Id: AdvisorChainFactoryUtils.java,v 1.5 2003-12-11 14:53:13 johnsonr Exp $
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
			else if (advisor instanceof InterceptionIntroductionAdvisor) {
				InterceptionIntroductionAdvisor ia = (InterceptionIntroductionAdvisor) advisor;
				if (ia.getClassFilter().matches(targetClass)) {
					interceptors.add(ia.getIntroductionInterceptor());
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
