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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanCircularReferenceException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Abstract superclass for BeanFactory implementations, implementing the
 * ConfigurableBeanFactory SPI interface. Does <i>not</i> assume a listable
 * bean factory: can therefore also be used as base class for bean factory
 * implementations which fetch bean definitions from a variety of backend
 * resources (where bean definition access is an expensive operation).
 *
 * <p>This class provides singleton/prototype determination, singleton cache,
 * aliases, FactoryBean handling, bean definition merging for child bean definitions,
 * and bean destruction (DisposableBean interface, custom destroy methods).
 * Furthermore, it can manage a bean factory hierarchy, through implementing the
 * HierarchicalBeanFactory interface (superinterface of ConfigurableBeanFactory).
 *
 * <p>The main template methods to be implemented by subclasses are
 * <code>getBeanDefinition</code> and <code>createBean</code>, retrieving a bean
 * definition for a given bean name respectively creating a bean instance for a
 * given bean definition. Default implementations for those can be found in
 * DefaultListableBeanFactory respectively AbstractAutowireCapableBeanFactory.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 * @see #getBeanDefinition
 * @see #createBean
 * @see org.springframework.beans.factory.HierarchicalBeanFactory
 * @see org.springframework.beans.factory.DisposableBean
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see AbstractAutowireCapableBeanFactory#createBean
 * @see DefaultListableBeanFactory#getBeanDefinition
 */
public abstract class AbstractBeanFactory implements ConfigurableBeanFactory {

	/**
	 * Used to dereference a FactoryBean and distinguish it from beans
	 * <i>created</i> by the factory. For example, if the bean named
	 * <code>myEjb</code> is a factory, getting <code>&myEjb</code> will
	 * return the factory, not the instance returned by the factory.
	 */
	public static final String FACTORY_BEAN_PREFIX = "&";

	/**
	 * Marker object to be temporarily registered in the singleton cache
	 * while instantiating a bean, to be able to detect circular references.
	 */
	private static final Object CURRENTLY_IN_CREATION = new Object();

	static {
		// Eagerly load the DisposableBean and DestructionAwareBeanPostProcessor
		// classes to avoid weird classloader issues on application shutdown in
		// WebLogic 8.1. (Reported by Andreas Senft and Eric Ma.)
		DisposableBean.class.getName();
		DestructionAwareBeanPostProcessor.class.getName();
	}


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent bean factory, for bean inheritance support */
	private BeanFactory parentBeanFactory;

	/** Custom PropertyEditors to apply to the beans of this factory */
	private Map customEditors = new HashMap();

	/** Dependency types to ignore on dependency check and autowire */
	private final Set ignoreDependencyTypes = new HashSet();

	/** BeanPostProcessors to apply in createBean */
	private final List beanPostProcessors = new ArrayList();

	/** Map from alias to canonical bean name */
	private final Map aliasMap = Collections.synchronizedMap(new HashMap());

	/** Cache of singletons: bean name --> bean instance */
	private final Map singletonCache = Collections.synchronizedMap(new HashMap());

	/**
	 * Map that holds further beans created by this factory that implement
	 * the DisposableBean interface, to be destroyed on destroySingletons.
	 * Keyed by inner bean name.
	 * @see #destroySingletons
	 */
	private final Map disposableBeans = Collections.synchronizedMap(new HashMap());

	/**
	 * Map that holds dependent bean names for per bean name.
	 * @see #registerDependentBean
	 */
	private final Map dependentBeanMap = Collections.synchronizedMap(new HashMap());


