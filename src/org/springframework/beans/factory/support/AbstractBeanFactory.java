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

import java.beans.PropertyEditor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.core.CollectionFactory;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

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
 * <code>getBeanDefinition</code> and <code>createBean</code>, retrieving a
 * bean definition for a given bean name or creating a bean instance for a
 * given bean definition. Default implementations for those can be found in
 * DefaultListableBeanFactory or AbstractAutowireCapableBeanFactory, respectively.
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
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

	/** Parent bean factory, for bean inheritance support */
	private BeanFactory parentBeanFactory;

	/** ClassLoader to resolve bean names with, if necessary */
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	/** Whether to cache bean metadata or rather reobtain it for every access */
	private boolean cacheBeanMetadata = true;

	/** Custom PropertyEditors to apply to the beans of this factory */
	private final Map customEditors = new HashMap();

	/** Custom PropertyEditorRegistrars to apply to the beans of this factory */
	private final Set propertyEditorRegistrars = CollectionFactory.createLinkedSetIfPossible(16);

	/** BeanPostProcessors to apply in createBean */
	private final List beanPostProcessors = new ArrayList();

	/** Indicates whether any DestructionAwareBeanPostProcessors have been registered */
	private boolean hasDestructionAwareBeanPostProcessors;

	/** Map from scope identifier String to corresponding Scope */
	private final Map scopes = new HashMap();

	/** Map from alias to canonical bean name */
	private final Map aliasMap = new HashMap();

	/** Map from bean name to merged RootBeanDefinition */
	private final Map mergedBeanDefinitions = new HashMap();

	/** Names of beans that have already been created at least once */
	private final Set alreadyCreated = Collections.synchronizedSet(new HashSet());

	/** Names of beans that are currently in creation */
	private final ThreadLocal prototypesCurrentlyInCreation = new ThreadLocal();

	/** Cache of singleton objects created by FactoryBeans: FactoryBean name --> object */
	private final Map factoryBeanObjectCache = new HashMap();


	/**
	 * Create a new AbstractBeanFactory.
	 */
	public AbstractBeanFactory() {
	}

	/**
	 * Create a new AbstractBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or <code>null</code> if none
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
	 */
	public Object getBean(String name, Class requiredType, final Object[] args) throws BeansException {
		final String beanName = transformedBeanName(name);
		Object bean = null;

		// Eagerly check singleton cache for manually registered singletons.
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null) {
			if (isSingletonCurrentlyInCreation(beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
							"' that is not fully initialized yet - a consequence of a circular reference");
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			if (containsBeanDefinition(beanName)) {
				RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
				bean = getObjectForBeanInstance(sharedInstance, name, mergedBeanDefinition);
			}
			else {
				bean = getObjectForBeanInstance(sharedInstance, name, null);
			}
		}

		else {
			// Fail if we're already creating this bean instance:
			// We're assumably within a circular reference.
			if (isCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			// Check if bean definition exists in this factory.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (parentBeanFactory instanceof AbstractBeanFactory) {
					// Delegation to parent with args only possible for AbstractBeanFactory.
					return ((AbstractBeanFactory) parentBeanFactory).getBean(nameToLookup, requiredType, args);
				}
				else if (args == null) {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
				else {
					throw new NoSuchBeanDefinitionException(beanName,
							"Cannot delegate to parent BeanFactory because it does not supported passed-in arguments");
				}
			}

			this.alreadyCreated.add(beanName);

			final RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName, false);
			checkMergedBeanDefinition(mergedBeanDefinition, beanName, args);

			// Create bean instance.
			if (mergedBeanDefinition.isSingleton()) {
				sharedInstance = getSingleton(beanName, new ObjectFactory() {
					public Object getObject() throws BeansException {
						try {
							return createBean(beanName, mergedBeanDefinition, args);
						}
						catch (BeansException ex) {
							// Explicitly remove instance from singleton cache: It might have been put there
							// eagerly by the creation process, to allow for circular reference resolution.
							// Also remove any beans that received a temporary reference to the bean.
							destroySingleton(beanName);
							throw ex;
						}
					}
				});
				bean = getObjectForBeanInstance(sharedInstance, name, mergedBeanDefinition);
			}

			else if (mergedBeanDefinition.isPrototype()) {
				// It's a prototype -> create a new instance.
				Object prototypeInstance = null;
				try {
					beforePrototypeCreation(beanName);
					prototypeInstance = createBean(beanName, mergedBeanDefinition, args);
				}
				finally {
					afterPrototypeCreation(beanName);
				}
				bean = getObjectForBeanInstance(prototypeInstance, name, mergedBeanDefinition);
			}

			else {
				String scopeName = mergedBeanDefinition.getScope();
				final Scope scope = (Scope) this.scopes.get(scopeName);
				if (scope == null) {
					throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
				}
				try {
					Object scopedInstance = scope.get(beanName, new ObjectFactory() {
						public Object getObject() throws BeansException {
							beforePrototypeCreation(beanName);
							try {
								Object bean = createBean(beanName, mergedBeanDefinition, args);
								if (requiresDestruction(bean, mergedBeanDefinition)) {
									scope.registerDestructionCallback(beanName,
											new DisposableBeanAdapter(bean, beanName, mergedBeanDefinition, getBeanPostProcessors()));
								}
								return bean;
							}
							finally {
								afterPrototypeCreation(beanName);
							}
						}
					});
					bean = getObjectForBeanInstance(scopedInstance, name, mergedBeanDefinition);
				}
				catch (IllegalStateException ex) {
					throw new BeanCreationException(beanName,
							"Scope '" + scopeName + "' is not active for the current thread; " +
							"consider defining a scoped proxy for this bean if you intend to refer to it from a singleton",
							ex);
				}
			}
		}

		// Check if required type matches the type of the actual bean instance.
		if (requiredType != null && !requiredType.isAssignableFrom(bean.getClass())) {
			throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
		}
		return bean;
	}

	public boolean containsBean(String name) {
		if (containsLocalBean(name)) {
			return true;
		}
		// Not found -> check parent.
		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null) {
			return parentBeanFactory.containsBean(originalBeanName(name));
		}
		return false;
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return ((FactoryBean) beanInstance).isSingleton();
			}
			else {
				return true;
			}
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.isSingleton(originalBeanName(name));
			}

			RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
			boolean singleton = bd.isSingleton();

			// In case of FactoryBean, return singleton status of created object if not a dereference.
			if (singleton && !BeanFactoryUtils.isFactoryDereference(name)) {
				Class beanClass = resolveBeanClass(bd, beanName);
				if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
					FactoryBean factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
					singleton = factoryBean.isSingleton();
				}
			}

			return singleton;
		}
	}

	public Class getType(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		// Check manually registered singletons.
		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return ((FactoryBean) beanInstance).getObjectType();
			}
			else {
				return beanInstance.getClass();
			}
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.getType(originalBeanName(name));
			}

			RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName, false);

			Class beanClass = null;
			// Delegate to getTypeForFactoryMethod in case of factory method.
			if (mergedBeanDefinition.getFactoryMethodName() != null) {
				beanClass = getTypeForFactoryMethod(name, mergedBeanDefinition);
			}
			else {
				beanClass = resolveBeanClass(mergedBeanDefinition, beanName);
			}

			// Check bean class whether we're dealing with a FactoryBean.
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass) &&
					!BeanFactoryUtils.isFactoryDereference(name)) {
				// If it's a FactoryBean, we want to look at what it creates, not the factory class.
				return getTypeForFactoryBean(beanName, mergedBeanDefinition);
			}

			return beanClass;
		}
	}

	public String[] getAliases(String name) {
		String beanName = transformedBeanName(name);
		List aliases = new ArrayList();
		boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
		String fullBeanName = beanName;
		if (factoryPrefix) {
			fullBeanName = FACTORY_BEAN_PREFIX + beanName;
		}
		if (!fullBeanName.equals(name)) {
			aliases.add(fullBeanName);
		}
		synchronized (this.aliasMap) {
			for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				if (entry.getValue().equals(beanName)) {
					String key = (factoryPrefix ? FACTORY_BEAN_PREFIX : "") + entry.getKey();
					if (!key.equals(name)) {
						aliases.add(key);
					}
				}
			}
		}
		if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null) {
				aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
			}
		}
		return StringUtils.toStringArray(aliases);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory interface
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
	}

	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return (containsSingleton(beanName) || containsBeanDefinition(beanName));
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory interface
	//---------------------------------------------------------------------

	public void setParentBeanFactory(BeanFactory parentBeanFactory) {
		if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
			throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
		}
		this.parentBeanFactory = parentBeanFactory;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
	}

	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

	public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
		this.cacheBeanMetadata = cacheBeanMetadata;
	}

	public boolean isCacheBeanMetadata() {
		return cacheBeanMetadata;
	}

	public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
		Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
		this.propertyEditorRegistrars.add(registrar);
	}

	/**
	 * Return the set of PropertyEditorRegistrars.
	 */
	public Set getPropertyEditorRegistrars() {
		return propertyEditorRegistrars;
	}

	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		Assert.notNull(requiredType, "Required type must not be null");
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
		if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
			this.hasDestructionAwareBeanPostProcessors = true;
		}
	}

	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 */
	public List getBeanPostProcessors() {
		return beanPostProcessors;
	}

	/**
	 * Return whether this factory holds a DestructionAwareBeanPostProcessor
	 * that will get applied to singleton beans on shutdown.
	 * @see #addBeanPostProcessor
	 * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor
	 */
	protected boolean hasDestructionAwareBeanPostProcessors() {
		return this.hasDestructionAwareBeanPostProcessors;
	}

	public void registerScope(String scopeName, Scope scope) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		Assert.notNull(scope, "Scope must not be null");
		if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
		}
		this.scopes.put(scopeName, scope);
	}

	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	public Scope getRegisteredScope(String scopeName) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		return (Scope) this.scopes.get(scopeName);
	}

	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		Assert.notNull(otherFactory, "BeanFactory must not be null");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
		if (otherFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
			this.customEditors.putAll(otherAbstractFactory.customEditors);
			this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
			this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
			this.hasDestructionAwareBeanPostProcessors = this.hasDestructionAwareBeanPostProcessors ||
					otherAbstractFactory.hasDestructionAwareBeanPostProcessors;
			this.scopes.putAll(otherAbstractFactory.scopes);
		}
	}

	public void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException {
		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.hasText(alias, "Alias must not be empty");
		if (!alias.equals(beanName)) {
			// Only actually register the alias if it is not equal to the bean name itself.
			if (logger.isDebugEnabled()) {
				logger.debug("Registering alias '" + alias + "' for bean with name '" + beanName + "'");
			}
			synchronized (this.aliasMap) {
				Object registeredName = this.aliasMap.get(alias);
				if (registeredName != null && !registeredName.equals(beanName)) {
					throw new BeanDefinitionStoreException("Cannot register alias '" + alias + "' for bean name '" +
							beanName + "': It's already registered for bean name '" + registeredName + "'.");
				}
				this.aliasMap.put(alias, beanName);
			}
		}
	}

	/**
	 * Callback before prototype creation.
	 * <p>Default implementation register the prototype as currently in creation.
	 * @param beanName the name of the prototype about to be created
	 * @see #isPrototypeCurrentlyInCreation
	 */
	private void beforePrototypeCreation(String beanName) {
		Set beanNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (beanNames == null) {
			beanNames = new HashSet();
			this.prototypesCurrentlyInCreation.set(beanNames);
		}
		beanNames.add(beanName);
	}

	/**
	 * Callback after prototype creation.
	 * <p>Default implementation marks the prototype as not in creation anymore.
	 * @param beanName the name of the prototype that has been created
	 * @see #isPrototypeCurrentlyInCreation
	 */
	private void afterPrototypeCreation(String beanName) {
		Set beanNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (beanNames != null) {
			beanNames.remove(beanName);
			if (beanNames.isEmpty()) {
				this.prototypesCurrentlyInCreation.set(null);
			}
		}
	}

	/**
	 * Return whether the specified prototype bean is currently in creation
	 * (within the current thread).
	 * @param beanName the name of the bean
	 */
	protected final boolean isPrototypeCurrentlyInCreation(String beanName) {
		Set beanNames = (Set) this.prototypesCurrentlyInCreation.get();
		if (beanNames != null) {
			return beanNames.contains(beanName);
		}
		return false;
	}

	public boolean isCurrentlyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName);
	}

	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getMergedBeanDefinition(beanName));
	}

	/**
	 * Destroy the given bean instance (usually a prototype instance
	 * obtained from this factory) according to the given bean definition.
	 * @param beanName the name of the bean definition
	 * @param beanInstance the bean instance to destroy
	 * @param mergedBeanDefinition the merged bean definition
	 */
	protected void destroyBean(String beanName, Object beanInstance, RootBeanDefinition mergedBeanDefinition) {
		new DisposableBeanAdapter(beanInstance, beanName, mergedBeanDefinition, getBeanPostProcessors()).destroy();
	}

	public void destroyScopedBean(String beanName) {
		RootBeanDefinition mergedBeanDefinition = getMergedBeanDefinition(beanName);
		if (mergedBeanDefinition.isSingleton() || mergedBeanDefinition.isPrototype()) {
			throw new IllegalArgumentException(
					"Bean name '" + beanName + "' does not correspond to an object in a Scope");
		}
		String scopeName = mergedBeanDefinition.getScope();
		Scope scope = (Scope) this.scopes.get(scopeName);
		if (scope == null) {
			throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mergedBeanDefinition);
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Return the bean name, stripping out the factory dereference prefix if necessary,
	 * and resolving aliases to canonical names.
	 */
	protected String transformedBeanName(String name) {
		String beanName = BeanFactoryUtils.transformedBeanName(name);
		// Handle aliasing.
		synchronized (this.aliasMap) {
			String canonicalName = (String) this.aliasMap.get(beanName);
			return (canonicalName != null ? canonicalName : beanName);
		}
	}

	/**
	 * Return the original bean name, resolving locally defined aliases to canonical names.
	 */
	protected String originalBeanName(String name) {
		String beanName = transformedBeanName(name);
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			beanName = FACTORY_BEAN_PREFIX + beanName;
		}
		return beanName;
	}

	/**
	 * Determine whether this given bean name is defines as an alias
	 * (as opposed to the name of an actual bean definition).
	 * @param beanName the bean name to check
	 */
	protected boolean isAlias(String beanName) {
		synchronized (this.aliasMap) {
			return this.aliasMap.containsKey(beanName);
		}
	}

	/**
	 * Initialize the given BeanWrapper with the custom editors registered
	 * with this factory. To be called for BeanWrappers that will create
	 * and populate bean instances.
	 * <p>The default implementation delegates to <code>registerCustomEditors</code>.
	 * Can be overridden in subclasses.
	 * @param bw the BeanWrapper to initialize
	 * @see #registerCustomEditors
	 */
	protected void initBeanWrapper(BeanWrapper bw) {
		registerCustomEditors(bw);
	}

	/**
	 * Initialize the given PropertyEditorRegistry with the custom editors
	 * registered with this BeanFactory.
	 * <p>To be called for BeanWrappers that will create and populate bean
	 * instances, and for SimpleTypeConverter used for constructor argument
	 * and factory method type conversion.
	 * @param registry the PropertyEditorRegistry to initialize
	 */
	protected void registerCustomEditors(PropertyEditorRegistry registry) {
		registry.registerCustomEditor(String[].class, new StringArrayPropertyEditor());
		for (Iterator it = getPropertyEditorRegistrars().iterator(); it.hasNext();) {
			PropertyEditorRegistrar registrar = (PropertyEditorRegistrar) it.next();
			registrar.registerCustomEditors(registry);
		}
		for (Iterator it = getCustomEditors().entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Class clazz = (Class) entry.getKey();
			PropertyEditor editor = (PropertyEditor) entry.getValue();
			registry.registerCustomEditor(clazz, editor);
		}
	}

	/**
	 * Convert the given value into the specified target type,
	 * using the specified BeanWrapper.
	 * @param converter the TypeConverter to work on
	 * @param value the original value
	 * @param targetType the target type to convert to
	 * (or <code>null</code> if not known, for example in case of a collection element)
	 * @return the converted value, matching the target type
	 * @throws org.springframework.beans.TypeMismatchException if type conversion failed
	 * @see org.springframework.beans.BeanWrapperImpl#convertIfNecessary(Object, Class)
	 */
	protected Object doTypeConversionIfNecessary(
			TypeConverter converter, Object value, Class targetType, MethodParameter methodParam)
			throws TypeMismatchException {

		// Synchronize if custom editors are registered.
		// Necessary because PropertyEditors are not thread-safe.
		if (!getCustomEditors().isEmpty()) {
			synchronized (getCustomEditors()) {
				return converter.convertIfNecessary(value, targetType, methodParam);
			}
		}
		else {
			return converter.convertIfNecessary(value, targetType, methodParam);
		}
	}


	/**
	 * Return a RootBeanDefinition for the given bean name,
	 * merging a child bean definition with its parent if necessary.
	 * @param name the name of the bean to retrieve the merged definition for
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @throws BeansException in case of errors
	 */
	public RootBeanDefinition getMergedBeanDefinition(String name) throws BeansException {
		return getMergedBeanDefinition(name, false);
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a
	 * child definition. Can ask the parent bean factory if not found in this instance.
	 * @param name the name of the bean to retrieve the merged definition for
	 * @param includingAncestors whether to ask the parent bean factory if not found
	 * in this instance
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String name, boolean includingAncestors)
	    throws BeansException {

		String beanName = transformedBeanName(name);

		// Efficiently check whether bean definition exists in this factory.
		if (includingAncestors && !containsBeanDefinition(beanName) &&
				getParentBeanFactory() instanceof AbstractBeanFactory) {
			return ((AbstractBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName, true);
		}

		// Resolve merged bean definition locally.
		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}

	/**
	 * Return a RootBeanDefinition for the given bean name, by merging with the
	 * parent if the given original bean definition is a child bean definition.
	 * @param beanName the name of the bean definition
	 * @param bd the original bean definition (Root/ChildBeanDefinition)
	 * @return a (potentially merged) RootBeanDefinition for the given bean
	 * @throws BeanDefinitionStoreException in case of an invalid bean definition
	 */
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd)
			throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = (RootBeanDefinition) this.mergedBeanDefinitions.get(beanName);
			if (mbd == null) {

				if (bd instanceof RootBeanDefinition) {
					// Use copy of given root bean definition.
					mbd = new RootBeanDefinition((RootBeanDefinition) bd);
				}

				else if (bd instanceof ChildBeanDefinition) {
					// Child bean definition: needs to be merged with parent.
					ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
					RootBeanDefinition pbd = null;
					try {
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
										"': cannot be resolved without an AbstractBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(cbd.getResourceDescription(), beanName,
								"Could not resolve parent bean definition '" + cbd.getParentName() + "'", ex);
					}

					// Deep copy with overridden values.
					mbd = new RootBeanDefinition(pbd);
					mbd.overrideFrom(cbd);
				}

				else {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName,
							"Definition is neither a RootBeanDefinition nor a ChildBeanDefinition");
				}

				// Only cache the merged bean definition if we're already about to create an
				// instance of the bean, or at least have already created an instance before.
				if (isCacheBeanMetadata() && this.alreadyCreated.contains(beanName)) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}

			return mbd;
		}
	}

	/**
	 * Check the given merged bean definition,
	 * potentially throwing validation exceptions.
	 * @param mergedBeanDefinition the bean definition to check
	 * @param beanName the name of the bean
	 * @param args the arguments for bean creation, if any
	 * @throws BeansException in case of validation failure
	 */
	protected void checkMergedBeanDefinition(RootBeanDefinition mergedBeanDefinition, String beanName, Object[] args)
			throws BeansException {

		// check if bean definition is not abstract
		if (mergedBeanDefinition.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
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
	 * Resolve the bean class for the specified bean definition,
	 * resolving a bean class name into a Class references (if necessary).
	 * @param mbd the merged bean definition to determine the class for
	 * @param beanName the name of the bean (for error handling purposes)
	 * @return the resolved bean class (or <code>null</code> if none)
	 * @throws CannotLoadBeanClassException if we failed to load the class
	 */
	protected Class resolveBeanClass(RootBeanDefinition mbd, String beanName) throws CannotLoadBeanClassException {
		if (mbd.hasBeanClass()) {
			return mbd.getBeanClass();
		}
		try {
			return mbd.resolveBeanClass(getBeanClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
		}
	}


	/**
	 * Get the object for the given bean instance, either the bean
	 * instance itself or its created object in case of a FactoryBean.
	 * @param beanInstance the shared bean instance
	 * @param name name that may include factory dereference prefix
	 * @param mbd the merged bean definition
	 * @return the object to expose for the bean
	 */
	protected Object getObjectForBeanInstance(Object beanInstance, String name, RootBeanDefinition mbd)
			throws BeansException {

		String beanName = transformedBeanName(name);

		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory.
		if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance.getClass());
		}

		boolean shared = (mbd == null || mbd.isSingleton());
		Object object = beanInstance;

		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
		// If it's a FactoryBean, we use it to create a bean instance, unless the
		// caller actually wants a reference to the factory.
		if (beanInstance instanceof FactoryBean) {
			if (!BeanFactoryUtils.isFactoryDereference(name)) {
				// Return bean instance from factory.
				FactoryBean factory = (FactoryBean) beanInstance;
				if (logger.isDebugEnabled()) {
					logger.debug("Bean with name '" + beanName + "' is a factory bean");
				}
				// Cache object obtained from FactoryBean if it is a singleton.
				if (shared && factory.isSingleton()) {
					synchronized (this.factoryBeanObjectCache) {
						object = this.factoryBeanObjectCache.get(beanName);
						if (object == null) {
							object = getObjectFromFactoryBean(factory, beanName, mbd);
							this.factoryBeanObjectCache.put(beanName, object);
						}
					}
				}
				else {
					object = getObjectFromFactoryBean(factory, beanName, mbd);
				}
			}
			else {
	 			// The user wants the factory itself.
				if (logger.isDebugEnabled()) {
					logger.debug("Calling code asked for FactoryBean instance for name '" + beanName + "'");
				}
			}
		}

		return object;
	}

	/**
	 * Obtain an object to expose from the given FactoryBean.
	 * @param factory the FactoryBean instance
	 * @param beanName the name of the bean
	 * @return the object obtained from the FactoryBean
	 * @throws BeanCreationException if FactoryBean object creation failed
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	private Object getObjectFromFactoryBean(FactoryBean factory, String beanName, RootBeanDefinition mbd)
			throws BeanCreationException {

		Object object;

		try {
			object = factory.getObject();
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}

		// Do not accept a null value for a FactoryBean that's not fully
		// initialized yet: Many FactoryBeans just return null then.
		if (object == null && isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(
					beanName, "FactoryBean which is currently in creation returned null from getObject");
		}

		if (object != null && (mbd == null || !mbd.isSynthetic())) {
			object = postProcessObjectFromFactoryBean(object, beanName);
		}

		return object;
	}

	/**
	 * Determine whether the bean with the given name is a FactoryBean.
	 * @param name the name of the bean to check
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName);
		if (beanInstance != null) {
			return (beanInstance instanceof FactoryBean);
		}

		// No singleton instance found -> check bean definition.
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof AbstractBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((AbstractBeanFactory) getParentBeanFactory()).isFactoryBean(name);
		}

		RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
		Class beanClass = resolveBeanClass(bd, beanName);
		return (FactoryBean.class.equals(beanClass));
	}


	/**
	 * Return whether the given bean name is already used within this factory,
	 * that is, whether there is a local bean registered under this name or
	 * an inner bean created with this name.
	 * @param beanName the name to check
	 */
	protected boolean isBeanNameUsed(String beanName) {
		return containsLocalBean(beanName) || hasDependentBean(beanName);
	}

	protected boolean requiresDestruction(Object bean, RootBeanDefinition mergedBeanDefinition) {
		return (bean instanceof DisposableBean || mergedBeanDefinition.getDestroyMethodName() != null ||
				hasDestructionAwareBeanPostProcessors());
	}

	/**
	 * Add the given bean to the list of disposable beans in this factory,
	 * registering its DisposableBean interface and/or the given destroy method
	 * to be called on factory shutdown (if applicable). Only applies to singletons.
	 * <p>Also registers bean as dependent on other beans, according to the
	 * "depends-on" configuration in the bean definition.
	 * @param beanName the name of the bean
	 * @param bean the bean instance
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @see RootBeanDefinition#isSingleton
	 * @see RootBeanDefinition#getDependsOn
	 * @see #registerDisposableBean
	 * @see #registerDependentBean
	 */
	protected void registerDisposableBeanIfNecessary(
			String beanName, Object bean, RootBeanDefinition mergedBeanDefinition) {

		if (mergedBeanDefinition.isSingleton() && requiresDestruction(bean, mergedBeanDefinition)) {
			// Register a DisposableBean implementation that performs all destruction
			// work for the given bean: DestructionAwareBeanPostProcessors,
			// DisposableBean interface, custom destroy method.
			registerDisposableBean(beanName,
					new DisposableBeanAdapter(bean, beanName, mergedBeanDefinition, getBeanPostProcessors()));

			// Register bean as dependent on other beans, if necessary,
			// for correct shutdown order.
			String[] dependsOn = mergedBeanDefinition.getDependsOn();
			if (dependsOn != null) {
				for (int i = 0; i < dependsOn.length; i++) {
					registerDependentBean(dependsOn[i], beanName);
				}
			}
		}
	}

	/**
	 * Overridden to clear FactoryBean object cache as well.
	 */
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		synchronized (this.factoryBeanObjectCache) {
			this.factoryBeanObjectCache.remove(beanName);
		}
	}


	//---------------------------------------------------------------------
	// Abstract methods to be implemented by subclasses
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
	 * to a static factory method. This parameter must be <code>null</code> except in this case.
	 * @return a new instance of the bean
	 * @throws BeanCreationException if the bean could not be created
	 */
	protected abstract Object createBean(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args) throws BeanCreationException;

	/**
	 * Determine the bean type for the given bean definition which is based on
	 * a factory method. Only called if there is no singleton instance registered
	 * for the target bean already.
	 * <p>Default implementation returns <code>null</code> to indicate that the
	 * type cannot be determined. Subclasses are encouraged to try to determine
	 * the actual return type here, matching their strategy of resolving
	 * factory methods in the <code>createBean</code> implementation.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @return the type for the bean if determinable, or <code>null</code> else
	 * @see #createBean
	 */
	protected Class getTypeForFactoryMethod(String beanName, RootBeanDefinition mergedBeanDefinition) {
		return null;
	}

	/**
	 * Determine the bean type for the given FactoryBean definition, as far as possible.
	 * Only called if there is no singleton instance registered for the target bean already.
	 * <p>Default implementation creates the FactoryBean via <code>getBean</code>
	 * to call its <code>getObjectType</code> method. Subclasses are encouraged to optimize
	 * this, typically by just instantiating the FactoryBean but not populating it yet,
	 * trying whether its <code>getObjectType</code> method already returns a type.
	 * If no type found, a full FactoryBean creation as performed by this implementation
	 * should be used as fallback.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @return the type for the bean if determinable, or <code>null</code> else
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 * @see #getBean(String)
	 */
	protected Class getTypeForFactoryBean(String beanName, RootBeanDefinition mergedBeanDefinition) {
		FactoryBean factoryBean = null;
		try {
			factoryBean = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
		}
		catch (BeanCreationException ex) {
			// Can only happen when getting a FactoryBean.
			logger.debug("Ignoring bean creation exception on FactoryBean type check", ex);
			return null;
		}
		try {
			return factoryBean.getObjectType();
		}
		catch (Throwable ex) {
			// Thrown from the FactoryBean's getObjectType implementation.
			logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " +
					"that it should return null if the type of its object cannot be determined yet", ex);
			return null;
		}
	}

	/**
	 * Post-process the given object that has been obtained from the FactoryBean.
	 * The resulting object will get exposed for bean references.
	 * <p>The default implementation simply returns the given object as-is.
	 * Subclasses may override this, for example, to apply post-processors.
	 * @param object the object obtained from the FactoryBean.
	 * @param beanName the name of the bean
	 * @return the object to expose
	 */
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return object;
	}

}
