/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.MethodMatcher;

/**
 * Simple method invocation factory that performs no caching.
 * Convenient superclass for smarter implmentations.
 * Exposes a useful static method for subclasses or other implementations.
 * @author Rod Johnson
 * @version $Id: MethodInvocationFactorySupport.java,v 1.2 2003-11-15 15:30:14 johnsonr Exp $
 */
public class MethodInvocationFactorySupport implements MethodInvocationFactory {


	/**
	 * @see org.springframework.aop.framework.MethodInvocationFactory#getMethodInvocation(org.springframework.aop.framework.ProxyConfig, java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public MethodInvocation getMethodInvocation(Advised config, Object proxy, Method method, Object[] args) {
		Class targetClass = config.getTarget() != null ? config.getTarget().getClass() : method.getDeclaringClass();
		return new MethodInvocationImpl(
			proxy,
			config.getTarget(),
			method.getDeclaringClass(),
			method,
			args,
			targetClass,
			getInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass));
	}
	
	
	/**
	 * Caching subclasses must override this. This implementation does nothing.
	 * @see org.springframework.aop.framework.MethodInvocationFactory#refresh(org.springframework.aop.framework.ProxyConfig)
	 */
	public void refresh(Advised config) {
	}
	
	/**
	 * Return the static interceptors and dynamic interception advice that may apply
	 * to this method invocation
	 * @param config
	 * @param proxy
	 * @param method
	 * @param targetClass
	 * @return list of MethodInterceptor and InterceptionAdvice (if there's a dynamic
	 * method matcher that needs evaluation at runtime)
	 */
	protected static List GetInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
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
	
	
	/**
	 * Subclasses can override this for optimization
	 * @param config
	 * @param proxy
	 * @param method
	 * @param targetClass
	 * @return
	 */
	protected List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		return GetInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
	}

}
