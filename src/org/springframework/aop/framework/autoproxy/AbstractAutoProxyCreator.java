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

package org.springframework.aop.framework.autoproxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;

/**
 * BeanPostProcessor implementation that wraps a group of beans with AOP proxies
 * that delegate to the given interceptors before invoking the bean itself.
 *
 * <p>This class distinguishes between "common" interceptors: shared for all proxies it
 * creates, and "specific" interceptors: unique per bean instance. There need not
 * be any common interceptors. If there are, they are set using the interceptorNames
 * property. As with ProxyFactoryBean, interceptors names in the current factory
 * are used rather than bean references to allow correct handling of prototype
 * advisors and interceptors: for example, to support stateful mixins.
 * Any advice type is supported for "interceptorNames" entries.
 *
 * <p>Such auto-proxying is particularly useful if there's a large number of beans that need
 * to be wrapped with similar proxies, i.e. delegating to the same interceptors.
 * Instead of x repetitive proxy definitions for x target beans, you can register
 * one single such post processor with the bean factory to achieve the same effect.
 *
 * <p>Subclasses can apply any strategy to decide if a bean is to be proxied,
 * e.g. by type, by name, by definition details, etc. They can also return
 * additional interceptors that should just be applied to the specific bean
 * instance. The default concrete implementation is BeanNameAutoProxyCreator,
 * identifying the beans to be proxied via a list of bean names.
 *
 * <p>Any number of TargetSourceCreator implementations can be used with any subclass,
 * to create a custom target source - for example, to pool prototype objects.
 * Autoproxying will occur even if there is no advice if a TargetSourceCreator specifies
 * a custom TargetSource. If there are no TargetSourceCreators set, or if none matches,
 * a SingletonTargetSource will be used by default to wrap the bean to be autoproxied.
 *
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 13.10.2003
 * @see #setInterceptorNames
 * @see BeanNameAutoProxyCreator
 */
