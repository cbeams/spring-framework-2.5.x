/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * Default object instantiation strategy for use in BeanFactories. Uses CGLIB to generate subclasses
 * dynamically if methods need to be overridden by the container, to implement Method Injection.
 * Using Method Injection features requires CGLIB on the classpath. However, the core IoC container
 * will still run without CGLIB being available.
 * 
 * @author Rod Johnson
 * @version $Id: CglibSubclassingInstantiationStrategy.java,v 1.1 2004-06-23 21:12:24 johnsonr Exp $
 */
class CglibSubclassingInstantiationStrategy extends DefaultInstantiationStrategy {

	private final Log log = LogFactory.getLog(getClass());

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

	protected Object instantiateWithMethodInjection(RootBeanDefinition rbd, BeanFactory owner) {
		// Must generate CGLIB subclass
		return new CglibSubclassCreator(rbd, owner).instantiate();
	}

	protected Object instantiateWithMethodInjection(RootBeanDefinition rbd, BeanFactory owner, Constructor ctor,
			Object[] args) {
		return new CglibSubclassCreator(rbd, owner).instantiate(ctor, args);
	}

	/**
	 * An inner class so we don't have a CGLIB dependency in core
	 */
	private class CglibSubclassCreator {

		private RootBeanDefinition rbd;
		private BeanFactory owner;

		public CglibSubclassCreator(RootBeanDefinition rbd, BeanFactory owner) {
			this.rbd = rbd;
			this.owner = owner;
		}

		public Object instantiate() {
			Enhancer e = new Enhancer();
			e.setSuperclass(rbd.getBeanClass());
			e.setCallbackFilter(new CallbackFilterImpl());
			e.setCallbacks(new Callback[]{
					NoOp.INSTANCE, 
					new LookupOverrideMethodInterceptor()
			});

			return e.create();
		}

		public Object instantiate(Constructor ctor, Object[] args) {
			throw new UnsupportedOperationException("Method overriding not yet supported with Constructor Injection");
		}

		
		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with an implementation
		 * that returns a bean looked up in the container. 
		 */
		private class LookupOverrideMethodInterceptor implements MethodInterceptor {

			/**
			 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object,
			 *      java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
			 */
			public Object intercept(Object o, Method m, Object[] args, MethodProxy mp) throws Throwable {
				// Cast is safe as CallbackFilter filters are used selectively
				LookupOverride lo = (LookupOverride) rbd.getMethodOverrides().getOverride(m);
				return owner.getBean(lo.getBeanName());
			}
		}

		/**
		 * CGLIB object to filter method interception behaviour.
		 */
		private class CallbackFilterImpl implements CallbackFilter {

			/**
			 * @see net.sf.cglib.proxy.CallbackFilter#accept(java.lang.reflect.Method)
			 */
			public int accept(Method m) {
				MethodOverride methodOverride = rbd.getMethodOverrides().getOverride(m);
				log.info("Override for " + m.getName() + " is " + methodOverride);
				if (methodOverride == null) {
					return PASSTHROUGH;
				}
				else if (methodOverride instanceof LookupOverride) {
					return LOOKUP_OVERRIDE;
				}
				
				throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " + methodOverride.getClass());
			}
		}
	}

}