/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.aspectj.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.reflect.PerClauseKind;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJProxyUtils;
import org.springframework.aop.framework.ProxyCreatorSupport;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * AspectJ-based proxy factory, allowing for programmatic building
 * of proxies which include AspectJ aspects (code style as well
 * Java 5 annotation style).
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #addAspect(Object)
 * @see #addAspect(Class)
 * @see #getProxy()
 * @see #getProxy(ClassLoader)
 * @see org.springframework.aop.framework.ProxyFactory
 */
public class AspectJProxyFactory extends ProxyCreatorSupport {

	/** Cache for singleton aspect instances */
	private static final Map aspectCache = new HashMap();

	private final AspectJAdvisorFactory aspectFactory = new ReflectiveAspectJAdvisorFactory();


	/**
	 * Create a new AspectJProxyFactory.
	 */
	public AspectJProxyFactory() {
	}

	/**
	 * Create a new AspectJProxyFactory.
	 * <p>Will proxy all interfaces that the given target implements.
	 * @param target the target object to be proxied
	 */
	public AspectJProxyFactory(Object target) {
		Assert.notNull(target, "Target object must not be null");
		setInterfaces(ClassUtils.getAllInterfaces(target));
		setTarget(target);
	}

	/**
	 * Create a new <code>AspectJProxyFactory</code>.
	 * No target, only interfaces. Must add interceptors.
	 */
	public AspectJProxyFactory(Class[] interfaces) {
		setInterfaces(interfaces);
	}


	/**
	 * Add the supplied aspect instance to the chain. The type of the aspect instance
	 * supplied must be a singleton aspect. True singleton lifecycle is not honoured when
	 * using this method - the caller is responsible for managing the lifecycle of any
	 * aspects added in this way.
	 */
	public void addAspect(Object aspect) {
		Class aspectType = aspect.getClass();
		String beanName = aspectType.getName();
		AspectMetadata am = createAspectMetadata(aspectType, beanName);
		if (am.getAjType().getPerClause().getKind() != PerClauseKind.SINGLETON) {
			throw new IllegalArgumentException("Aspect type '" + aspectType.getName() + "' is not a singleton aspect.");
		}
		MetadataAwareAspectInstanceFactory instanceFactory =
				new SingletonMetadataAwareAspectInstanceFactory(aspect, beanName);
		addAdvisorsFromAspectInstanceFactory(instanceFactory);
	}

	/**
	 * Add an aspect of the supplied type to the end of the advice chain.
	 */
	public void addAspect(Class aspectType) {
		String beanName = aspectType.getName();
		AspectMetadata am = createAspectMetadata(aspectType, beanName);
		MetadataAwareAspectInstanceFactory instanceFactory = createAspectInstanceFactory(am, aspectType, beanName);
		addAdvisorsFromAspectInstanceFactory(instanceFactory);
	}

	/**
	 * Add all {@link Advisor Advisors} from the supplied {@link MetadataAwareAspectInstanceFactory}
	 * to the current chain. Exposes any special purpose {@link Advisor Advisors} if needed.
	 * @see #makeAdvisorChainAspectJCapableIfNecessary()
	 */
	private void addAdvisorsFromAspectInstanceFactory(MetadataAwareAspectInstanceFactory instanceFactory) {
		List advisors = this.aspectFactory.getAdvisors(instanceFactory);
		this.addAllAdvisors((Advisor[]) advisors.toArray(new Advisor[advisors.size()]));
		makeAdvisorChainAspectJCapableIfNecessary();
	}

	/**
	 * Create an {@link AspectMetadata} instance for the supplied aspect type.
	 */
	private AspectMetadata createAspectMetadata(Class aspectType, String beanName) {
		AspectMetadata am = new AspectMetadata(aspectType, beanName);
		if (!am.getAjType().isAspect()) {
			throw new IllegalArgumentException("Class [" + aspectType.getName() + "] is not a valid aspect type");
		}
		return am;
	}

	/**
	 * Create a {@link MetadataAwareAspectInstanceFactory} for the supplied aspect type. If the aspect type
	 * has no per clause, then a {@link SingletonMetadataAwareAspectInstanceFactory} is returned, otherwise
	 * a {@link PrototypeAspectInstanceFactory} is returned.
	 */
	private MetadataAwareAspectInstanceFactory createAspectInstanceFactory(AspectMetadata am, Class aspectType, String beanName) {
		MetadataAwareAspectInstanceFactory instanceFactory = null;
		if (am.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
			Object instance = getSingletonAspectInstance(aspectType);
			instanceFactory = new SingletonMetadataAwareAspectInstanceFactory(instance, beanName);
		}
		else {
			// create BeanFactory for this aspect
			DefaultListableBeanFactory bf = getPrototypeAspectBeanFactory(aspectType, beanName);
			instanceFactory = new PrototypeAspectInstanceFactory(bf, beanName);
		}
		return instanceFactory;
	}

	/**
	 * Add any special-purpose {@link Advisor Advisors} needed for AspectJ support
	 * to the chain. {@link #updateAdvisorArray() Updates} the {@link Advisor} array
	 * and fires {@link #adviceChanged events}.
	 */
	private void makeAdvisorChainAspectJCapableIfNecessary() {
		if (AspectJProxyUtils.makeAdvisorChainAspectJCapableIfNecessary(getAdvisorsInternal())) {
			updateAdvisorArray();
			adviceChanged();
		}
	}

	/**
	 * Create a {@link DefaultListableBeanFactory} used to create prototype instances
	 * of the supplied aspect type.
	 */
	private DefaultListableBeanFactory getPrototypeAspectBeanFactory(Class aspectType, String beanName) {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		RootBeanDefinition definition = new RootBeanDefinition(aspectType);
		definition.setSingleton(false);
		bf.registerBeanDefinition(beanName, definition);
		return bf;
	}

	/**
	 * Get the singleton aspect instance for the supplied aspect type. An instance
	 * is created if one cannot be found in the instance cache.
	 */
	private Object getSingletonAspectInstance(Class aspectType) {
		synchronized (aspectCache) {
			Object instance = aspectCache.get(aspectType);
			if (instance != null) {
				return instance;
			}
			instance = BeanUtils.instantiateClass(aspectType);
			aspectCache.put(aspectType, instance);
			return instance;
		}
	}


	/**
	 * Create a new proxy according to the settings in this factory.
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * <p>Uses a default class loader: Usually, the thread context class loader
	 * (if necessary for proxy creation).
	 * @return the new proxy
	 */
	public <T> T getProxy() {
		return (T) createAopProxy().getProxy();
	}

	/**
	 * Create a new proxy according to the settings in this factory.
	 * <p>Can be called repeatedly. Effect will vary if we've added
	 * or removed interfaces. Can add and remove interceptors.
	 * <p>Uses the given class loader (if necessary for proxy creation).
	 * @param classLoader the class loader to create the proxy with
	 * @return the new proxy
	 */
	public <T> T getProxy(ClassLoader classLoader) {
		return (T) createAopProxy().getProxy(classLoader);
	}

}
