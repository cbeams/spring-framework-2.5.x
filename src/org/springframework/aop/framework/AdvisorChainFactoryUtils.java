/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.Advisor;
import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.BeforeAdvisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.MethodBeforeAdviceInterceptor;

/**
 * Utility methods for use by AdviceChainFactory implementations.
 * The calculateInterceptorsAndDynamicInterceptionAdvice() method is the
 * definitive way of working out an advice chain for a Method, given an
 * AdvisedSupport object.
 * @author Rod Johnson
 * @version $Id: AdvisorChainFactoryUtils.java,v 1.3 2003-12-05 13:05:54 johnsonr Exp $
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
					MethodMatcher mm = pointcutAdvisor.getPointcut().getMethodMatcher();
					if (mm.matches(method, targetClass)) {
						if (mm.isRuntime()) {
							interceptors.add(new InterceptorAndDynamicMethodMatcher((MethodInterceptor) getInterceptor(advisor), mm) );
						}
						else {							
							interceptors.add(getInterceptor(advisor));
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
	
	
	/**
	 * Find an interceptor for this advisor if possible.
	 * Object creation here isn't a problem, as advice chains, once created,
	 * are normally cached.
	 */
	private static Interceptor getInterceptor(Advisor advisor) {
		/**
		 * As we add additional advice types, we may make this data driven, or
		 * make it possible to register new adapters for different advice types.
		 */
		
		if (advisor instanceof InterceptionAroundAdvisor) {
			return ((InterceptionAroundAdvisor) advisor).getInterceptor();
		}
		else if (advisor instanceof BeforeAdvisor) {
			BeforeAdvisor ba = (BeforeAdvisor) advisor;
			BeforeAdvice advice = ba.getBeforeAdvice();
			// TODO class cast
			return new MethodBeforeAdviceInterceptor( (MethodBeforeAdvice) advice) ;
		}
		throw new AopConfigException("Cannot create Interceptor for unknown advisor type: " + advisor);
	}
	
	
	
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
