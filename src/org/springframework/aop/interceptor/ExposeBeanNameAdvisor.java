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

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * Advisor that may be used when autoproxying beans
 * created with the Spring IoC container, binding the bean name
 * to the current invocation. May support a bean()
 * pointcut designator with AspectJ.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class ExposeBeanNameAdvisor extends DefaultPointcutAdvisor {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Binding for the bean name of the bean which is currently being invoked
	 * in the ReflectiveMethodInvocation userAttributes Map
	 */
	private static final String BEAN_NAME_ATTRIBUTE = ExposeBeanNameAdvisor.class.getName() + ".beanName";

	
	/**
	 * Find the bean name for the current invocation. Assumes that an ExposeBeanNameAdvisor
	 * has been included in the interceptor chain, and that the invocation is exposed
	 * with ExposeInvocationInterceptor.
	 * @return the bean name. Never returns null
	 * @throws AspectException if the bean name has not been exposed
	 */
	public static String getBeanName() throws AspectException, IllegalArgumentException {
		MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
		return getBeanName(mi);
	}
	
	/**
	 * Find the bean name for the given invocation. Assumes that an ExposeBeanNameAdvisor
	 * has been included in the interceptor chain.
	 * @param mi MethodInvocation that should contain the bean name as an attribute
	 * @return the bean name. Never returns null
	 * @throws AspectException if the bean name has not been exposed
	 */
	public static String getBeanName(MethodInvocation mi) throws AspectException, IllegalArgumentException {
		if (!(mi instanceof ReflectiveMethodInvocation)) {
			throw new IllegalArgumentException("Not a Spring AOP ReflectiveMethodInvocation");
		}
		String beanName = (String) ((ReflectiveMethodInvocation) mi).getUserAttributes().get(BEAN_NAME_ATTRIBUTE);
		if (beanName == null) {
			String mesg = "Cannot get bean name: not set on MethodInvocation. Include ExposeBeanNameAdvisor in interceptor chain";
			throw new AspectException(mesg, new Throwable(mesg));
		}
		return beanName;
	}
	
	
	private final String beanName;
	
	/**
	 * Create a new advisor that will expose the given bean name
	 * @param beanName bean name to expose
	 */
	public ExposeBeanNameAdvisor(String beanName) {
		this.beanName = beanName;
		setAdvice(new ExposeBeanNameInterceptor());
	}
	
	private class ExposeBeanNameInterceptor implements MethodInterceptor {
		
		public Object invoke(MethodInvocation mi) throws Throwable {
			ReflectiveMethodInvocation rmi = (ReflectiveMethodInvocation) mi;
			rmi.getUserAttributes().put(BEAN_NAME_ATTRIBUTE, beanName);
			return mi.proceed();
			
		}
	}

}