	/**
	 * Create a new AbstractBeanFactory.
	 */
	public AbstractBeanFactory() {
		ignoreDependencyType(BeanFactory.class);
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this();
		this.parentBeanFactory = parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name name of the bean to retrieve
	 */
	public Object getBean(String name) throws BeansException {
		return getBean(name, (Object[]) null);
	}
		
	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name name of the bean to retrieve
	 * @param args arguments to use if creating a prototype using explicit arguments to a static
	 * factory method. It is invalid to use a non-null args value in any other case.
	 * TODO: We could consider supporting this for constructor args also, but it's really a
	 * corner case required for AspectJ integration.
	 */
	public Object getBean(String name, Object[] args) throws BeansException {
		String beanName = transformedBeanName(name);
		// eagerly check singleton cache for manually registered singletons
		Object sharedInstance = this.singletonCache.get(beanName);
		if (sharedInstance != null) {
			if (sharedInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName, "Requested bean is already currently in creation");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
			}
			return getObjectForSharedInstance(name, sharedInstance);
		}
		else {
			// check if bean definition exists
			RootBeanDefinition mergedBeanDefinition = null;
			try {
				mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// not found -> check parent
				if (this.parentBeanFactory != null) {
					return this.parentBeanFactory.getBean(name);
				}
				throw ex;
			}

			// check if bean definition is not abstract
			if (mergedBeanDefinition.isAbstract()) {
				throw new BeanIsAbstractException(name);
			}

			// Check validity of the usage of the args parameter. This can
			// only be used for prototypes constructed via a factory method.
			if (args != null) {
				if (mergedBeanDefinition.isSingleton()) {			
					throw new BeanDefinitionStoreException(
							"Cannot specify arguments in the getBean() method when referring to a singleton bean definition");
				}
				else if (mergedBeanDefinition.getFactoryMethodName() == null) {			
					throw new BeanDefinitionStoreException(
							"Can only specify arguments in the getBean() method in conjunction with a factory method");
				}
			}
			
			// create bean instance
			if (mergedBeanDefinition.isSingleton()) {
				synchronized (this.singletonCache) {
					// re-check singleton cache within synchronized block
					sharedInstance = this.singletonCache.get(beanName);
					if (sharedInstance == null) {
						if (logger.isInfoEnabled()) {
							logger.info("Creating shared instance of singleton bean '" + beanName + "'");
						}
						this.singletonCache.put(beanName, CURRENTLY_IN_CREATION);
						try {
							sharedInstance = createBean(beanName, mergedBeanDefinition, args);
							this.singletonCache.put(beanName, sharedInstance);
						}
						catch (BeansException ex) {
							this.singletonCache.remove(beanName);
							throw ex;
						}
					}
				}
				return getObjectForSharedInstance(name, sharedInstance);
			}
			else {
				// prototype
				return createBean(name, mergedBeanDefinition, args);
			}
		}
	}

