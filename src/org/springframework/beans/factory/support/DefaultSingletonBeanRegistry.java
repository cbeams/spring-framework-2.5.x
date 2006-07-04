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

package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Generic registry for shared bean instances. Allows for registering singleton
 * instances that should be shared for all callers of the registry,
 * to be obtained via bean name.
 *
 * <p>Also supports registering DisposableBean instances (which might or
 * might not correspond to registered singletons), to be destroyed on
 * shutdown of the registry. Dependencies between beans can be registered
 * to enforce an appropriate shutdown order.
 *
 * <p>This class mainly serves as base class for BeanFactory implementations,
 * factoring out the common management of singleton bean instances. Some
 * of its methods can also be found on the ConfigurableBeanFactory interface.
 *
 * <p>Note that this class assumes neither a bean definition concept
 * nor a specific creation process for bean instances, in contrast to
 * AbstractBeanFactory and DefaultListableBeanFactory (which inherit from it).
 * Can alternatively also be used as a nested helper to delegate to.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerSingleton
 * @see #registerDisposableBean
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 * @see AbstractBeanFactory
 * @see DefaultListableBeanFactory
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Cache of singletons: bean name --> bean instance */
	private final Map singletonCache = new HashMap();

	/** Names of beans that are currently in creation */
	private final Set singletonsCurrentlyInCreation = Collections.synchronizedSet(new HashSet());

	/** Disposable bean instances: bean name --> disposable instance */
	private final Map disposableBeans = CollectionFactory.createLinkedMapIfPossible(16);

	/** Map between dependent bean names: bean name --> dependent bean name */
	private final Map dependentBeanMap = new HashMap();


	public void registerSingleton(String beanName, Object sharedBean) throws IllegalStateException {
		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(sharedBean, "Singleton object must not be null");
		synchronized (this.singletonCache) {
			Object oldObject = this.singletonCache.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("Could not register object [" + sharedBean +
						"] under bean name '" + beanName + "': there's already object [" + oldObject + " bound");
			}
			addSingleton(beanName, sharedBean);
		}
	}

	/**
	 * Add the given singleton object to the singleton cache of this factory.
	 * <p>To be called for eager registration of singletons, e.g. to be able to
	 * resolve circular references.
	 * @param beanName the name of the bean
	 * @param sharedBean the singleton object
	 */
	protected void addSingleton(String beanName, Object sharedBean) {
		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(sharedBean, "Singleton object must not be null");
		synchronized (this.singletonCache) {
			this.singletonCache.put(beanName, sharedBean);
		}
	}

	public Object getSingleton(String beanName) {
		synchronized (this.singletonCache) {
			return this.singletonCache.get(beanName);
		}
	}

	/**
	 * Return the (raw) singleton object registered under the given name,
	 * creating and registering a new one if none registered yet.
	 * @param beanName the name of the bean
	 * @return the registered singleton object
	 */
	public Object getSingleton(String beanName, ObjectFactory singletonFactory) {
		synchronized (this.singletonCache) {
			// Re-check singleton cache within synchronized block.
			Object sharedInstance = this.singletonCache.get(beanName);
			if (sharedInstance == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}
				try {
					beforeSingletonCreation(beanName);
					sharedInstance = singletonFactory.getObject();
				}
				finally {
					afterSingletonCreation(beanName);
				}
				addSingleton(beanName, sharedInstance);
			}
			return sharedInstance;
		}
	}

	/**
	 * Remove the bean with the given name from the singleton cache of this factory.
	 * <p>To be able to clean up eager registration of a singleton if creation failed.
	 * @param beanName the name of the bean
	 */
	protected void removeSingleton(String beanName) {
		Assert.hasText(beanName, "Bean name must not be empty");
		synchronized (this.singletonCache) {
			this.singletonCache.remove(beanName);
		}
	}

	public boolean containsSingleton(String beanName) {
		Assert.hasText(beanName, "Bean name must not be empty");
		synchronized (this.singletonCache) {
			return this.singletonCache.containsKey(beanName);
		}
	}

	public String[] getSingletonNames() {
		synchronized (this.singletonCache) {
			return StringUtils.toStringArray(this.singletonCache.keySet());
		}
	}

	public int getSingletonCount() {
		synchronized (this.singletonCache) {
			return this.singletonCache.size();
		}
	}


	/**
	 * Callback before singleton creation.
	 * <p>Default implementation register the singleton as currently in creation.
	 * @param beanName the name of the singleton about to be created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void beforeSingletonCreation(String beanName) {
		this.singletonsCurrentlyInCreation.add(beanName);
	}

	/**
	 * Callback after singleton creation.
	 * <p>Default implementation marks the singleton as not in creation anymore.
	 * @param beanName the name of the singleton that has been created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void afterSingletonCreation(String beanName) {
		this.singletonsCurrentlyInCreation.remove(beanName);
	}

	/**
	 * Return whether the specified singleton bean is currently in creation
	 * (within the entire factory).
	 * @param beanName the name of the bean
	 */
	public final boolean isSingletonCurrentlyInCreation(String beanName) {
		return this.singletonsCurrentlyInCreation.contains(beanName);
	}


	/**
	 * Add the given bean to the list of disposable beans in this registry.
	 * Disposable beans usually correspond to registered singletons,
	 * matching the bean name but potentially being a different instance
	 * (for example, a DisposableBean adapter for a singleton that does not
	 * naturally implement Spring's DisposableBean interface).
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 */
	public void registerDisposableBean(String beanName, DisposableBean bean) {
		synchronized (this.disposableBeans) {
			this.disposableBeans.put(beanName, bean);
		}
	}

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 * @param beanName the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 */
	public void registerDependentBean(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			Set dependencies = (Set) this.dependentBeanMap.get(beanName);
			if (dependencies == null) {
				dependencies = CollectionFactory.createLinkedSetIfPossible(8);
				this.dependentBeanMap.put(beanName, dependencies);
			}
			dependencies.add(dependentBeanName);
		}
	}

	/**
	 * Return whether a dependent bean has been registered under the given name.
	 * @param beanName the name of the bean
	 */
	protected boolean hasDependentBean(String beanName) {
		synchronized (this.dependentBeanMap) {
			return this.dependentBeanMap.containsKey(beanName);
		}
	}

	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in {" + this + "}");
		}
		synchronized (this.singletonCache) {
			synchronized (this.disposableBeans) {
				String[] disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
				for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
					destroySingleton(disposableBeanNames[i]);
				}
			}
			this.singletonCache.clear();
		}
	}

	/**
	 * Destroy the given bean. Delegates to <code>destroyBean</code>
	 * if a corresponding disposable bean instance is found.
	 * @param beanName the name of the bean
	 * @see #destroyBean
	 */
	public void destroySingleton(String beanName) {
		// Remove a registered singleton of the given name, if any.
		removeSingleton(beanName);

		// Destroy the corresponding DisposableBean instance.
		DisposableBean disposableBean = null;
		synchronized (this.disposableBeans) {
			disposableBean = (DisposableBean) this.disposableBeans.remove(beanName);
		}
		destroyBean(beanName, disposableBean);
	}

	/**
	 * Destroy the given bean. Must destroy beans that depend on the given
	 * bean before the bean itself. Should not throw any exceptions.
	 * @param beanName the name of the bean
	 * @param bean the bean instance to destroy
	 */
	protected void destroyBean(String beanName, DisposableBean bean) {
		Set dependencies = null;
		synchronized (this.dependentBeanMap) {
			dependencies = (Set) this.dependentBeanMap.remove(beanName);
		}

		if (dependencies != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieved dependent beans for bean '" + beanName + "': " + dependencies);
			}
			for (Iterator it = dependencies.iterator(); it.hasNext();) {
				String dependentBeanName = (String) it.next();
				destroySingleton(dependentBeanName);
			}
		}

		if (bean != null) {
			try {
				bean.destroy();
			}
			catch (Throwable ex) {
				logger.error("Destroy method on bean with name '" + beanName + "' threw an exception", ex);
			}
		}
	}

}
