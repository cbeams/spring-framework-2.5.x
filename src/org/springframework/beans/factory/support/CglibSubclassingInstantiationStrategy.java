/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;

/**
 * Default object instantiation strategy for use in BeanFactories.
 * Uses CGLIB to generate subclasses dynamically if methods need to be
 * overridden by the container, to implement Method Injection.
 *
 * <p>Using Method Injection features requires CGLIB on the classpath.
 * However, the core IoC container will still run without CGLIB being available.
 *
 * @author Rod Johnson
 * @version $Id: CglibSubclassingInstantiationStrategy.java,v 1.3 2004-06-24 08:43:53 jhoeller Exp $
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

	/**
	 * Index in the CGLIB callback array for passthrough behaviour,
	 * in which case the subclass won't override the original class.
	 */
	private static final int PASSTHROUGH = 0;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden to provide method lookup.
	 */
	private static final int LOOKUP_OVERRIDE = 1;


	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, BeanFactory owner) {
		// must generate CGLIB subclass
		return new CglibSubclassCreator(beanDefinition, owner).instantiate();
	}

	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, BeanFactory owner,
																									Constructor ctor, Object[] args) {
		return new CglibSubclassCreator(beanDefinition, owner).instantiate(ctor, args);
	}


	/**
	 * An inner class so we don't have a CGLIB dependency in core.
	 */
	private static class CglibSubclassCreator {

		private static final Log logger = LogFactory.getLog(CglibSubclassCreator.class);

		private RootBeanDefinition beanDefinition;

		private BeanFactory owner;

		public CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		public Object instantiate() {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(this.beanDefinition.getBeanClass());
			enhancer.setCallbackFilter(new CallbackFilterImpl());
			enhancer.setCallbacks(new Callback[] {
					NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor()
			});

			return enhancer.create();
		}

		public Object instantiate(Constructor ctor, Object[] args) {
			throw new UnsupportedOperationException("Method overriding not yet supported with Constructor Injection");
		}


		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with an
		 * implementation that returns a bean looked up in the container.
		 */
		private class LookupOverrideMethodInterceptor implements MethodInterceptor {

			public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
				// cast is safe as CallbackFilter filters are used selectively
				LookupOverride lo = (LookupOverride) beanDefinition.getMethodOverrides().getOverride(m);
				return owner.getBean(lo.getBeanName());
			}
		}


		/**
		 * CGLIB object to filter method interception behavior.
		 */
		private class CallbackFilterImpl implements CallbackFilter {

			public int accept(Method method) {
				MethodOverride methodOverride = beanDefinition.getMethodOverrides().getOverride(method);
				if (logger.isInfoEnabled()) {
					logger.info("Override for '" + method.getName() + "' is [" + methodOverride + "]");
				}
				if (methodOverride == null) {
					return PASSTHROUGH;
				}
				else if (methodOverride instanceof LookupOverride) {
					return LOOKUP_OVERRIDE;
				}
				throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " +
																								methodOverride.getClass().getName());
			}
		}
	}

}
