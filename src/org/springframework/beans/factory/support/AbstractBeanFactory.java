/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanCircularReferenceException;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.PropertyValuesProviderFactoryBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Abstract superclass that makes implementing a BeanFactory very easy.
 *
 * <p>This class uses the <b>Template Method</b> design pattern.
 * Subclasses must implement only the
 * <code>
 * getBeanDefinition(name)
 * </code>
 * method.
 *
 * <p>This class handles resolution of runtime bean references,
 * FactoryBean dereferencing, and management of collection properties.
 * It also allows for management of a bean factory hierarchy,
 * implementing the HierarchicalBeanFactory interface.
 *
 * @author Rod Johnson
 * @since 15 April 2001
 * @version $Id: AbstractBeanFactory.java,v 1.28 2003-11-28 21:09:22 jhoeller Exp $
 */
public abstract class AbstractBeanFactory implements HierarchicalBeanFactory, ConfigurableBeanFactory {

	/**
	 * Used to dereference a FactoryBean and distinguish it from
	 * beans <i>created</i> by the factory. For example,
	 * if the bean named <code>myEjb</code> is a factory, getting
	 * <code>&myEjb</code> will return the factory, not the instance
	 * returned by the factory.
	 */
	public static final String FACTORY_BEAN_PREFIX = "&";


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent bean factory, for bean inheritance support */
	private BeanFactory parentBeanFactory;

	/** BeanPostProcessors to apply on createBean */
	private final List beanPostProcessors = new ArrayList();

	/** Dependency types to ignore on dependency check and autowire */
	private final Set ignoreDependencyTypes = new HashSet();

	/** Cache of singletons: bean name --> bean instance */
	private final Map singletonCache = new HashMap();

	/** Map from alias to canonical bean name */
	private final Map aliasMap = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

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