	public Object getBean(String name, Class requiredType) throws BeansException {
		Object bean = getBean(name);
		if (!requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean);
		}
		return bean;
	}

	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		if (this.singletonCache.containsKey(beanName)) {
			return true;
		}
		if (containsBeanDefinition(beanName)) {
			return true;
		}
		else {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.containsBean(beanName);
			}
			else {
				return false;
			}
		}
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		try {
			Class beanClass = null;
			boolean singleton = true;
			Object beanInstance = this.singletonCache.get(beanName);
			if (beanInstance != null) {
				beanClass = beanInstance.getClass();
				singleton = true;
			}
			else {
				RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
				if (bd.hasBeanClass()) {
					beanClass = bd.getBeanClass();
				}
				singleton = bd.isSingleton();
			}
			// in case of FactoryBean, return singleton status of created object if not a dereference
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass) && !isFactoryDereference(name)) {
				FactoryBean factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.isSingleton();
			}
			else {
				return singleton;
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.isSingleton(beanName);
			}
			throw ex;
		}
	}

	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		// check if bean actually exists in this bean factory
		if (this.singletonCache.containsKey(beanName) || containsBeanDefinition(beanName)) {
			// if found, gather aliases
			List aliases = new ArrayList();
			synchronized (this.aliasMap) {
				for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (entry.getValue().equals(beanName)) {
						aliases.add(entry.getKey());
					}
				}
			}
			return (String[]) aliases.toArray(new String[aliases.size()]);
		}
		else {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.getAliases(beanName);
			}
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory interface
	//---------------------------------------------------------------------

	public void setParentBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}

	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		this.customEditors.put(requiredType, propertyEditor);
	}

	/**
	 * Return the map of custom editors, with Classes as keys
	 * and PropertyEditors as values.
	 */
	public Map getCustomEditors() {
		return customEditors;
	}

	public void ignoreDependencyType(Class type) {
		this.ignoreDependencyTypes.add(type);
	}

	/**
	 * Return the set of classes that will get ignored for autowiring.
	 */
	public Set getIgnoredDependencyTypes() {
		return ignoreDependencyTypes;
	}

	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		this.beanPostProcessors.add(beanPostProcessor);
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 */
	public List getBeanPostProcessors() {
		return beanPostProcessors;
	}

	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		if (logger.isDebugEnabled()) {
			logger.debug("Registering alias '" + alias + "' for bean with name '" + beanName + "'");
		}
		synchronized (this.aliasMap) {
			Object registeredName = this.aliasMap.get(alias);
			if (registeredName != null) {
				throw new BeanDefinitionStoreException("Cannot register alias '" + alias + "' for bean name '" + beanName +
						"': it's already registered for bean name '" + registeredName + "'");
			}
			this.aliasMap.put(alias, beanName);
		}
	}

	/**
	 * Initialize the given BeanWrapper with the custom editors registered
	 * with this factory.
	 * @param bw the BeanWrapper to initialize
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
			Class clazz = (Class) it.next();
			bw.registerCustomEditor(clazz, (PropertyEditor) this.customEditors.get(clazz));
		}
	}

	public void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException {
		synchronized (this.singletonCache) {
			Object oldObject = this.singletonCache.get(beanName);
			if (oldObject != null) {
				throw new BeanDefinitionStoreException("Could not register object [" + singletonObject +
						"] under bean name '" + beanName + "': there's already object [" + oldObject + " bound");
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
		this.singletonCache.put(beanName, singletonObject);
	}

	/**
	 * Remove the bean with the given name from the singleton cache of this factory.
	 * <p>To be able to clean up eager registration of a singleton if creation failed.
	 * @param beanName the name of the bean
	 */
	protected void removeSingleton(String beanName) {
		this.singletonCache.remove(beanName);
	}

	/**
	 * Return the names of beans in the singleton cache that match the given
	 * object type (including subclasses). Will <i>not</i> consider FactoryBeans
	 * as the type of their created objects is not known before instantiation.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 * @param type class or interface to match, or null for all bean names
	 * @return the names of beans in the singleton cache that match the given
	 * object type (including subclasses), or an empty array if none
	 */
	public String[] getSingletonNames(Class type) {
		synchronized (this.singletonCache) {
			Set matches = new HashSet();
			Iterator it = this.singletonCache.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				String beanName = (String) entry.getKey();
				Object bean = entry.getValue();
				if (type == null || type.isAssignableFrom(bean.getClass())) {
					matches.add(beanName);
				}
			}
			return (String[]) matches.toArray(new String[matches.size()]);
		}
	}

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 * @param beanName the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 */
	protected void registerDependentBean(String beanName, String dependentBeanName) {
		synchronized (this.dependentBeanMap) {
			List dependencies = (List) this.dependentBeanMap.get(beanName);
			if (dependencies == null) {
				dependencies = new LinkedList();
				this.dependentBeanMap.put(beanName, dependencies);
			}
			dependencies.add(dependentBeanName);
		}
	}

	/**
	 * Add the given bean to the list of further disposable beans in this factory.
	 * Typically used for inner beans which are not registered in the singleton cache.
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 */
	protected void addDisposableBean(String beanName, DisposableBean bean) {
		this.disposableBeans.put(beanName, bean);
	}

	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in factory {" + this + "}");
		}
		synchronized (this.singletonCache) {
			Set singletonCacheKeys = new HashSet(this.singletonCache.keySet());
			for (Iterator it = singletonCacheKeys.iterator(); it.hasNext();) {
				destroySingleton((String) it.next());
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Destroying inner beans in factory {" + this + "}");
		}
		synchronized (this.disposableBeans) {
			for (Iterator it = new HashSet(this.disposableBeans.keySet()).iterator(); it.hasNext();) {
				destroyDisposableBean((String) it.next());
			}
		}
	}

	/**
	 * Destroy the given bean. Delegates to destroyBean if a corresponding
	 * singleton instance is found.
	 * @param beanName name of the bean
	 * @see #destroyBean
	 */
	private void destroySingleton(String beanName) {
		Object singletonInstance = this.singletonCache.remove(beanName);
		if (singletonInstance != null) {
			destroyBean(beanName, singletonInstance);
		}
	}

	/**
	 * Fetch the given bean from the list of further DisposableBeans and destroy it.
	 * Delegates to destroyBean if a corresponding bean instance is found.
	 * @param beanName the name of the DisposableBean
	 * @see #destroyBean
	 */
	private void destroyDisposableBean(String beanName) {
		Object innerBeanInstance = this.disposableBeans.remove(beanName);
		if (innerBeanInstance != null) {
			destroyBean(beanName, innerBeanInstance);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Return whether this name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 */
	private boolean isFactoryDereference(String name) {
		return name.startsWith(FACTORY_BEAN_PREFIX);
	}

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary,
	 * and resolving aliases to canonical names.
	 */
	private String transformedBeanName(String name) throws NoSuchBeanDefinitionException {
		if (name == null) {
			throw new NoSuchBeanDefinitionException(name, "Cannot get bean with null name");
		}
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			name = name.substring(FACTORY_BEAN_PREFIX.length());
		}
		// handle aliasing
		String canonicalName = (String) this.aliasMap.get(name);
		return canonicalName != null ? canonicalName : name;
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * Will ask the parent bean factory if not found in this instance.
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, boolean includingAncestors)
	    throws BeansException {
		try {
			return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
		}
		catch (NoSuchBeanDefinitionException ex) {
			if (includingAncestors && getParentBeanFactory() instanceof AbstractBeanFactory) {
				return ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName, true);
			}
			else {
				throw ex;
			}
		}
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd) {
		if (bd instanceof RootBeanDefinition) {
			return (RootBeanDefinition) bd;
		}

		else if (bd instanceof ChildBeanDefinition) {
			ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
			RootBeanDefinition pbd = null;
			if (!beanName.equals(cbd.getParentName())) {
				pbd = getMergedBeanDefinition(cbd.getParentName(), true);
			}
			else {
				if (getParentBeanFactory() instanceof AbstractBeanFactory) {
					pbd = ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(cbd.getParentName(), true);
				}
				else {
					throw new NoSuchBeanDefinitionException(cbd.getParentName(),
							"Parent name '" + cbd.getParentName() + "' is equal to bean name '" + beanName +
							"' - cannot be resolved without an AbstractBeanFactory parent");
				}
			}

			// deep copy with overridden values
			RootBeanDefinition rbd = new RootBeanDefinition(pbd);
			rbd.overrideFrom(cbd);
			return rbd;
		}
		else {
			throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
					"Definition is neither a RootBeanDefinition nor a ChildBeanDefinition");
		}
	}

	/**
	 * Get the object for the given shared bean, either the bean
	 * instance itself or its created object in case of a FactoryBean.
	 * @param name name that may include factory dereference prefix
	 * @param beanInstance the shared bean instance
	 * @return the singleton instance of the bean
	 */
	protected Object getObjectForSharedInstance(String name, Object beanInstance) {
		String beanName = transformedBeanName(name);

		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory
		if (isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance);
		}

		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
		// If it's a FactoryBean, we use it to create a bean instance, unless the
		// caller actually wants a reference to the factory.
		if (beanInstance instanceof FactoryBean) {
			if (!isFactoryDereference(name)) {
				// return bean instance from factory
				FactoryBean factory = (FactoryBean) beanInstance;
				if (logger.isDebugEnabled()) {
					logger.debug("Bean with name '" + beanName + "' is a factory bean");
				}
				try {
					beanInstance = factory.getObject();
				}
				catch (Exception ex) {
					throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
				}
				if (beanInstance == null) {
					throw new FactoryBeanCircularReferenceException(
					    beanName, "FactoryBean returned null object: not fully initialized due to circular bean reference");
				}
			}
			else {
				// the user wants the factory itself
				if (logger.isDebugEnabled()) {
					logger.debug("Calling code asked for FactoryBean instance for name '" + beanName + "'");
				}
			}
		}

		return beanInstance;
	}

	/**
	 * Destroy the given bean. Must destroy beans that depend on the given
	 * bean before the bean itself. Should not throw any exceptions.
	 * @param beanName name of the bean
	 * @param bean the bean instance to destroy
	 */
	protected void destroyBean(String beanName, Object bean) {
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving dependent beans for bean '" + beanName + "'");
		}
		List dependencies = (List) this.dependentBeanMap.remove(beanName);
		if (dependencies != null) {
			for (Iterator it = dependencies.iterator(); it.hasNext();) {
				String dependentBeanName = (String) it.next();
				if (containsBean(dependentBeanName)) {
					// registered singleton
					destroySingleton(dependentBeanName);
				}
				else {
					// disposable inner bean
					destroyDisposableBean(dependentBeanName);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Applying DestructionAwareBeanPostProcessors to bean with name '" + beanName + "'");
		}
		for (int i = getBeanPostProcessors().size() - 1; i >= 0; i--) {
			Object beanProcessor = getBeanPostProcessors().get(i);
			if (beanProcessor instanceof DestructionAwareBeanPostProcessor) {
				((DestructionAwareBeanPostProcessor) beanProcessor).postProcessBeforeDestruction(bean, beanName);
			}
		}

		invokeDestroyMethods(beanName, bean);
	}

	/**
	 * Give a bean a chance to react to the shutdown of the bean factory.
	 * This means checking whether the bean implements DisposableBean or defines
	 * a custom destroy method, and invoking the necessary callback(s) if it does.
	 * <p>Called by destroyBean.
	 * @param beanName the bean has in the factory. Used for debug output.
	 * @param bean new bean instance we may need to notify of destruction
	 * @see #invokeCustomDestroyMethod
	 * @see #destroyBean
	 */
	protected void invokeDestroyMethods(String beanName, Object bean) {
		if (bean instanceof DisposableBean) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking destroy() on bean with name '" + beanName + "'");
			}
			try {
				((DisposableBean) bean).destroy();
			}
			catch (Throwable ex) {
				logger.error("destroy() on bean with name '" + beanName + "' threw an exception", ex);
			}
		}

		try {
			RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
			if (mergedBeanDefinition.getDestroyMethodName() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invoking custom destroy method '" + mergedBeanDefinition.getDestroyMethodName() +
							"' on bean with name '" + beanName + "'");
				}
				invokeCustomDestroyMethod(beanName, bean, mergedBeanDefinition.getDestroyMethodName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ignore: probably from a manually registered singleton.
		}
	}

	/**
	 * Invoke the specified custom destroy method on the given bean.
	 * Called by invokeDestroyMethods.
	 * <p>This implementation invokes a no-arg method if found, else checking
	 * for a method with a single boolean argument (passing in "true",
	 * assuming a "force" parameter), else logging an error.
	 * <p>Can be overridden in subclasses for custom resolution of destroy
	 * methods with arguments.
	 * @param beanName the bean has in the factory. Used for debug output.
	 * @param bean new bean instance we may need to notify of destruction
	 * @param destroyMethodName the name of the custom destroy method
	 * @see #invokeDestroyMethods
	 */
	protected void invokeCustomDestroyMethod(String beanName, Object bean, String destroyMethodName) {
		Method destroyMethod =
				BeanUtils.findDeclaredMethodWithMinimalParameters(bean.getClass(), destroyMethodName);
		if (destroyMethod == null) {
			logger.error("Couldn't find a destroy method named '" + destroyMethodName +
					"' on bean with name '" + beanName + "'");
		}
		else {
			Class[] paramTypes = destroyMethod.getParameterTypes();
			if (paramTypes.length > 1) {
				logger.error("Method '" + destroyMethodName + "' of bean '" + beanName +
						"' has more than one parameter - not supported as destroy method");
			}
			else if (paramTypes.length == 1 && !paramTypes[0].equals(boolean.class)) {
				logger.error("Method '" + destroyMethodName + "' of bean '" + beanName +
						"' has a non-boolean parameter - not supported as destroy method");
			}
			else {
				Object[] args = new Object[paramTypes.length];
				if (paramTypes.length == 1) {
					args[0] = Boolean.TRUE;
				}
				if (!Modifier.isPublic(destroyMethod.getModifiers())) {
					destroyMethod.setAccessible(true);
				}
				try {
					destroyMethod.invoke(bean, args);
				}
				catch (InvocationTargetException ex) {
					logger.error("Couldn't invoke destroy method '" + destroyMethodName +
							"' of bean with name '" + beanName + "'", ex.getTargetException());
				}
				catch (Throwable ex) {
					logger.error("Couldn't invoke destroy method '" + destroyMethodName +
							"' of bean with name '" + beanName + "'", ex);
				}
			}
		}
	}


	//---------------------------------------------------------------------
	// Abstract methods to be implemented by concrete subclasses
	//---------------------------------------------------------------------

	/**
	 * Check if this bean factory contains a bean definition with the given name.
	 * Does not consider any hierarchy this factory may participate in.
	 * Invoked by containsBean when no cached singleton instance is found.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 */
	protected abstract boolean containsBeanDefinition(String beanName);

	/**
	 * Return the bean definition for the given bean name.
	 * Subclasses should normally implement caching, as this method is invoked
	 * by this class every time bean definition metadata is needed.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * Create a bean instance for the given bean definition.
	 * The bean definition will already have been merged with the parent
	 * definition in case of a child definition.
	 * <p>All the other methods in this class invoke this method, although
	 * beans may be cached after being instantiated by this method. All bean
	 * instantiation within this class is performed by this method.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param args arguments to use if creating a prototype using explicit arguments
	 * to a static factory method. This parameter must be null except in this case.
	 * @return a new instance of the bean
	 * @throws BeansException in case of errors
	 */
	protected abstract Object createBean(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args) throws BeansException;

}
