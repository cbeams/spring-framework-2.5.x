/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.factory.NamedBean;

/**
 * Convenient methods for creating advisors that may be used when autoproxying beans
 * created with the Spring IoC container, binding the bean name to the current
 * invocation. May support a <code>bean()</code> pointcut designator with AspectJ.
 *
 * <p>Typically used in Spring auto-proxying, where the bean name is known
 * at proxy creation time.
 *
 * @author Rod Johnson
 * @since 2.0
 * @see org.springframework.beans.factory.NamedBean
 */
public abstract class ExposeBeanNameAdvisors {

	/**
	 * Binding for the bean name of the bean which is currently being invoked
	 * in the ReflectiveMethodInvocation userAttributes Map.
	 */
	private static final String BEAN_NAME_ATTRIBUTE = ExposeBeanNameAdvisors.class.getName() + ".beanName";

	
	/**
	 * Find the bean name for the current invocation. Assumes that an ExposeBeanNameAdvisor
	 * has been included in the interceptor chain, and that the invocation is exposed
	 * with ExposeInvocationInterceptor.
	 * @return the bean name (never <code>null</code>)
	 * @throws IllegalStateException if the bean name has not been exposed
	 */
	public static String getBeanName() throws IllegalStateException {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		return getBeanName(mi);
	}
	
	/**
	 * Find the bean name for the given invocation. Assumes that an ExposeBeanNameAdvisor
	 * has been included in the interceptor chain.
	 * @param mi MethodInvocation that should contain the bean name as an attribute
	 * @return the bean name (never <code>null</code>)
	 * @throws IllegalStateException if the bean name has not been exposed
	 */
	public static String getBeanName(MethodInvocation mi) throws IllegalStateException {
		if (!(mi instanceof ReflectiveMethodInvocation)) {
			throw new IllegalArgumentException("Not a Spring AOP ReflectiveMethodInvocation");
		}
		ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
		String beanName = (String) rmi.getUserAttribute(BEAN_NAME_ATTRIBUTE);
		if (beanName == null) {
			throw new IllegalStateException("Cannot get bean name: not set on MethodInvocation. " +
					"Include ExposeBeanNameAdvisor in interceptor chain.");
		}
		return beanName;
	}
	
	
	/**
	 * Create a new advisor that will expose the given bean name,
	 * with no introduction
	 * @param beanName bean name to expose
	 */
	public static Advisor createAdvisorWithoutIntroduction(String beanName) {
		return new DefaultPointcutAdvisor(new ExposeBeanNameInterceptor(beanName));
	}
	
	
	/**
	 * Create a new advisor that will expose the given bean name, introducing
	 * the NamedBean interface to make the bean name accessible without forcing
	 * the target object to be aware of this Spring IoC concept.
	 * @param beanName bean name to expose
	 */
	public static Advisor createAdvisorIntroducingNamedBean(String beanName) {
		return new DefaultIntroductionAdvisor(new ExposeBeanNameIntroduction(beanName));
	}
	
	
	private static class ExposeBeanNameInterceptor implements MethodInterceptor {
		
		private final String beanName;
		
		public ExposeBeanNameInterceptor(String beanName) {
			this.beanName = beanName;
		}
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
			rmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, beanName);
			return mi.proceed();
		}
	}


	private static class ExposeBeanNameIntroduction extends DelegatingIntroductionInterceptor implements NamedBean {
		
		private final String beanName; 
		
		public ExposeBeanNameIntroduction(String beanName) {
			this.beanName = beanName;
		}
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
			rmi.setUserAttribute(BEAN_NAME_ATTRIBUTE, beanName);
			return super.invoke(mi);
		}
		
		public String getBeanName() {
			return beanName;
		}
	}

}
