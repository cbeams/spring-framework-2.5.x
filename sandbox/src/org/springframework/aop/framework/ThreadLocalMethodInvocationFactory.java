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
 * @version $Id: ThreadLocalMethodInvocationFactory.java,v 1.4 2003-12-02 22:28:40 johnsonr Exp $
 */
public class ThreadLocalMethodInvocationFactory extends SimpleMethodInvocationFactory {
	
	private static ThreadLocal instance = new ThreadLocal();
	
	private HashMap methodCache = new HashMap();
	
	public ThreadLocalMethodInvocationFactory() {
	}
	
	public MethodInvocation getMethodInvocation(AdvisedSupport advised, Object proxy, Method method, Class targetClass, Object[] args, List interceptorsAndDynamicInterceptionAdvice) {
		
		ReflectiveMethodInvocation mii = (ReflectiveMethodInvocation) instance.get();
		// Need to use OLD to replace so as not to zap existing
		if (mii == null) {
			mii = new ReflectiveMethodInvocation();
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
		((ReflectiveMethodInvocation) invocation).clear();
	}

	
	


}
