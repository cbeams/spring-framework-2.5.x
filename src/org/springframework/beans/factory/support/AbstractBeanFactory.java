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
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
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
import org.springframework.util.Assert;

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
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 * @see #getBean
	 */
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBean(name, null, null);
	}
		
	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBean(name, requiredType, null);
	}

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name the name of the bean to retrieve
	 * @param args arguments to use if creating a prototype using explicit arguments to a
	 * static factory method. It is invalid to use a non-null args value in any other case.
	 */
	public Object getBean(String name, Object[] args) throws BeansException {
		return getBean(name, null, args);
	}

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name the name of the bean to retrieve
	 * @param requiredType the required type of the bean to retrieve
	 * @param args arguments to use if creating a prototype using explicit arguments to a
	 * static factory method. It is invalid to use a non-null args value in any other case.
	 * TODO: We could consider supporting this for constructor args also, but it's really a
	 * corner case required for AspectJ integration.
	 */
	public Object getBean(String name, Class requiredType, Object[] args) throws BeansException {
		String beanName = transformedBeanName(name);
		Object bean = null;

		// Eagerly check singleton cache for manually registered singletons.
		Object sharedInstance = this.singletonCache.get(beanName);
		if (sharedInstance != null) {
			if (sharedInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
			}
			bean = getObjectForSharedInstance(name, sharedInstance);
		}

		else {
			// Check if bean definition exists in this factory.
			RootBeanDefinition mergedBeanDefinition = null;
			try {
				mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Not found -> check parent.
				if (this.parentBeanFactory instanceof AbstractBeanFactory) {
					// Delegation to parent with args only possible for AbstractBeanFactory.
					return ((AbstractBeanFactory) this.parentBeanFactory).getBean(name, requiredType, args);
				}
				else if (this.parentBeanFactory != null && args == null) {
					// No args -> delegate to standard getBean method.
					return this.parentBeanFactory.getBean(name, requiredType);
				}
				throw ex;
			}

			checkMergedBeanDefinition(mergedBeanDefinition, beanName, requiredType, args);

			// Create bean instance.
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
				bean = getObjectForSharedInstance(name, sharedInstance);
			}
			else {
				// It's a prototype -> create a new instance.
				bean = createBean(name, mergedBeanDefinition, args);
			}
		}

		// Check if required type matches the type of the actual bean instance.
		if (requiredType != null && !requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
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
			// Not found -> check parent.
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.containsBean(name);
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
			if (beanInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName);
			}
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

			// In case of FactoryBean, return singleton status of created object if not a dereference.
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass) &&
					!isFactoryDereference(name)) {
				FactoryBean factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.isSingleton();
			}
			return singleton;
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Not found -> check parent.
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.isSingleton(name);
			}
			throw ex;
		}
	}

	public Class getType(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		try {
			Class beanClass = null;

			// Check manually registered singletons.
			Object beanInstance = this.singletonCache.get(beanName);
			if (beanInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName);
			}
			if (beanInstance != null) {
				beanClass = beanInstance.getClass();
			}
			
			else {
				// OK, let's assume it's a bean definition.
				RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName, false);

				// Return "undeterminable" for beans without class or with factory method.
				if (!mergedBeanDefinition.hasBeanClass() || mergedBeanDefinition.getFactoryMethodName() != null) {
					return null;
				}

				beanClass = mergedBeanDefinition.getBeanClass();
			}

			// Check bean class whether we're dealing with a FactoryBean.
			if (FactoryBean.class.isAssignableFrom(beanClass) && !isFactoryDereference(name)) {
				// If it's a FactoryBean, we want to look at what it creates, not the factory class.
				FactoryBean factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
				return factoryBean.getObjectType();
			}
			return beanClass;
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Not found -> check parent.
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.getType(name);
			}
			throw ex;
		}
	}

	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		// Check if bean actually exists in this bean factory.
		if (this.singletonCache.containsKey(beanName) || containsBeanDefinition(beanName)) {
			// If found, gather aliases.
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
			// Not found -> check parent.
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.getAliases(name);
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
		Assert.notNull(propertyEditor, "PropertyEditor must not be null");
		this.customEditors.put(requiredType, propertyEditor);
	}

	/**
	 * Return the map of custom editors, with Classes as keys
	 * and PropertyEditors as values.
	 */
	public Map getCustomEditors() {
		return customEditors;
	}

	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
		this.beanPostProcessors.add(beanPostProcessor);
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 */
	public List getBeanPostProcessors() {
		return beanPostProcessors;
	}


	/**
	 * Create a new BeanWrapper for the given bean instance.
	 * <p>Default implementation creates a BeanWrapperImpl.
	 * Can be overridden for custom BeanWrapper adaptations.
	 * @param beanInstance the bean instance to create the BeanWrapper for,
	 * or null for a BeanWrapper that does not yet contain a target object
	 * @return the BeanWrapper
	 */
	protected BeanWrapper createBeanWrapper(Object beanInstance) {
		return (beanInstance != null ? new BeanWrapperImpl(beanInstance) : new BeanWrapperImpl());
	}

	/**
	 * Initialize the given BeanWrapper with the custom editors registered
	 * with this factory. To be called for BeanWrappers that will create
	 * and populate bean instances.
	 * @param bw the BeanWrapper to initialize
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
			Class clazz = (Class) it.next();
			bw.registerCustomEditor(clazz, (PropertyEditor) this.customEditors.get(clazz));
		}
	}

	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		Assert.notNull(alias, "Alias must not be null");
		if (logger.isDebugEnabled()) {
			logger.debug("Registering alias '" + alias + "' for bean with name '" + beanName + "'");
		}
		synchronized (this.aliasMap) {
			Object registeredName = this.aliasMap.get(alias);
			if (registeredName != null) {
				throw new BeanDefinitionStoreException("Cannot register alias '" + alias + "' for bean name '" +
						beanName + "': it's already registered for bean name '" + registeredName + "'");
			}
			this.aliasMap.put(alias, beanName);
		}
	}

	public void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException {
		Assert.notNull(singletonObject, "Singleton object must not be null");
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
		Assert.notNull(singletonObject, "Singleton object must not be null");
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
	 * Return the names of beans in the singleton cache.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 */
	public String[] getSingletonNames() {
		return (String[]) this.singletonCache.keySet().toArray(new String[this.singletonCache.size()]);
	}

	/**
	 * Return the number of beans in the singleton cache.
	 * <p>Does not consider any hierarchy this factory may participate in.
	 */
	public int getSingletonCount() {
		return this.singletonCache.size();
	}

	/**
	 * Determine whether the bean with the given name is a FactoryBean.
	 * @param name the name of the bean to check
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		try {
			Object beanInstance = this.singletonCache.get(beanName);
			if (beanInstance == CURRENTLY_IN_CREATION) {
				throw new BeanCurrentlyInCreationException(beanName);
			}
			if (beanInstance != null) {
				return (beanInstance instanceof FactoryBean);
			}
			else {
				RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
				return (bd.hasBeanClass() && FactoryBean.class.equals(bd.getBeanClass()));
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Not found -> check parent.
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.isSingleton(name);
			}
			throw ex;
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
	 * <p>Note: This does not have to be called for beans that reside in the singleton
	 * cache! It just allows to register further beans for destruction on shutdown.
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 */
	protected void registerDisposableBean(String beanName, DisposableBean bean) {
		this.disposableBeans.put(beanName, bean);
	}

	/**
	 * Add the given bean to the list of further disposable beans in this factory,
	 * registering the given destroy method to be called on factory shutdown.
	 * Typically used for inner beans which are not registered in the singleton cache.
	 * <p>Note: This does not have to be called for beans that reside in the singleton
	 * cache! It just allows to register further beans for destruction on shutdown.
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 * @param destroyMethodName the name of the destroy method
	 */
	protected void registerDisposableBean(
			final String beanName, final Object bean, final String destroyMethodName) {
		// Register DisposableBean wrapper for inner bean, to be able to call the
		// custom destroy method on factory shutdown.
		registerDisposableBean(beanName, new DisposableBean() {
			public void destroy() {
				invokeCustomDestroyMethod(beanName, bean, destroyMethodName);
			}
		});
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

		if (logger.isDebugEnabled()) {
			logger.debug("Destroying inner beans in factory {" + this + "}");
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
		Object disposableBean = this.disposableBeans.remove(beanName);
		if (disposableBean != null) {
			destroyBean(beanName, disposableBean);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Return whether the given name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 * @see BeanFactory#FACTORY_BEAN_PREFIX
	 * @see org.springframework.beans.factory.BeanFactoryUtils#isFactoryDereference
	 */
	protected boolean isFactoryDereference(String name) {
		return BeanFactoryUtils.isFactoryDereference(name);
	}

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary,
	 * and resolving aliases to canonical names.
	 */
	protected String transformedBeanName(String name) {
		String beanName = BeanFactoryUtils.transformedBeanName(name);
		// handle aliasing
		String canonicalName = (String) this.aliasMap.get(beanName);
		return canonicalName != null ? canonicalName : beanName;
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a
	 * child definition. Can ask the parent bean factory if not found in this instance.
	 * @param beanName the name of the bean definition
	 * @param includingAncestors whether to ask the parent bean factory if not found
	 * in this instance
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
	 * Return a RootBeanDefinition for the given bean name, by merging with the
	 * parent if the given original bean definition is a child bean definition.
	 * @param beanName the name of the bean definition
	 * @param bd the original bean definition (Root/ChildBeanDefinition)
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeansException {

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
					AbstractBeanFactory parentFactory = (AbstractBeanFactory) getParentBeanFactory();
					pbd = parentFactory.getMergedBeanDefinition(cbd.getParentName(), true);
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
	 * Check the given merged bean definition,
	 * potentially throwing validation exceptions.
	 * @param mergedBeanDefinition the bean definition to check
	 * @param beanName the name of the bean
	 * @param requiredType the required type of the bean
	 * @param args the arguments for bean creation, if any
	 * @throws BeansException in case of validation failure
	 */
	protected void checkMergedBeanDefinition(
			RootBeanDefinition mergedBeanDefinition, String beanName, Class requiredType, Object[] args)
			throws BeansException {

		// check if bean definition is not abstract
		if (mergedBeanDefinition.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
		}

		// Check if required type can match according to the bean definition.
		// This is only possible at this early stage for conventional beans!
		if (mergedBeanDefinition.hasBeanClass()) {
			Class beanClass = mergedBeanDefinition.getBeanClass();
			if (requiredType != null && mergedBeanDefinition.getFactoryMethodName() == null &&
					!FactoryBean.class.isAssignableFrom(beanClass) && !requiredType.isAssignableFrom(beanClass)) {
				throw new BeanNotOfRequiredTypeException(beanName, requiredType, beanClass);
			}
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
	}

	/**
	 * Get the object for the given shared bean, either the bean
	 * instance itself or its created object in case of a FactoryBean.
	 * @param name name that may include factory dereference prefix
	 * @param beanInstance the shared bean instance
	 * @return the singleton instance of the bean
	 */
	protected Object getObjectForSharedInstance(String name, Object beanInstance) throws BeansException {
		String beanName = transformedBeanName(name);

		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory.
		if (isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
		}

		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
		// If it's a FactoryBean, we use it to create a bean instance, unless the
		// caller actually wants a reference to the factory.
		if (beanInstance instanceof FactoryBean) {
			if (!isFactoryDereference(name)) {
				// Return bean instance from factory.
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
					    beanName, "FactoryBean returned null object: not fully initialized due to circular bean reference?");
				}
			}
			else {
	 			// The user wants the factory itself.
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
					// It's a registered singleton.
					destroySingleton(dependentBeanName);
				}
				else {
					// It's a disposable inner bean.
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
	 * Invoked by <code>containsBean</code> when no cached singleton instance is found.
	 * <p>Depending on the nature of the concrete bean factory implementation,
	 * this operation might be expensive (for example, because of directory lookups
	 * in external registries). However, for listable bean factories, this usually
	 * just amounts to a local hash lookup: The operation is therefore part of the
	 * public interface there. The same implementation can serve for both this
	 * template method and the public interface method in that case.
	 * @param beanName the name of the bean to look for
	 * @return if this bean factory contains a bean definition with the given name
	 * @see #containsBean
	 * @see org.springframework.beans.factory.ListableBeanFactory#containsBeanDefinition
	 */
	protected abstract boolean containsBeanDefinition(String beanName);

	/**
	 * Return the bean definition for the given bean name.
	 * Subclasses should normally implement caching, as this method is invoked
	 * by this class every time bean definition metadata is needed.
	 * <p>Depending on the nature of the concrete bean factory implementation,
	 * this operation might be expensive (for example, because of directory lookups
	 * in external registries). However, for listable bean factories, this usually
	 * just amounts to a local hash lookup: The operation is therefore part of the
	 * public interface there. The same implementation can serve for both this
	 * template method and the public interface method in that case.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory#getBeanDefinition
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
