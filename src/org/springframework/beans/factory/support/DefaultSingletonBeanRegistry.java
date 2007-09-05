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

package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.core.CollectionFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Generic registry for shared bean instances, implementing the
 * {@link org.springframework.beans.factory.config.SingletonBeanRegistry}.
 * Allows for registering singleton instances that should be shared
 * for all callers of the registry, to be obtained via bean name.
 *
 * <p>Also supports registration of
 * {@link org.springframework.beans.factory.DisposableBean} instances,
 * (which might or might not correspond to registered singletons),
 * to be destroyed on shutdown of the registry. Dependencies between
 * beans can be registered to enforce an appropriate shutdown order.
 *
 * <p>This class mainly serves as base class for
 * {@link org.springframework.beans.factory.BeanFactory} implementations,
 * factoring out the common management of singleton bean instances. Note that
 * the {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * interface extends the {@link SingletonBeanRegistry} interface.
 *
 * <p>Note that this class assumes neither a bean definition concept
 * nor a specific creation process for bean instances, in contrast to
 * {@link AbstractBeanFactory} and {@link DefaultListableBeanFactory}
 * (which inherit from it). Can alternatively also be used as a nested
 * helper to delegate to.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerSingleton
 * @see #registerDisposableBean
 * @see org.springframework.beans.factory.DisposableBean
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory
 */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

	/**
	 * Internal marker for a null singleton object:
	 * used as marker value for concurrent Maps (which don't support null values).
	 */
	private static final Object NULL_OBJECT = new Object();


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Cache of singleton objects: bean name --> bean instance */
	private final Map singletonObjects = CollectionFactory.createConcurrentMapIfPossible(16);

	/** Set of registered singletons, containing the bean names in registration order */
	private final Set registeredSingletons = CollectionFactory.createLinkedSetIfPossible(16);

	/** Names of beans that are currently in creation */
	private final Set singletonsCurrentlyInCreation = Collections.synchronizedSet(new HashSet());

	/** Flag that indicates whether we're currently within destroySingletons */
	private boolean singletonsCurrentlyInDestruction = false;

	/** Disposable bean instances: bean name --> disposable instance */
	private final Map disposableBeans = CollectionFactory.createLinkedMapIfPossible(16);

	/** Map between dependent bean names: bean name --> Set of dependent bean names */
	private final Map dependentBeanMap = new HashMap();


	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		Assert.notNull(beanName, "'beanName' must not be null");
		synchronized (this.singletonObjects) {
			Object oldObject = this.singletonObjects.get(beanName);
			if (oldObject != null) {
				throw new IllegalStateException("Could not register object [" + singletonObject +
						"] under bean name '" + beanName + "': there is already object [" + oldObject + "] bound");
			}
			addSingleton(beanName, singletonObject);
		}
	}

	/**
	 * Add the given singleton object to the singleton cache of this factory.
	 * <p>To be called for eager registration of singletons, e.g. to be able to
	 * resolve circular references.
	 * @param beanName the name of the bean
	 * @param singletonObject the singleton object
	 */
	protected void addSingleton(String beanName, Object singletonObject) {
		synchronized (this.singletonObjects) {
			this.singletonObjects.put(beanName, (singletonObject != null ? singletonObject : NULL_OBJECT));
			this.registeredSingletons.add(beanName);
		}
	}

	public Object getSingleton(String beanName) {
		Object singletonObject = this.singletonObjects.get(beanName);
		return (singletonObject != NULL_OBJECT ? singletonObject : null);
	}

	/**
	 * Return the (raw) singleton object registered under the given name,
	 * creating and registering a new one if none registered yet.
	 * @param beanName the name of the bean
	 * @param singletonFactory the ObjectFactory to lazily create the singleton
	 * with, if necessary
	 * @return the registered singleton object
	 */
	public Object getSingleton(String beanName, ObjectFactory singletonFactory) {
		Assert.notNull(beanName, "'beanName' must not be null");
		synchronized (this.singletonObjects) {
			// Re-check singleton cache within synchronized block.
			Object singletonObject = this.singletonObjects.get(beanName);
			if (singletonObject == null) {
				if (this.singletonsCurrentlyInDestruction) {
					throw new BeanCreationNotAllowedException(beanName,
							"Singleton bean creation not allowed while the singletons of this factory are in destruction " +
							"(Do not request a bean from a BeanFactory in a destroy method implementation!)");
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Creating shared instance of singleton bean '" + beanName + "'");
				}
				beforeSingletonCreation(beanName);
				try {
					singletonObject = singletonFactory.getObject();
				}
				finally {
					afterSingletonCreation(beanName);
				}
				addSingleton(beanName, singletonObject);
			}
			return (singletonObject != NULL_OBJECT ? singletonObject : null);
		}
	}

	/**
	 * Remove the bean with the given name from the singleton cache of this factory.
	 * <p>To be able to clean up eager registration of a singleton if creation failed.
	 * @param beanName the name of the bean
	 */
	protected void removeSingleton(String beanName) {
		this.singletonObjects.remove(beanName);
		this.registeredSingletons.remove(beanName);
	}

	public boolean containsSingleton(String beanName) {
		return (this.singletonObjects.containsKey(beanName));
	}

	public String[] getSingletonNames() {
		synchronized (this.singletonObjects) {
			return StringUtils.toStringArray(this.registeredSingletons);
		}
	}

	public int getSingletonCount() {
		synchronized (this.singletonObjects) {
			return this.registeredSingletons.size();
		}
	}


	/**
	 * Callback before singleton creation.
	 * <p>Default implementation register the singleton as currently in creation.
	 * @param beanName the name of the singleton about to be created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void beforeSingletonCreation(String beanName) {
		if (!this.singletonsCurrentlyInCreation.add(beanName)) {
			throw new BeanCurrentlyInCreationException(beanName);
		}
	}

	/**
	 * Callback after singleton creation.
	 * <p>Default implementation marks the singleton as not in creation anymore.
	 * @param beanName the name of the singleton that has been created
	 * @see #isSingletonCurrentlyInCreation
	 */
	protected void afterSingletonCreation(String beanName) {
		if (!this.singletonsCurrentlyInCreation.remove(beanName)) {
			throw new IllegalStateException("Singleton '" + beanName + "' isn't currently in creation");
		}
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

	/**
	 * Return the names of all beans which depend on the specified bean, if any.
	 * @param beanName the name of the bean
	 * @return the array of dependent bean names, or an empty array if none
	 */
	protected Set getDependentBeans(String beanName) {
		synchronized (this.dependentBeanMap) {
			return Collections.unmodifiableSet((Set) this.dependentBeanMap.get(beanName));
		}
	}

	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in " + this);
		}
		synchronized (this.singletonObjects) {
			this.singletonsCurrentlyInDestruction = true;
		}
		synchronized (this.disposableBeans) {
			String[] disposableBeanNames = StringUtils.toStringArray(this.disposableBeans.keySet());
			for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
				destroySingleton(disposableBeanNames[i]);
			}
		}
		synchronized (this.singletonObjects) {
			this.singletonObjects.clear();
			this.registeredSingletons.clear();
			this.singletonsCurrentlyInDestruction = false;
		}
	}

	/**
	 * Destroy the given bean. Delegates to <code>destroyBean</code>
	 * if a corresponding disposable bean instance is found.
	 * @param beanName the name of the bean
	 * @see #destroyBean
	 */
	public void destroySingleton(String beanName) {
		synchronized (this.singletonObjects) {
			// Remove a registered singleton of the given name, if any.
			removeSingleton(beanName);
		}

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

	/**
	 * Expose the singleton mutex to subclasses.
	 * <p>Subclasses should synchronize on the given Object if they perform
	 * any sort of extended singleton creation phase. In particular, subclasses
	 * should <i>not</i> have their own mutexes involved in singleton creation,
	 * to avoid the potential for deadlocks in lazy-init situations.
	 */
	protected final Object getSingletonMutex() {
		return this.singletonObjects;
	}

}
