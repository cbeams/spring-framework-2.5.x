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
 * @version $Id: ThreadLocalMethodInvocationFactory.java,v 1.3 2003-11-30 17:59:22 johnsonr Exp $
 */
public class ThreadLocalMethodInvocationFactory extends SimpleMethodInvocationFactory {
	
	private static ThreadLocal instance = new ThreadLocal();
	
	private HashMap methodCache = new HashMap();
	
	public ThreadLocalMethodInvocationFactory() {
	}
	
	public MethodInvocation getMethodInvocation(AdvisedSupport advised, Object proxy, Method method, Class targetClass, Object[] args, List interceptorsAndDynamicInterceptionAdvice) {
		
		MethodInvocationImpl mii = (MethodInvocationImpl) instance.get();
		// Need to use OLD to replace so as not to zap existing
		if (mii == null) {
			mii = new MethodInvocationImpl();
			instance.set(mii);
		}

		mii.populate(
			proxy,
			advised.getTarget(),
			method.getDeclaringClass(),
			method,
			args,
			targetClass,
			interceptorsAndDynamicInterceptionAdvice);
		return mii;
	}

	public void release(MethodInvocation invocation) {
		// TODO move into AOP Alliance
		((MethodInvocationImpl) invocation).clear();
	}

	
	


}