	public BeanFactory getParentBeanFactory() {
		return parentBeanFactory;
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

	public void ignoreDependencyType(Class type) {
		this.ignoreDependencyTypes.add(type);
	}

	/**
	 * Return the set of classes that will get ignored for autowiring.
	 */
	public Set getIgnoredDependencyTypes() {
		return ignoreDependencyTypes;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory interface
	//---------------------------------------------------------------------

	/**
	 * Return the bean name, stripping out the factory deference prefix if necessary,
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
	 * Return whether this name is a factory dereference
	 * (beginning with the factory dereference prefix).
	 */
	private boolean isFactoryDereference(String name) {
		return name.startsWith(FACTORY_BEAN_PREFIX);
	}

	/**
	 * Return the bean with the given name,
	 * checking the parent bean factory if not found.
	 * @param name name of the bean to retrieve
	 */
	public Object getBean(String name) throws BeansException {
		String beanName = transformedBeanName(name);
		Object sharedInstance = this.singletonCache.get(beanName);
		if (sharedInstance != null) {
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
			// create bean instance
			if (mergedBeanDefinition.isSingleton()) {
				logger.info("Creating shared instance of singleton bean '" + beanName + "'");
				sharedInstance = createBean(beanName, mergedBeanDefinition);
				return getObjectForSharedInstance(name, sharedInstance);
			}
			else {
				return createBean(name, mergedBeanDefinition);
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

	public boolean containsBean(String name) throws BeansException {
		String beanName = transformedBeanName(name);
		if (this.singletonCache.containsKey(beanName)) {
			return true;
		}
		try {
			getBeanDefinition(beanName);
			return true;
		}
		catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.containsBean(beanName);
			}
			return false;
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
				beanClass = bd.getBeanClass();
				singleton = bd.isSingleton();
			}
			// in case of FactoryBean, return singleton status of created object if not a dereference
			if (FactoryBean.class.isAssignableFrom(beanClass) && !isFactoryDereference(name)) {
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
		try {
			// check if bean actually exists in this bean factory
			if (!singletonCache.containsKey(beanName)) {
				getBeanDefinition(beanName);
			}

			// if found, gather aliases
			List aliases = new ArrayList();
			for (Iterator it = this.aliasMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				if (entry.getValue().equals(beanName)) {
					aliases.add(entry.getKey());
				}
			}
			return (String[]) aliases.toArray(new String[aliases.size()]);
		}
		catch (NoSuchBeanDefinitionException ex) {
			// not found -> check parent
			if (this.parentBeanFactory != null) {
				return this.parentBeanFactory.getAliases(beanName);
			}
			throw ex;
		}
	}

	public void autowireExistingBean(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Just constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE allowed");
		}
		RootBeanDefinition bd = new RootBeanDefinition(existingBean.getClass(), null);
		bd.setAutowire(autowireMode);
		if (dependencyCheck) {
			bd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
		configureBean("(existing bean)", bd, new BeanWrapperImpl(existingBean));
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableBeanFactory interface
	//---------------------------------------------------------------------

	public PropertyValues getPropertyValues(String beanName) {
		return getBeanDefinition(beanName).getPropertyValues();
	}

	/**
	 * Register property value for a specific bean, overriding an existing value.
	 * If no previous value exists, a new one will be added.
	 * <p>This is intended for bean factory post processing, i.e. overriding
	 * certain property values after parsing the original bean definitions.
	 * @param beanName name of the bean
	 * @param pv property name and value
	 * @throws org.springframework.beans.BeansException if the property values of the specified bean are immutable
	 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
	 */
	public void overridePropertyValue(String beanName, PropertyValue pv) throws BeansException {
		AbstractBeanDefinition bd = getBeanDefinition(beanName);
		if (!(bd.getPropertyValues() instanceof MutablePropertyValues)) {
			throw new FatalBeanException("Cannot modify immutable property values for bean '" + beanName + "'");
		}
		MutablePropertyValues pvs = (MutablePropertyValues) bd.getPropertyValues();
		pvs.addPropertyValue(pv);
	}

	public void registerAlias(String beanName, String alias) throws BeansException {
		logger.debug("Registering alias '" + alias + "' for bean with name '" + beanName + "'");
		Object registeredName = this.aliasMap.get(alias);
		if (registeredName != null) {
			throw new FatalBeanException("Cannot register alias '" + alias + "' for bean name '" + beanName +
			                             "': it's already registered for bean name '" + registeredName + "'");
		}
		this.aliasMap.put(alias, beanName);
	}

	public void registerSingleton(String beanName, Object singletonObject) throws BeanDefinitionStoreException {
		Object oldObject = this.singletonCache.get(beanName);
		if (oldObject != null) {
			throw new BeanDefinitionStoreException("Could not register object [" + singletonObject +
			                                       "] under bean name '" + beanName + "': there's already object [" +
			                                       oldObject + " bound");
		}
		this.singletonCache.put(beanName, singletonObject);
	}

	public void destroySingletons() {
		if (logger.isInfoEnabled()) {
			logger.info("Destroying singletons in factory {" + this + "}");
		}
		Set singletonCacheKeys = new HashSet(this.singletonCache.keySet());
		for (Iterator it = singletonCacheKeys.iterator(); it.hasNext();) {
			destroyBean((String) it.next());
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

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
		Set keys = this.singletonCache.keySet();
		Set matches = new HashSet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			Object singletonObject = this.singletonCache.get(name);
			if (type == null || type.isAssignableFrom(singletonObject.getClass())) {
				matches.add(name);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	/**
	 * Return a RootBeanDefinition, even by traversing parent if the parameter is a child definition.
	 * Will ask the parent bean factory if not found in this instance.
	 * @return a merged RootBeanDefinition with overridden properties
	 */
	public RootBeanDefinition getMergedBeanDefinition(String beanName, boolean includingAncestors)
	    throws BeansException {
		try {
			AbstractBeanDefinition bd = getBeanDefinition(beanName);
			if (bd instanceof RootBeanDefinition) {
				return (RootBeanDefinition) bd;
			}
			else if (bd instanceof ChildBeanDefinition) {
				ChildBeanDefinition cbd = (ChildBeanDefinition) bd;
				// Deep copy
				RootBeanDefinition rbd = new RootBeanDefinition(getMergedBeanDefinition(cbd.getParentName(), true));
				// Override settings
				rbd.setSingleton(cbd.isSingleton());
				rbd.setLazyInit(cbd.isLazyInit());
				// Override properties
				for (int i = 0; i < cbd.getPropertyValues().getPropertyValues().length; i++) {
					rbd.addPropertyValue(cbd.getPropertyValues().getPropertyValues()[i]);
				}
				return rbd;
			}
			else {
				throw new FatalBeanException("BeanDefinition for '" + beanName +
				                             "' is neither a RootBeanDefinition or ChildBeanDefinition");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			if (includingAncestors && this.parentBeanFactory instanceof AbstractBeanFactory) {
				return ((AbstractBeanFactory) this.parentBeanFactory).getMergedBeanDefinition(beanName, true);
			}
			else {
				throw ex;
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
	protected Object getObjectForSharedInstance(String name, Object beanInstance) {
		String beanName = transformedBeanName(name);
		// Don't let calling code try to dereference the
		// bean factory if the bean isn't a factory
		if (isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(beanName, beanInstance);
		}

		// Now we have the bean instance, which may be a normal bean
		// or a FactoryBean. If it's a FactoryBean, we use it to
		// create a bean instance, unless the caller actually wants
		// a reference to the factory.
		if (beanInstance instanceof FactoryBean) {
			if (!isFactoryDereference(name)) {
				// configure and return new bean instance from factory
				FactoryBean factory = (FactoryBean) beanInstance;
				logger.debug("Bean with name '" + beanName + "' is a factory bean");
				try {
					beanInstance = factory.getObject();
				}
				catch (Exception ex) {
					throw new FatalBeanException("FactoryBean threw exception on object creation", ex);
				}

				if (beanInstance == null) {
					throw new FactoryBeanCircularReferenceException(
					    "Factory bean '" + beanName + "' returned null object - " +
					    "possible cause: not fully initialized due to circular bean reference");
				}
				// set pass-through properties
				if (factory instanceof PropertyValuesProviderFactoryBean) {
					PropertyValues pvs = ((PropertyValuesProviderFactoryBean) factory).getPropertyValues(beanName);
					if (pvs != null) {
						logger.debug("Applying pass-through properties to bean with name '" + beanName + "'");
						new BeanWrapperImpl(beanInstance).setPropertyValues(pvs);
					}
				}
			}
			else {
				// the user wants the factory itself
				logger.debug("Calling code asked for FactoryBean instance for name '" + beanName + "'");
			}
		}

		return beanInstance;
	}

	/**
	 * All the other methods in this class invoke this method, although beans may be cached
	 * after being instantiated by this method. All bean instantiation within this class is
	 * performed by this method.
	 * @param beanName name of the bean. Must be unique in the BeanFactory
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @return a new instance of the bean
	 */
	protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating instance of bean '" + beanName + "' with merged definition [" + mergedBeanDefinition + "]");
		}

		if (mergedBeanDefinition.getDependsOn() != null) {
			for (int i = 0; i < mergedBeanDefinition.getDependsOn().length; i++) {
				// guarantee initialization of beans that the current one depends on
				getBean(mergedBeanDefinition.getDependsOn()[i]);
			}
		}

		BeanWrapper instanceWrapper = null;
		if (mergedBeanDefinition.getAutowire() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
				mergedBeanDefinition.hasConstructorArgumentValues()) {
			instanceWrapper = autowireConstructor(beanName, mergedBeanDefinition);
		}
		else {
			instanceWrapper = new BeanWrapperImpl(mergedBeanDefinition.getBeanClass());
		}

		Object bean = instanceWrapper.getWrappedInstance();

		// Eagerly cache singletons to be able to resolve circular references
		// even when triggered by lifecycle interfaces like BeanFactoryAware.
		if (mergedBeanDefinition.isSingleton()) {
			this.singletonCache.put(beanName, bean);
		}

		configureBean(beanName, mergedBeanDefinition, instanceWrapper);
		callLifecycleMethodsIfNecessary(bean, beanName, mergedBeanDefinition, instanceWrapper);

		bean = applyBeanPostProcessors(bean, beanName);
		// re-cache the instance even if already eagerly cached in createBean,
		// as it could have been wrapped by a BeanPostProcessor
		if (mergedBeanDefinition.isSingleton()) {
			this.singletonCache.put(beanName, bean);
		}
		return bean;
	}

	/**
	 * "autowire constructor" (with constructor arguments by type) behaviour.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to PicoContainer's "Type 3 IoC" paradigm: In this mode, a Spring bean
	 * factory is able to host components that expect constructor-based dependency resolution.
	 * @param beanName name of the bean to autowire by type
	 * @param mergedBeanDefinition bean definition to update through autowiring
	 * @return BeanWrapper for the new instance
	 */
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mergedBeanDefinition) {
		ConstructorArgumentValues cargs = mergedBeanDefinition.getConstructorArgumentValues();
		ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();

		int minNrOfArgs = 0;
		if (cargs != null) {
			minNrOfArgs = cargs.getNrOfArguments();
			for (Iterator it = cargs.getIndexedArgumentValues().entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int index = ((Integer) entry.getKey()).intValue();
				if (index < 0) {
					throw new BeanDefinitionStoreException("Invalid constructor argument index: " + index);
				}
				if (index > minNrOfArgs) {
					minNrOfArgs = index + 1;
				}
				String argName = "constructor argument with index " + index;
				Object resolvedValue = resolveValueIfNecessary(beanName, argName, entry.getValue());
				resolvedValues.addIndexedArgumentValue(index, resolvedValue);
			}
			for (Iterator it = cargs.getGenericArgumentValues().iterator(); it.hasNext();) {
				Object value = it.next();
				String argName = "constructor argument";
				Object resolvedValue = resolveValueIfNecessary(beanName, argName, value);
				resolvedValues.addGenericArgumentValue(resolvedValue);
			}
		}

		Constructor[] constructors = mergedBeanDefinition.getBeanClass().getConstructors();
		if (constructors.length == 0) {
			throw new FatalBeanException("No public constructor in class [" + mergedBeanDefinition.getBeanClass() +
																	 "] of bean with name '" + beanName + "'");
		}
		Arrays.sort(constructors, new Comparator() {
			public int compare(Object o1, Object o2) {
				int c1pl = ((Constructor) o1).getParameterTypes().length;
				int c2pl = ((Constructor) o2).getParameterTypes().length;
				return (new Integer(c1pl)).compareTo(new Integer(c2pl)) * -1;
			}
		});

		BeanWrapperImpl bw = new BeanWrapperImpl();
		Constructor constructorToUse = null;
		Object[] argsToUse = null;
		int minTypeDiffWeight = Integer.MAX_VALUE;
		for (int i = 0; i < constructors.length; i++) {
			try {
				Constructor constructor = constructors[i];
				if (constructor.getParameterTypes().length < minNrOfArgs) {
					throw new BeanDefinitionStoreException(minNrOfArgs + " constructor arguments specified but no " +
																								 "matching constructor found in bean '" + beanName + "'");
				}
				Class[] argTypes = constructor.getParameterTypes();
				Object[] args = new Object[argTypes.length];
				for (int j = 0; j < argTypes.length; j++) {
					args[j] = resolvedValues.getArgumentValue(j, argTypes[j]);
					if (args[j] != null) {
						args[j] = bw.doTypeConversionIfNecessary(null, null, args[j], argTypes[j]);
					}
					else {
						if (mergedBeanDefinition.getAutowire() != RootBeanDefinition.AUTOWIRE_CONSTRUCTOR) {
							throw new UnsatisfiedDependencyException(beanName, j, argTypes[j]);
						}
						Map matchingBeans = findMatchingBeans(argTypes[j]);
						if (matchingBeans.size() != 1) {
							throw new UnsatisfiedDependencyException(beanName, j, argTypes[j],
									"There are " + matchingBeans.size() + " beans of type [" + argTypes[j] + "] for autowiring constructor. " +
									"There should have been 1 to be able to autowire constructor of bean '" + beanName + "'.");
						}
						args[j] = matchingBeans.values().iterator().next();
						logger.info("Autowiring by type from bean name '" + beanName +
												"' via constructor to bean named '" + matchingBeans.keySet().iterator().next() + "'");
					}
				}
				int typeDiffWeight = getTypeDifferenceWeight(argTypes, args);
				if (typeDiffWeight < minTypeDiffWeight) {
					constructorToUse = constructor;
					argsToUse = args;
					minTypeDiffWeight = typeDiffWeight;
				}
			}
			catch (BeansException ex) {
				if (i == constructors.length - 1 && constructorToUse == null) {
					// all constructors tried
					throw ex;
				}
				else {
					// swallow and try next constructor
					logger.debug("Ignoring constructor [" + constructors[i] + "] of bean '" + beanName +
					             "': could not satisfy dependencies", ex);
				}
			}
		}

		bw.setWrappedInstance(BeanUtils.instantiateClass(constructorToUse, argsToUse));
		logger.info("Bean '" + beanName + "' instantiated via constructor [" + constructorToUse + "]");
		return bw;
	}

	/**
	 * Determine a weight that represents the class hierarchy difference between types and
	 * arguments. A direct match, i.e. type Integer -> arg of class Integer, does not increase
	 * the result - all direct matches means weight 0. A match between type Object and arg of
	 * class Integer would increase the weight by 2, due to the superclass 2 steps up in the
	 * hierarchy (i.e. Object) being the last one that still matches the required type Object.
	 * Type Number and class Integer would increase the weight by 1 accordingly, due to the
	 * superclass 1 step up the hierarchy (i.e. Number) still matching the required type Number.
	 * Therefore, with an arg of type Integer, a constructor (Integer) would be preferred to a
	 * constructor (Number) which would in turn be preferred to a constructor (Object).
	 * All argument weights get accumulated.
	 * @param argTypes the argument types to match
	 * @param args the arguments to match
	 * @return the accumulated weight for all arguments
	 */
	private int getTypeDifferenceWeight(Class[] argTypes, Object[] args) {
		int result = 0;
		for (int i = 0; i < argTypes.length; i++) {
			if (argTypes[i].isInstance(args[i])) {
				Class superClass = args[i].getClass().getSuperclass();
				while (superClass != null) {
					if (argTypes[i].isAssignableFrom(superClass)) {
						result++;
						superClass = superClass.getSuperclass();
					}
					else {
						superClass = null;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Configure the bean instance in the given BeanWrapper.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param bw BeanWrapper with bean instance
	 */
	protected void configureBean(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
		// add property values based on autowire by name if it's applied
		if (mergedBeanDefinition.getAutowire() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
			autowireByName(beanName, mergedBeanDefinition, bw);
		}

		// add property values based on autowire by type if it's applied
		if (mergedBeanDefinition.getAutowire() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
			autowireByType(beanName, mergedBeanDefinition, bw);
		}

		dependencyCheck(beanName, mergedBeanDefinition, bw);
		applyPropertyValues(beanName, bw, mergedBeanDefinition.getPropertyValues());
	}

	/**
	 * Fills in any missing property values with references to
	 * other beans in this factory if autowire is set to "byName".
	 * @param beanName name of the bean we're wiring up.
	 * Useful for debugging messages; not used functionally.
	 * @param mergedBeanDefinition bean definition to update through autowiring
	 */
	protected void autowireByName(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
		String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, bw);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				mergedBeanDefinition.addPropertyValue(new PropertyValue(propertyName, bean));
				if (logger.isDebugEnabled()) {
					logger.debug("Added autowiring by name from bean name '" + beanName +
						"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Not autowiring property '" + propertyName + "' of bean '" + beanName +
											"' by name: no matching bean found");
				}
			}
		}
	}

	/**
	 * Abstract method defining "autowire by type" (bean properties by type) behaviour.
	 * <p>This is like PicoContainer default, in which there must be exactly one bean of the
	 * property type in the bean factory. This makes bean factories simple to configure for small
	 * namespaces, but doesn't work as well as standard Spring behaviour for bigger applications.
	 * @param beanName of the bean to autowire by type
	 * @param mergedBeanDefinition bean definition to update through autowiring
	 * @param instanceWrapper BeanWrapper from which we can obtain information about the bean
	 */
	protected void autowireByType(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper instanceWrapper) {
		String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, instanceWrapper);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			// Look for a matching type
			Class requiredType = instanceWrapper.getPropertyDescriptor(propertyName).getPropertyType();
			Map matchingBeans = findMatchingBeans(requiredType);
			if (matchingBeans.size() == 1) {
				mergedBeanDefinition.addPropertyValue(
				    new PropertyValue(propertyName, matchingBeans.values().iterator().next()));
				if (logger.isDebugEnabled()) {
					logger.debug("Autowiring by type from bean name '" + beanName +
											"' via property '" + propertyName + "' to bean named '" +
											matchingBeans.keySet().iterator().next() + "'");
				}
			}
			else if (matchingBeans.size() > 1) {
				throw new UnsatisfiedDependencyException(beanName, propertyName,
						"There are " + matchingBeans.size() + " beans of type [" + requiredType + "] for autowire by type. " +
						"There should have been 1 to be able to autowire property '" + propertyName + "' of bean '" + beanName + "'.");
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Not autowiring property '" + propertyName + "' of bean '" + beanName +
											"' by type: no matching bean found");
				}
			}
		}
	}

	/**
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName name of the bean
	 * @throws org.springframework.beans.factory.UnsatisfiedDependencyException
	 */
	protected void dependencyCheck(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw)
			throws UnsatisfiedDependencyException {
		int dependencyCheck = mergedBeanDefinition.getDependencyCheck();
		if (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_NONE)
			return;

		Set ignoreTypes = getIgnoredDependencyTypes();
		PropertyValues pvs = mergedBeanDefinition.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null &&
			    !ignoreTypes.contains(pds[i].getPropertyType()) &&
			    pvs.getPropertyValue(pds[i].getName()) == null) {
				boolean isSimple = BeanUtils.isSimpleProperty(pds[i].getPropertyType());
				boolean unsatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL) ||
					(isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
					(!isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(beanName, pds[i].getName());
				}
			}
		}
	}

	/**
	 * Return an array of object-type property names that are unsatisfied.
	 * These are probably unsatisfied references to other beans in the
	 * factory. Does not include simple properties like primitives or Strings.
	 * @return an array of object-type property names that are unsatisfied
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	protected String[] unsatisfiedObjectProperties(RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
		Set result = new TreeSet();
		Set ignoreTypes = getIgnoredDependencyTypes();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			String name = pds[i].getName();
			if (pds[i].getWriteMethod() != null &&
			    !BeanUtils.isSimpleProperty(pds[i].getPropertyType()) &&
			    !ignoreTypes.contains(pds[i].getPropertyType()) &&
			    mergedBeanDefinition.getPropertyValues().getPropertyValue(name) == null) {
				result.add(name);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Apply the given property values, resolving any runtime references
	 * to other beans in this bean factory.
	 * Must use deep copy, so we don't permanently modify this property
	 * @param beanName bean name passed for better exception information
	 * @param bw BeanWrapper wrapping the target object
	 * @param pvs new property values
	 */
	private void applyPropertyValues(String beanName, BeanWrapper bw, PropertyValues pvs) throws BeansException {
		if (pvs == null)
			return;

		MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
		PropertyValue[] pvals = deepCopy.getPropertyValues();

		for (int i = 0; i < pvals.length; i++) {
			String argName = "property '" + pvals[i].getName() + "'";
			PropertyValue pv = new PropertyValue(pvals[i].getName(), resolveValueIfNecessary(beanName, argName, pvals[i].getValue()));
			// Update mutable copy
			deepCopy.setPropertyValueAt(pv, i);
		}

		// Set our (possibly massaged) deepCopy
		try {
			bw.setPropertyValues(deepCopy);
		}
		catch (FatalBeanException ex) {
			// Improve the message by showing the context
			throw new FatalBeanException("Error setting property on bean '" + beanName + "'", ex);
		}
	}

	/**
	 * Given a PropertyValue, return a value, resolving any references to other
	 * beans in the factory if necessary. The value could be:
	 * <li>An ordinary object or null, in which case it's left alone
	 * <li>A RuntimeBeanReference, which must be resolved
	 * <li>A ManagedList. This is a special collection that may contain
	 * RuntimeBeanReferences that will need to be resolved.
	 * <li>A ManagedMap. In this case the value may be a reference that
	 * must be resolved.
	 * If the value is a simple object, but the property takes a Collection type,
	 * the value must be placed in a list.
	 */
	private Object resolveValueIfNecessary(String beanName, String argName, Object value) throws BeansException {
		// We must check each PropertyValue to see whether it
		// requires a runtime reference to another bean to be resolved.
		// If it does, we'll attempt to instantiate the bean and set the reference.
		if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(beanName, argName, ref);
		}
		else if (value instanceof ManagedList) {
			// Convert from managed list. This is a special container that
			// may contain runtime bean references.
			// May need to resolve references
			return resolveManagedList(beanName, argName, (ManagedList) value);
		}
		else if (value instanceof ManagedMap) {
			// Convert from managed map. This is a special container that
			// may contain runtime bean references as values.
			// May need to resolve references
			ManagedMap mm = (ManagedMap) value;
			return resolveManagedMap(beanName, argName, mm);
		}
		else {
			// No need to resolve value
			return value;
		}
	}

	/**
	 * Resolve a reference to another bean in the factory.
	 */
	private Object resolveReference(String beanName, String argName, RuntimeBeanReference ref) throws BeansException {
		try {
			// Try to resolve bean reference
			logger.debug("Resolving reference from '" + argName + "' in bean '" +
			             beanName + "' to bean '" + ref.getBeanName() + "'");
			Object bean = getBean(ref.getBeanName());
			// Create a new PropertyValue object holding the bean reference
			return bean;
		}
		catch (BeansException ex) {
			throw new FatalBeanException("Can't resolve reference to bean '" + ref.getBeanName() +
																	 "' while setting '" + argName + "' on bean '" + beanName + "'", ex);
		}
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	private List resolveManagedList(String beanName, String argName, ManagedList ml) throws BeansException {
		List resolved = new ArrayList();
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(resolveValueIfNecessary(beanName, argName + "[" + i + "]", ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the ManagedMap, resolve reference if necessary.
	 */
	private Map resolveManagedMap(String beanName, String argName, ManagedMap mm) throws BeansException {
		Map resolved = new HashMap();
		Iterator keys = mm.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			resolved.put(key, resolveValueIfNecessary(beanName, argName + "[" + key + "]", mm.get(key)));
		}
		return resolved;
	}

	/**
	 * Give a bean a chance to react now all its properties are set,
	 * and a chance to know about its owning bean factory (this object).
	 * This means checking whether the bean implements InitializingBean
	 * and/or BeanFactoryAware, and invoking the necessary callback(s) if it does.
	 * @param bean new bean instance we may need to initialize
	 * @param name the bean has in the factory. Used for debug output.
	 */
	private void callLifecycleMethodsIfNecessary(Object bean, String name, RootBeanDefinition rbd, BeanWrapper bw)
	    throws BeansException {

		if (bean instanceof BeanNameAware) {
			((BeanNameAware) bean).setBeanName(name);
		}

		if (bean instanceof InitializingBean) {
			logger.debug("Calling afterPropertiesSet() on bean with name '" + name + "'");
			try {
				((InitializingBean) bean).afterPropertiesSet();
			}
			catch (BeansException ex) {
				throw ex;
			}
			catch (Exception ex) {
				throw new FatalBeanException("afterPropertiesSet() on bean with name '" + name + "' threw exception", ex);
			}
		}

		if (rbd.getInitMethodName() != null) {
			logger.debug("Calling custom init method '" + rbd.getInitMethodName() + "' on bean with name '" + name + "'");
			bw.invoke(rbd.getInitMethodName(), null);
			// Can throw MethodInvocationException
		}

		if (bean instanceof BeanFactoryAware) {
			logger.debug("Calling setBeanFactory() on BeanFactoryAware bean with name '" + name + "'");
			((BeanFactoryAware) bean).setBeanFactory(this);
		}
	}

	/**
	 * Apply BeanPostProcessors to a new bean instance.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 */
	private Object applyBeanPostProcessors(Object bean, String name) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors on bean with name '" + name + "'");
		}
		Object result = bean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			result = beanProcessor.postProcessBean(result, name);
		}
		return result;
	}

	/**
	 * Destroy the bean with the given name. Only applicable for singletons.
	 * Will destroy beans that depend on the given bean before the bean itself.
	 */
	protected void destroyBean(String beanName) {
		Object bean = this.singletonCache.get(beanName);
		if (bean != null) {
			logger.debug("Retrieving depending beans for bean '" + beanName + "'");
			String[] dependingBeans = getDependingBeanNames(beanName);
			for (int i = 0; i < dependingBeans.length; i++) {
				destroyBean(dependingBeans[i]);
			}

			if (bean instanceof DisposableBean) {
				logger.debug("Calling destroy() on bean with name '" + beanName + "'");
				try {
					((DisposableBean) bean).destroy();
				}
				catch (Exception ex) {
					logger.error("destroy() on bean with name '" + beanName + "' threw an exception", ex);
				}
			}

			try {
				RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
				if (bd.getDestroyMethodName() != null) {
					logger.debug("Calling custom destroy method '" + bd.getDestroyMethodName() +
											 "' on bean with name '" + beanName + "'");
					BeanWrapper bw = new BeanWrapperImpl(bean);
					try {
						bw.invoke(bd.getDestroyMethodName(), null);
					}
					catch (MethodInvocationException ex) {
						logger.error(ex.getMessage(), ex.getRootCause());
					}
				}
			}
			catch (NoSuchBeanDefinitionException ex) {
				// ignore, from manually registered singleton
			}
			this.singletonCache.remove(beanName);
		}
	}


	//---------------------------------------------------------------------
	// Abstract methods to be implemented by concrete subclasses
	//---------------------------------------------------------------------

	/**
	 * Return the bean definition for the given bean name.
	 * Subclasses should normally implement caching, as this method is invoked
	 * by this class every time a bean is requested.
	 * @param beanName name of the bean to find a definition for
	 * @return the BeanDefinition for this prototype name. Must never return null.
	 * @throws NoSuchBeanDefinitionException if the bean definition cannot be resolved
	 * @throws BeansException in case of errors
	 */
	protected abstract AbstractBeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * Return the names of the beans that depend on the given bean.
	 * Called by destroyBean, to be able to destroy depending beans first.
	 * @param beanName name of the bean to find depending beans for
	 * @throws BeansException in case of errors
	 * @see #destroyBean
	 */
	protected abstract String[] getDependingBeanNames(String beanName) throws BeansException;

	/**
	 * Find bean instances that match the required type.
	 * <p>This method is unsupported in this class, and throws UnsupportedOperationException.
	 * Subclasses should override it if they can obtain information about bean names
	 * by type, as a ListableBeanFactory implementation can.
	 * @param requiredType the type of the beans to look up
	 * @return a Map of bean names and bean instances that match the required type
	 * @throws BeansException in case of errors
	 */
	protected Map findMatchingBeans(Class requiredType) throws BeansException {
		throw new UnsupportedOperationException("AbstractBeanFactory does not support autowiring by type");
	}

}
