/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;

/**
 * TODO reentrance tests
 * @author Rod Johnson
 * @version $Id: ThreadLocalMethodInvocationFactory.java,v 1.1 2003-11-22 10:40:31 johnsonr Exp $
 */
public class ThreadLocalMethodInvocationFactory extends MethodInvocationFactorySupport {
	
	private static ThreadLocal instance = new ThreadLocal();
	
	private HashMap methodCache = new HashMap();
	
	public ThreadLocalMethodInvocationFactory() {
	}
	
	public MethodInvocation getMethodInvocation(Advised config, Object proxy, Method method, Object[] args) {
		Class targetClass = config.getTarget() != null ? config.getTarget().getClass() : method.getDeclaringClass();
		MethodInvocationImpl mii = (MethodInvocationImpl) instance.get();
		// Need to use OLD to replace so as not to zap existing
		if (mii == null) {
			mii = new MethodInvocationImpl();
			instance.set(mii);
		}

		mii.populate(
			proxy,
			config.getTarget(),
			method.getDeclaringClass(),
			method,
			args,
			targetClass,
			getInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass));
		return mii;
	}

	public void refresh(Advised pc) {
		super.refresh(pc);
		methodCache.clear();
	}
	
	protected List getInterceptorsAndDynamicInterceptionAdvice(Advised config, Object proxy, Method method, Class targetClass) {
		List cached = (List) methodCache.get(method);
		if (cached == null) {
			cached = super.getInterceptorsAndDynamicInterceptionAdvice(config, proxy, method, targetClass);
			methodCache.put(method, cached);
		}
		return cached;
	}

}
