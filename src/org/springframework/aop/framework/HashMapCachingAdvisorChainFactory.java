/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

/**
 * AdvisorChainFactory implementation that caches by method.
 * Uses IdentityHashMap in JVM 1.4, which skips expensive Method.hashCode()
 * call. In 1.3, falls back to using HashMap.
 * @author Rod Johnson
 * @version $Id: HashMapCachingAdvisorChainFactory.java,v 1.3 2004-01-31 10:34:58 johnsonr Exp $
 */
public final class HashMapCachingAdvisorChainFactory implements AdvisorChainFactory {
	
	private Map methodCache = createMap();
	
	private Map createMap() {
		// Use IdentityHashMap, introduced in Java 1.4, which is a lot faster
		// as we want to compare Method keys by reference.
		// The reason we do this via reflection rather than using new is to avoid a dependence in this
		// class that will break it under 1.3
		try {
			Class clazz = Class.forName("java.util.IdentityHashMap");
			return (Map) clazz.newInstance();
		}
		catch (Exception ex) {
			// Shouldn't happen
			LogFactory.getLog(getClass()).debug("Falling back to HashMap (JDK 1.3?): couldn't create an IdentityHashMap using reflection", ex);
			return new HashMap();
		}
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