public abstract class AbstractAutoProxyCreator extends ProxyConfig
		implements InstantiationAwareBeanPostProcessor, BeanFactoryAware, Ordered {

	/**
	 * Convenience constant for subclasses: Return value for "do not proxy".
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object[] DO_NOT_PROXY = null;

	/**
	 * Convenience constant for subclasses: Return value for
	 * "proxy without additional interceptors, just the common ones".
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	protected static final Object[] PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS = new Object[0];


	/** Default value is same as non-ordered */
	private int order = Integer.MAX_VALUE;

	/** Default is global AdvisorAdapterRegistry */
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();

	/**
	 * Indicates whether or not the proxy should be frozen. Overridden from super to prevent the
	 * configuration from becoming frozen too early.
	 */
	private boolean freezeProxy;
	/**
	 * Names of common interceptors. We must use bean name rather than object references
	 * to handle prototype advisors/interceptors.
	 * Default is the empty array: no common interceptors.
	 */
	private String[] interceptorNames = new String[0];

	private boolean applyCommonInterceptorsFirst = true;

	private TargetSourceCreator[] customTargetSourceCreators;

	private BeanFactory beanFactory;


	/**
	 * Set the ordering which will apply to this class's implementation
	 * of Ordered, used when applying multiple BeanPostProcessors.
	 * Default value is Integer.MAX_VALUE, meaning that it's non-ordered.
	 * @param order ordering value
	 */
	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	public boolean isFrozen() {
		return this.freezeProxy;
	}

	/**
	 * Sets whether or not the proxy should be frozen, preventing advice from being added to it
	 * once it is created. Overridden from the super class to prevent the proxy configuration from
	 * being frozen before the proxy is created.
	 */
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}

	/**
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}

	/**
	 * Set custom TargetSourceCreators to be applied in this order.
	 * If the list is empty, or they all return null, a SingletonTargetSource
	 * will be created for each bean.
	 * <p>Note that TargetSourceCreators will kick in even for target beans
	 * where no advices or advisors have been found. If a TargetSourceCreator
	 * returns a TargetSource for a specific bean, that bean will be proxied
	 * in any case.
	 * <p>TargetSourceCreators can only be invoked if this post processor is used
	 * in a BeanFactory, and its BeanFactoryAware callback is used.
	 * @param targetSourceCreators list of TargetSourceCreator.
	 * Ordering is significant: The TargetSource returned from the first matching
	 * TargetSourceCreator (that is, the first that returns non-null) will be used.
	 */
	public void setCustomTargetSourceCreators(TargetSourceCreator[] targetSourceCreators) {
		this.customTargetSourceCreators = targetSourceCreators;
	}

	/**
	 * Set the common interceptors. These must be bean names in the current factory.
	 * They can be of any advice or advisor type Spring supports. If this property
	 * isn't set, there will be zero common interceptors. This is perfectly valid,
	 * if "specific" interceptors such as matching Advisors are all we want.
	 */
	public void setInterceptorNames(String[] interceptorNames) {
		this.interceptorNames = interceptorNames;
	}

	/**
	 * Set whether the common interceptors should be applied before bean-specific ones.
	 * Default is "true"; else, bean-specific interceptors will get applied first.
	 */
	public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
		this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Return the owning BeanFactory
	 * May be <code>null</code>, as this object doesn't need to belong to a bean factory.
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}


	public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
		if (isInfrastructureClass(beanClass, beanName)) {
			return null;
		}

		// Create proxy here if we have a custom TargetSource.
		// Suppresses unnecessary default instantiation of the target bean:
		// The TargetSource will handle target instances in a custom fashion.
		TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
		if (targetSource != null) {
			Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
			return createProxy(beanClass, beanName, specificInterceptors, targetSource);
		}

		return null;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	/**
	 * Create a proxy with the configured interceptors if the bean is
	 * identified as one to proxy by the subclass.
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (isInfrastructureClass(bean.getClass(), beanName)) {
			return bean;
		}

		// Create proxy if we have advice.
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != DO_NOT_PROXY) {
			return createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
		}

		return bean;
	}


	/**
	 * Return whether the given bean class and bean name represents an
	 * infrastructure class that should never be proxied.
	 * <p>Default implementation considers Advisors, MethodInterceptors
	 * and AbstractAutoProxyCreators as infrastructure classes, and
	 * consults the <code>shouldSkip</coide> method.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean
	 * @return whether the bean represents an infrastructure class
	 * @see org.springframework.aop.Advisor
	 * @see org.aopalliance.intercept.MethodInterceptor
	 * @see #shouldSkip
	 */
	protected boolean isInfrastructureClass(Class beanClass, String beanName) {
		boolean retVal = Advisor.class.isAssignableFrom(beanClass) ||
				MethodInterceptor.class.isAssignableFrom(beanClass) ||
				AbstractAutoProxyCreator.class.isAssignableFrom(beanClass) ||
				shouldSkip(beanClass, beanName);

		if (retVal && logger.isDebugEnabled()) {
			logger.debug("Did not attempt to autoproxy infrastructure class [" + beanClass.getName() + "]");
		}
		return retVal;
	}

	/**
	 * Subclasses should override this method to return true if this
	 * bean should not be considered for auto-proxying by this post processor.
	 * Sometimes we need to be able to avoid this happening if it will lead to
	 * a circular reference. This implementation returns <code>false</code>.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean
	 */
	protected boolean shouldSkip(Class beanClass, String beanName) {
		return false;
	}

	/**
	 * Create a target source for bean instances. Uses any TargetSourceCreators if set.
	 * Returns <code>null</code> if no custom TargetSource should be used.
	 * <p>This implementation uses the "customTargetSourceCreators" property.
	 * Subclasses can override this method to use a different mechanism.
	 * @param beanClass the class of the bean to create a TargetSource for
	 * @param beanName the name of the bean
	 * @return a TargetSource for this bean
	 * @see #setCustomTargetSourceCreators
	 */
	protected TargetSource getCustomTargetSource(Class beanClass, String beanName) {
		// We can't create fancy target sources for directly registered singletons.
		if (this.beanFactory != null && this.beanFactory.containsBean(beanName)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Checking for custom TargetSource for bean with name '" + beanName + "'");
			}
			if (this.customTargetSourceCreators != null) {
				for (int i = 0; i < this.customTargetSourceCreators.length; i++) {
					TargetSourceCreator tsc = this.customTargetSourceCreators[i];
					TargetSource ts = tsc.getTargetSource(beanClass, beanName);
					if (ts != null) {
						// Found a matching TargetSource.
						if (logger.isDebugEnabled()) {
							logger.debug("TargetSourceCreator [" + tsc +
									" found custom TargetSource for bean with name '" + beanName + "'");
						}
						return ts;
					}
				}
			}
		}

		// No custom TargetSource found.
		return null;
	}

	/**
	 * Create an AOP proxy for the given bean.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean
	 * @param specificInterceptors the set of interceptors that is
	 * specific to this bean (may be empty, but not null)
	 * @param targetSource the TargetSource for the proxy,
	 * already pre-configured to access the bean
	 * @return the AOP proxy for the bean
	 */
	protected Object createProxy(
			Class beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {

		// handle prototypes correctly
		Advisor[] commonInterceptors = resolveInterceptorNames();

		List allInterceptors = new ArrayList();
		if (specificInterceptors != null) {
			allInterceptors.addAll(Arrays.asList(specificInterceptors));
			if (commonInterceptors != null) {
				if (this.applyCommonInterceptorsFirst) {
					allInterceptors.addAll(0, Arrays.asList(commonInterceptors));
				}
				else {
					allInterceptors.addAll(Arrays.asList(commonInterceptors));
				}
			}
		}
		if (logger.isDebugEnabled()) {
			int nrOfCommonInterceptors = commonInterceptors != null ? commonInterceptors.length : 0;
			int nrOfSpecificInterceptors = specificInterceptors != null ? specificInterceptors.length : 0;
			logger.debug(
					"Creating implicit proxy for bean '" + beanName + "' with " + nrOfCommonInterceptors +
					" common interceptors and " + nrOfSpecificInterceptors + " specific interceptors");
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		// Copy our properties (proxyTargetClass) inherited from ProxyConfig.
		proxyFactory.copyFrom(this);

		if (!isProxyTargetClass()) {
			// Must allow for introductions; can't just set interfaces to
			// the target's interfaces only.
			Class[] targetsInterfaces = ClassUtils.getAllInterfacesForClass(beanClass);
			for (int i = 0; i < targetsInterfaces.length; i++) {
				proxyFactory.addInterface(targetsInterfaces[i]);
			}
		}

		for (Iterator it = allInterceptors.iterator(); it.hasNext();) {
			Advisor advisor = this.advisorAdapterRegistry.wrap(it.next());
			proxyFactory.addAdvisor(advisor);
		}
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		return proxyFactory.getProxy();
	}

	/**
	 * Resolves the specified interceptor names to Advisor objects.
	 * @see #setInterceptorNames
	 */
	private Advisor[] resolveInterceptorNames() {
		Advisor[] advisors = new Advisor[this.interceptorNames.length];
		for (int i = 0; i < this.interceptorNames.length; i++) {
			Object next = this.beanFactory.getBean(this.interceptorNames[i]);
			advisors[i] = this.advisorAdapterRegistry.wrap(next);
		}
		return advisors;
	}

	/**
	 * Subclasses may choose to implement this: for example,
	 * to change the interfaces exposed.
	 * <p>Default implementation is emty.
	 * @param proxyFactory ProxyFactory that is already configured with
	 * TargetSource and interfaces and will be used to create the proxy
	 * immediably after this method returns
	 */
	protected void customizeProxyFactory(ProxyFactory proxyFactory) {
	}


	/**
	 * Return whether the given bean is to be proxied, what additional
	 * advices (e.g. AOP Alliance interceptors) and advisors to apply.
	 * <p>The previous name of this method was "getInterceptorAndAdvisorForBean".
	 * It has been renamed in the course of general terminology clarification
	 * in Spring 1.1. An AOP Alliance Interceptor is just a special form of
	 * Advice, so the generic Advice term is preferred now.
	 * <p>The third parameter, customTargetSource, is new in Spring 1.1;
	 * add it to existing implementations of this method.
	 * @param beanClass the class of the bean to advise
	 * @param beanName the name of the bean
	 * @param customTargetSource targetSource returned by getTargetSource() method:
	 * may be ignored. Will be <code>null</code> unless a custom target source is in use.
	 * @return an array of additional interceptors for the particular bean;
	 * or an empty array if no additional interceptors but just the common ones;
	 * or <code>null</code> if no proxy at all, not even with the common interceptors.
	 * See constants DO_NOT_PROXY and PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS.
	 * @throws BeansException in case of errors
	 * @see #postProcessAfterInitialization
	 * @see #DO_NOT_PROXY
	 * @see #PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS
	 */
	protected abstract Object[] getAdvicesAndAdvisorsForBean(
			Class beanClass, String beanName, TargetSource customTargetSource) throws BeansException;

}
