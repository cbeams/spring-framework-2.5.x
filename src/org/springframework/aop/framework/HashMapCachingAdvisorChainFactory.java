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
 * @version $Id: HashMapCachingAdvisorChainFactory.java,v 1.1 2003-11-28 11:17:17 johnsonr Exp $
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {
	
	private HashMap methodCache = new HashMap();
	
	public void refresh(Advised pc) {
		this.methodCache.clear();
	}
	
	public List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		List cached = (List) this.methodCache.get(method);
		if (cached == null) {
			// Recalculate
			cached = AdvisorChainFactoryUtils.calculateInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
			this.methodCache.put(method, cached);
		}
		return cached;
	}

}
