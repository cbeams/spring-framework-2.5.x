
package org.springframework.benchmark;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

/**
 * 
 * @author Rod Johnson
 */
public abstract class Advices {


	/**
	 * We're more interested in the framework than anything. 
	 * 
	 * @author Rod Johnson
	 */
	public static class NopInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			return mi.proceed();
		}
	}

	/**
	 * We're more interested in the framework than anything. 
	 * 
	 * @author Rod Johnson
	 */
	public static class ReadDataInterceptor implements MethodInterceptor {
		public Object invoke(MethodInvocation mi) throws Throwable {
			mi.getArguments();
			mi.getMethod().getName();
			//mi.s
			return mi.proceed();
		}
	}

	/**
	 * Static method
	 * 
	 * @author Rod Johnson
	 */
	public static class SetterPointCut extends StaticMethodMatcherPointcutAdvisor {
		public static final String SET= "set";
		public SetterPointCut(MethodInterceptor mi) {
			super(mi);
		}

		/**
		 * @see org.springframework.aop.framework.StaticMethodPointcut#applies(java.lang.reflect.Method, org.aopalliance.intercept.AttributeRegistry)
		 */
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith(SET);
		}
	}
	
	
	public static class ObjectReturnPointCut extends StaticMethodMatcherPointcutAdvisor {
		public ObjectReturnPointCut(MethodInterceptor mi) {
			super(mi);
		}

		/**
		 * @see org.springframework.aop.framework.StaticMethodPointcut#applies(java.lang.reflect.Method, org.aopalliance.intercept.AttributeRegistry)
		 */
		public boolean matches(Method m, Class targetClass) {
			boolean ret = m.getReturnType().isAssignableFrom(Object.class);
			//System.out.println(m.getReturnType() + ": " + ret);
			return ret;
		}
	}

}
