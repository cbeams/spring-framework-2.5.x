/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * AdvisorChainFactory implementation that caches by method.
 * @author Rod Johnson
 * @version $Id: HashMapCachingAdvisorChainFactory.java,v 1.2 2003-12-01 10:02:25 johnsonr Exp $
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {
	
	private HashMap methodCache = new HashMap();
	
	
	public List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		List cached = (List) this.methodCache.get(method);
		if (cached == null) {
			// Recalculate
			cached = AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
			this.methodCache.put(method, cached);
		}
		return cached;
	}


	/**
	 * @see org.springframework.aop.framework.AdvisedSupportListener#activated(org.springframework.aop.framework.AdvisedSupport)
	 */
	public void activated(AdvisedSupport advisedSupport) {
	}


	/**
	 * @see org.springframework.aop.framework.AdvisedSupportListener#adviceChanged(org.springframework.aop.framework.AdvisedSupport)
	 */
	public void adviceChanged(AdvisedSupport advisedSupport) {
		methodCache.clear();
	}

}
