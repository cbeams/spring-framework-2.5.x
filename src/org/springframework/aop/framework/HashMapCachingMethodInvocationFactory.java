/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * @author Rod Johnson
 * @version $Id: HashMapCachingMethodInvocationFactory.java,v 1.3 2003-11-21 22:45:29 jhoeller Exp $
 */
public class HashMapCachingMethodInvocationFactory extends MethodInvocationFactorySupport {
	
	private HashMap methodCache = new HashMap();
	
	public void refresh(Advised pc) {
		super.refresh(pc);
		this.methodCache.clear();
	}
	
	protected List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		List cached = (List) this.methodCache.get(method);
		if (cached == null) {
			cached = super.getInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
			this.methodCache.put(method, cached);
		}
		return cached;
	}

}
