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
 * @version $Id: ThreadLocalMethodInvocationFactory.java,v 1.6 2003-12-10 12:05:25 johnsonr Exp $
 */
public class ThreadLocalMethodInvocationFactory extends SimpleMethodInvocationFactory {
	
//	private static ThreadLocal instance = new ThreadLocal();
//	
//	private HashMap methodCache = new HashMap();
//	
//	
//	public ThreadLocalMethodInvocationFactory() {
//	}
//	
//	/**
//	 * @see org.springframework.aop.framework.MethodInvocationFactory#getMethodInvocation(java.lang.Object, java.lang.reflect.Method, java.lang.Class, java.lang.Object, java.lang.Object[], java.util.List, org.springframework.aop.framework.AdvisedSupport)
//	 */
//	public MethodInvocation getMethodInvocation(Object proxy, Method method, Class targetClass, Object target, Object[] args,
//			List interceptorsAndDynamicInterceptionAdvice, AdvisedSupport advised) {
//			
//		ReflectiveMethodInvocation mii = (ReflectiveMethodInvocation) instance.get();
//		// Need to use OLD to replace so as not to zap existing
//		if (mii == null) {
//			mii = new ReflectiveMethodInvocation();
//			instance.set(mii);
//		}
//
//		mii.populate(
//			proxy,
//			target,
//			method.getDeclaringClass(),
//			method,
//			args,
//			targetClass,
//			interceptorsAndDynamicInterceptionAdvice);
//		return mii;
//	}
//
//	public void release(MethodInvocation invocation) {
//		// TODO move into AOP Alliance
//		((ReflectiveMethodInvocation) invocation).clear();
//	}

	
	
	/*
	
		// Code for MethodInvocation subclass if we want to reuse method invocations
	
		protected ReflectiveMethodInvocation() {
		}

		protected void populate(Object proxy, Object target,
							Class targetInterface, Method m, Object[] arguments,
							Class targetClass, List interceptorsAndDynamicMethodMatchers) {
			this.proxy = proxy;
			this.target = target;
			this.targetInterface = targetInterface;
			this.targetClass = targetClass;
			this.method = m;
			this.arguments = arguments;
			this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
			this.currentInterceptorIndex = -1;
		}
	
		public void clear() {
			this.proxy = null;
			this.targetInterface = null;
			this.targetClass = null;
			this.target = null;
			this.method = null;
			this.arguments = null;
			this.interceptorsAndDynamicMethodMatchers = null;
			this.resources = null;
		}
		*/
	


}
