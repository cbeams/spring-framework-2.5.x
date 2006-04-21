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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.StringUtils;

/**
 * Abstract BeanFactory superclass that implements default bean creation,
 * with the full capabilities specified by the RootBeanDefinition class.
 * Implements the AutowireCapableBeanFactory interface in addition to
 * AbstractBeanFactory's <code>createBean</code> method.
 *
 * <p>Provides bean creation (with constructor resolution), property population,
 * wiring (including autowiring), and initialization. Handles runtime bean
 * references, resolves managed collections, calls initialization methods, etc.
 * Supports autowiring constructors, properties by name, and properties by type.
 *
 * <p>The main template method to be implemented by subclasses is
 * <code>findMatchingBeans</code>, used for autowiring by type. In case of
 * a factory which is capable of searching its bean definitions, matching
 * beans will typically be implemented through such a search. For other
 * factory styles, simplified matching algorithms can be implemented.
 *
 * <p>Note that this class does <i>not</i> assume or implement bean definition
 * registry capabilities. See DefaultListableBeanFactory for an implementation
 * of the ListableBeanFactory and BeanDefinitionRegistry interfaces, which
 * represent the API (or SPI) view of such a factory.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 13.02.2004
 * @see AutowireCapableBeanFactory
 * @see AbstractBeanFactory#createBean
 * @see RootBeanDefinition
 * @see #findMatchingBeans(Class)
 * @see DefaultListableBeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
    implements AutowireCapableBeanFactory {

	private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

	/** Whether to automatically try to resolve circular references between beans */
	private boolean allowCircularReferences = true;

	/**
	 * Dependency types to ignore on dependency check and autowire, as Set of
	 * Class objects: for example, String. Default is none.
	 */
	private final Set ignoredDependencyTypes = new HashSet();

	/**
	 * Dependency interfaces to ignore on dependency check and autowire, as Set of
	 * Class objects. By default, only the BeanFactory interface is ignored.
	 */
	private final Set ignoredDependencyInterfaces = new HashSet();


	/**
	 * Create a new AbstractAutowireCapableBeanFactory.
	 */
	public AbstractAutowireCapableBeanFactory() {
		super();
		ignoreDependencyInterface(BeanFactoryAware.class);
	}

	/**
	 * Create a new AbstractAutowireCapableBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or <code>null</code> if none
	 */
	public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
		this();
		setParentBeanFactory(parentBeanFactory);
	}

	/**
	 * Set the instantiation strategy to use for creating bean instances.
	 * Default is CglibSubclassingInstantiationStrategy.
	 * @see CglibSubclassingInstantiationStrategy
	 */
	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	/**
	 * Return the current instantiation strategy.
	 */
	public InstantiationStrategy getInstantiationStrategy() {
		return instantiationStrategy;
	}

	/**
	 * Set whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 * <p>Note that circular reference resolution means that one of the involved beans
	 * will receive a reference to another bean that is not fully initialized yet.
	 * This can lead to subtle and not-so-subtle side effects on initialization;
	 * it does work fine for many scenarios, though.
	 * <p>Default is "true". Turn this off to throw an exception when encountering
	 * a circular reference, disallowing them completely.
	 */
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}

	/**
	 * Return whether to allow circular references between beans - and automatically
	 * try to resolve them.
	 */
	public boolean isAllowCircularReferences() {
		return allowCircularReferences;
	}

	/**
	 * Ignore the given dependency type for autowiring:
	 * for example, String. Default is none.
	 */
	public void ignoreDependencyType(Class type) {
		this.ignoredDependencyTypes.add(type);
	}

	/**
	 * Return the set of dependency types that will get ignored for autowiring.
	 * @return Set of Class objects
	 */
	public Set getIgnoredDependencyTypes() {
		return ignoredDependencyTypes;
	}

	/**
	 * Ignore the given dependency interface for autowiring.
	 * <p>This will typically be used by application contexts to register
	 * dependencies that are resolved in other ways, like BeanFactory through
	 * BeanFactoryAware or ApplicationContext through ApplicationContextAware.
	 * <p>By default, only the BeanFactoryAware interface is ignored.
	 * For further types to ignore, invoke this method for each type.
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	public void ignoreDependencyInterface(Class ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}

	/**
	 * Return the set of dependency interfaces that will get ignored for autowiring.
	 */
	public Set getIgnoredDependencyInterfaces() {
		return ignoredDependencyInterfaces;
	}


	//---------------------------------------------------------------------
	// Implementation of AutowireCapableBeanFactory interface
	//---------------------------------------------------------------------

	public Object autowire(Class beanClass, int autowireMode, boolean dependencyCheck)
			throws BeansException {

		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setSingleton(false);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd).getWrappedInstance();
		}
		else {
			Object bean = this.instantiationStrategy.instantiate(bd, null, this);
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}

	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {

		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Just constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE allowed");
		}
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(existingBean.getClass(), autowireMode, dependencyCheck);
		bd.setSingleton(false);
		populateBean(existingBean.getClass().getName(), bd, new BeanWrapperImpl(existingBean));
	}

	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
		RootBeanDefinition bd = getMergedBeanDefinition(beanName, true);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}

	/**
	 * Apply InstantiationAwareBeanPostProcessors to the specified bean definition
	 * (by class and name), invoking their <code>postProcessBeforeInstantiation</code> methods.
     * <p>Any returned object will be used as the bean instead of actually instantiating the target
	 * bean. A <code>null</code> return value from the post-processor will result in the target bean being
	 * instantiated.
	 * @param beanClass the class of the bean to be instantiated
	 * @param beanName the name of the bean
	 * @return the bean object to use instead of a default instance of the target bean, or <code>null</code>
	 * @throws BeansException if any post-processing failed
	 * @see InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation
	 */
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class beanClass, String beanName)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors before instantiation of bean '" + beanName + "'");
		}
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			if (beanProcessor instanceof InstantiationAwareBeanPostProcessor) {
				Object result = ((InstantiationAwareBeanPostProcessor) beanProcessor).postProcessBeforeInstantiation(
						beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors before initialization of bean '" + beanName + "'");
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			result = beanProcessor.postProcessBeforeInitialization(result, beanName);
			if (result == null) {
				throw new BeanCreationException(beanName,
						"postProcessBeforeInitialization method of BeanPostProcessor [" + beanProcessor +
						"] returned null for bean [" + result + "] with name [" + beanName + "]");
			}
		}
		return result;
	}

	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors after initialization of bean '" + beanName + "'");
		}
		Object result = existingBean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			result = beanProcessor.postProcessAfterInitialization(result, beanName);
			if (result == null) {
				throw new BeanCreationException(beanName,
						"postProcessAfterInitialization method of BeanPostProcessor [" + beanProcessor +
						"] returned null for bean [" + result + "] with name [" + beanName + "]");
			}
		}
		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of relevant AbstractBeanFactory template methods
	//---------------------------------------------------------------------

	/**
	 * Central method of this class: creates a bean instance,
	 * populates the bean instance, applies post-processors, etc.
	 * <p>Differentiates between default bean instantiation, use of a
	 * factory method, and autowiring a constructor.
	 * @see #instantiateBean
	 * @see #instantiateUsingFactoryMethod
	 * @see #autowireConstructor
	 */
	protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
			throws BeanCreationException {

		if (logger.isDebugEnabled()) {
			logger.debug("Creating instance of bean '" + beanName +
					"' with merged definition [" + mergedBeanDefinition + "]");
		}

		Object bean = null;

		// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
		if (mergedBeanDefinition.hasBeanClass()) {
			bean = applyBeanPostProcessorsBeforeInstantiation(mergedBeanDefinition.getBeanClass(), beanName);
			if (bean != null) {
				return bean;
			}
		}

		// Guarantee initialization of beans that the current one depends on.
		if (mergedBeanDefinition.getDependsOn() != null) {
			for (int i = 0; i < mergedBeanDefinition.getDependsOn().length; i++) {
				getBean(mergedBeanDefinition.getDependsOn()[i]);
			}
		}

		BeanWrapper instanceWrapper = null;
		Object originalBean = null;
		String errorMessage = null;

		try {
			// Instantiate the bean.
			errorMessage = "Instantiation of bean failed";

			if (mergedBeanDefinition.getFactoryMethodName() != null)  {
				instanceWrapper = instantiateUsingFactoryMethod(beanName, mergedBeanDefinition, args);
			}
			else if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
					mergedBeanDefinition.hasConstructorArgumentValues() )  {
				instanceWrapper = autowireConstructor(beanName, mergedBeanDefinition);
			}
			else {
				// No special handling: simply use no-arg constructor.
				instanceWrapper = instantiateBean(beanName, mergedBeanDefinition);
			}
			bean = instanceWrapper.getWrappedInstance();

			// Eagerly cache singletons to be able to resolve circular references
			// even when triggered by lifecycle interfaces like BeanFactoryAware.
			if (isAllowCircularReferences() && isSingletonCurrentlyInCreation(beanName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Eagerly caching bean with name '" + beanName +
							"' to allow for resolving potential circular references");
				}
				addSingleton(beanName, bean);
			}

			// Initialize the bean instance.
			errorMessage = "Initialization of bean failed";
			populateBean(beanName, mergedBeanDefinition, instanceWrapper);

			if (bean instanceof BeanNameAware) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invoking setBeanName on BeanNameAware bean '" + beanName + "'");
				}
				((BeanNameAware) bean).setBeanName(beanName);
			}

			if (bean instanceof BeanFactoryAware) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invoking setBeanFactory on BeanFactoryAware bean '" + beanName + "'");
				}
				((BeanFactoryAware) bean).setBeanFactory(this);
			}

			originalBean = bean;
			bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
			invokeInitMethods(beanName, bean, mergedBeanDefinition);
			bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
		}
		catch (BeanCreationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName, errorMessage, ex);
		}

		// Register bean as disposable, and also as dependent on specified "dependsOn" beans.
		registerDisposableBeanIfNecessary(beanName, originalBean, mergedBeanDefinition);

		return bean;
	}

	/**
	 * This implementation determines the type matching <code>createBean</code>'s
	 * different creation strategies.
	 * <p>As far as possible, we'll perform static type checking to avoid creation
	 * of the target bean. However, in case of a non-lazy-init singleton with a
	 * "factory-bean" reference, checking the actual return type is preferable:
	 * For example, a generic service factory might be declared with return type
	 * "Object" or a generic service marker interface, where static type checking
	 * wouldn't reveal any actual type.
	 */
	protected Class getTypeForFactoryMethod(String beanName, RootBeanDefinition mergedBeanDefinition) {
		// Create bean in case of "factory-bean" declaration, to find out actual type.
		// Essentially, this is treated like an implementation of the FactoryBean interface.
		// If not a singleton or marked as lazy-init, we'll resort to static type checking.
		if (mergedBeanDefinition.getFactoryBeanName() != null &&
				mergedBeanDefinition.isSingleton() && !mergedBeanDefinition.isLazyInit()) {
			return getBean(beanName).getClass();
		}

		Class factoryClass = null;
		boolean isStatic = true;

		if (mergedBeanDefinition.getFactoryBeanName() != null) {
			// Check declared factory method return type on factory class.
			factoryClass = getType(mergedBeanDefinition.getFactoryBeanName());
			isStatic = false;
		}
		else {
			// Check declared factory method return type on bean class.
			if (!mergedBeanDefinition.hasBeanClass()) {
				return null;
			}
			factoryClass = mergedBeanDefinition.getBeanClass();
		}

		// If all factory methods have the same return type, return that type.
		// Can't clearly figure out exact method due to type converting / autowiring!
		int minNrOfArgs = mergedBeanDefinition.getConstructorArgumentValues().getArgumentCount();
		Method[] candidates = factoryClass.getMethods();
		Set returnTypes = new HashSet(1);
		for (int i = 0; i < candidates.length; i++) {
			Method factoryMethod = candidates[i];
			if (Modifier.isStatic(factoryMethod.getModifiers()) == isStatic &&
					factoryMethod.getName().equals(mergedBeanDefinition.getFactoryMethodName()) &&
					factoryMethod.getParameterTypes().length >= minNrOfArgs) {
				returnTypes.add(factoryMethod.getReturnType());
			}
		}

		if (returnTypes.size() == 1) {
			// Clear return type found: all factory methods return same type.
			return (Class) returnTypes.iterator().next();
		}
		else {
			// Ambiguous return types found: return null to indicate "not determinable".
			return null;
		}
	}


	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------

	/**
	 * Instantiate the given bean using its default constructor.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @return BeanWrapper for the new instance
	 */
	protected BeanWrapper instantiateBean(String beanName, RootBeanDefinition mergedBeanDefinition)
			throws BeansException {

		Object beanInstance = getInstantiationStrategy().instantiate(mergedBeanDefinition, beanName, this);
		BeanWrapper bw = new BeanWrapperImpl(beanInstance);
		initBeanWrapper(bw);
		return bw;
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the
	 * mergedBeanDefinition parameter specifies a class, rather than a factoryBean, or
	 * an instance variable on a factory object itself configured using Dependency Injection.
	 * <p>Implementation requires iterating over the static or instance methods with the
	 * name specified in the RootBeanDefinition (the method may be overloaded) and trying
	 * to match with the parameters. We don't have the types attached to constructor args,
	 * so trial and error is the only way to go here. The explicitArgs array may contain
	 * argument values passed in programmatically via the corresponding getBean method.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param explicitArgs argument values passed in programmatically via the getBean
	 * method, or <code>null</code> if none (-> use constructor argument values from bean definition)
	 * @return BeanWrapper for the new instance
	 * @see #getBean(String, Object[])
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] explicitArgs) throws BeansException {

		ConstructorArgumentValues cargs = mergedBeanDefinition.getConstructorArgumentValues();
		ConstructorArgumentValues resolvedValues = null;

		int minNrOfArgs = 0;
		if (explicitArgs == null) {
			// We don't have arguments passed in programmatically, so we need to resolve the
			// arguments specified in the constructor arguments held in the bean definition.
			resolvedValues = new ConstructorArgumentValues();
			minNrOfArgs = resolveConstructorArguments(beanName, mergedBeanDefinition, cargs, resolvedValues);
		}
		else {
			minNrOfArgs = explicitArgs.length;
		}

		boolean isStatic = true;
		Class factoryClass = null;
		Object factoryBean = null;

		if (mergedBeanDefinition.getFactoryBeanName() != null) {
			factoryBean = getBean(mergedBeanDefinition.getFactoryBeanName());
			factoryClass = factoryBean.getClass();
			isStatic = false;
		}
		else {
			// It's a static factory method on the bean class.
			factoryClass = mergedBeanDefinition.getBeanClass();
		}

		BeanWrapperImpl bw = new BeanWrapperImpl();
		initBeanWrapper(bw);

		// Try all methods with this name to see if they match the given arguments.
		Method[] candidates = factoryClass.getMethods();

		Method factoryMethodToUse = null;
		Object[] argsToUse = null;
		int minTypeDiffWeight = Integer.MAX_VALUE;

		for (int i = 0; i < candidates.length; i++) {
			Method factoryMethod = candidates[i];

			if (Modifier.isStatic(factoryMethod.getModifiers()) == isStatic &&
					factoryMethod.getName().equals(mergedBeanDefinition.getFactoryMethodName()) &&
					factoryMethod.getParameterTypes().length >= minNrOfArgs) {

				Class[] argTypes = factoryMethod.getParameterTypes();
				ArgumentsHolder args = null;

				if (resolvedValues != null) {
					// Resolved contructor arguments: type conversion and/or autowiring necessary.
					try {
						args = createArgumentArray(
								beanName, mergedBeanDefinition, resolvedValues, bw, argTypes, "factory method");
					}
					catch (UnsatisfiedDependencyException ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Ignoring factory method [" + factoryMethod + "] of bean '" + beanName +
									"': " + ex.getMessage());
						}
						if (i == candidates.length - 1 && factoryMethodToUse == null) {
							throw ex;
						}
						else {
							// Swallow and try next overloaded factory method.
							continue;
						}
					}
				}

				else {
					// Explicit arguments given -> arguments length must match exactly.
					if (argTypes.length != explicitArgs.length) {
						continue;
					}
					args = new ArgumentsHolder(explicitArgs);
				}

				int typeDiffWeight = args.getTypeDifferenceWeight(argTypes);
				// Choose this constructor if it represents the closest match.
				if (typeDiffWeight < minTypeDiffWeight) {
					factoryMethodToUse = factoryMethod;
					argsToUse = args.arguments;
					minTypeDiffWeight = typeDiffWeight;
				}
			}
		}

		if (factoryMethodToUse == null) {
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName,
					"Cannot find matching factory method '" + mergedBeanDefinition.getFactoryMethodName() +
					"' on class [" + factoryClass.getName() + "]");
		}

		// If we get here, we found a factory method.
		Object beanInstance = this.instantiationStrategy.instantiate(
				mergedBeanDefinition, beanName, this, factoryBean, factoryMethodToUse, argsToUse);
		if (beanInstance == null) {
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName,
					"Factory method '" + mergedBeanDefinition.getFactoryMethodName() + "' on class [" +
					factoryClass.getName() + "] returned null");
		}

		bw.setWrappedInstance(beanInstance);
		if (logger.isDebugEnabled()) {
			logger.debug("Bean '" + beanName + "' instantiated via factory method '" + factoryMethodToUse + "'");
		}
		return bw;
	}

	/**
	 * "autowire constructor" (with constructor arguments by type) behavior.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based
	 * dependency resolution.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @return BeanWrapper for the new instance
	 */
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mergedBeanDefinition)
			throws BeansException {

		ConstructorArgumentValues cargs = mergedBeanDefinition.getConstructorArgumentValues();
		ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();

		BeanWrapperImpl bw = new BeanWrapperImpl();
		initBeanWrapper(bw);

		int minNrOfArgs = 0;
		if (cargs != null) {
			minNrOfArgs = resolveConstructorArguments(beanName, mergedBeanDefinition, cargs, resolvedValues);
		}

		Constructor[] candidates = mergedBeanDefinition.getBeanClass().getDeclaredConstructors();
		AutowireUtils.sortConstructors(candidates);

		Constructor constructorToUse = null;
		Object[] argsToUse = null;
		int minTypeDiffWeight = Integer.MAX_VALUE;

		for (int i = 0; i < candidates.length; i++) {
			Constructor constructor = candidates[i];

			if (constructorToUse != null &&
					constructorToUse.getParameterTypes().length > constructor.getParameterTypes().length) {
				// Already found greedy constructor that can be satisfied ->
				// do not look any further, there are only less greedy constructors left.
				break;
			}
			if (constructor.getParameterTypes().length < minNrOfArgs) {
				throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
						minNrOfArgs + " constructor arguments specified but no matching constructor found in bean '" +
						beanName + "' " +
						"(hint: specify index and/or type arguments for simple parameters to avoid type ambiguities)");
			}

			// Try to resolve arguments for current constructor.
			try {
				Class[] argTypes = constructor.getParameterTypes();
				ArgumentsHolder args = createArgumentArray(
						beanName, mergedBeanDefinition, resolvedValues, bw, argTypes, "constructor");

				int typeDiffWeight = args.getTypeDifferenceWeight(argTypes);
				// Choose this constructor if it represents the closest match.
				if (typeDiffWeight < minTypeDiffWeight) {
					constructorToUse = constructor;
					argsToUse = args.arguments;
					minTypeDiffWeight = typeDiffWeight;
				}
			}
			catch (UnsatisfiedDependencyException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring constructor [" + constructor + "] of bean '" + beanName +
							"': " + ex.getMessage());
				}
				if (i == candidates.length - 1 && constructorToUse == null) {
					throw ex;
				}
				else {
					// Swallow and try next constructor.
				}
			}
		}

		if (constructorToUse == null) {
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName, "Could not resolve matching constructor");
		}

		Object beanInstance = this.instantiationStrategy.instantiate(
				mergedBeanDefinition, beanName, this, constructorToUse, argsToUse);
		bw.setWrappedInstance(beanInstance);
		if (logger.isDebugEnabled()) {
			logger.debug("Bean '" + beanName + "' instantiated via constructor [" + constructorToUse + "]");
		}
		return bw;
	}

	/**
	 * Resolve the constructor arguments for this bean into the resolvedValues object.
	 * This may involve looking up other beans.
	 * This method is also used for handling invocations of static factory methods.
	 */
	private int resolveConstructorArguments(
			String beanName, RootBeanDefinition mergedBeanDefinition,
			ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {

		BeanDefinitionValueResolver valueResolver =
				new BeanDefinitionValueResolver(this, beanName, mergedBeanDefinition);

		int minNrOfArgs = cargs.getArgumentCount();

		for (Iterator it = cargs.getIndexedArgumentValues().entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			int index = ((Integer) entry.getKey()).intValue();
			if (index < 0) {
				throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
						"Invalid constructor argument index: " + index);
			}
			if (index > minNrOfArgs) {
				minNrOfArgs = index + 1;
			}
			String argName = "constructor argument with index " + index;
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) entry.getValue();
			Object resolvedValue = valueResolver.resolveValueIfNecessary(argName, valueHolder.getValue());
			resolvedValues.addIndexedArgumentValue(index, resolvedValue, valueHolder.getType());
		}

		for (Iterator it = cargs.getGenericArgumentValues().iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) it.next();
			String argName = "constructor argument";
			Object resolvedValue = valueResolver.resolveValueIfNecessary(argName, valueHolder.getValue());
			resolvedValues.addGenericArgumentValue(resolvedValue, valueHolder.getType());
		}

		return minNrOfArgs;
	}

	/**
	 * Create an array of arguments to invoke a constructor or factory method,
	 * given the resolved constructor argument values.
	 */
	private ArgumentsHolder createArgumentArray(
			String beanName, RootBeanDefinition mergedBeanDefinition, ConstructorArgumentValues resolvedValues,
			BeanWrapperImpl bw, Class[] argTypes, String methodType)
			throws UnsatisfiedDependencyException {

		ArgumentsHolder args = new ArgumentsHolder(argTypes.length);
		Set usedValueHolders = new HashSet(argTypes.length);

		for (int j = 0; j < argTypes.length; j++) {
			// Try to find matching constructor argument value, either indexed or generic.
			ConstructorArgumentValues.ValueHolder valueHolder =
					resolvedValues.getArgumentValue(j, argTypes[j], usedValueHolders);
			// If we couldn't find a direct match and are not supposed to autowire,
			// let's try the next generic, untyped argument value as fallback:
			// it could match after type conversion (for example, String -> int).
			if (valueHolder == null &&
					mergedBeanDefinition.getResolvedAutowireMode() != RootBeanDefinition.AUTOWIRE_CONSTRUCTOR) {
				valueHolder = resolvedValues.getGenericArgumentValue(null, usedValueHolders);
			}
			if (valueHolder != null) {
				// We found a potential match - let's give it a try.
				// Do not consider the same value definition multiple times!
				usedValueHolders.add(valueHolder);
				args.rawArguments[j] = valueHolder.getValue();
				try {
					args.arguments[j] = doTypeConversionIfNecessary(args.rawArguments[j], argTypes[j], bw);
				}
				catch (TypeMismatchException ex) {
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
							"Could not convert " + methodType + " argument value [" + valueHolder.getValue() +
							"] to required type [" + argTypes[j].getName() + "]: " + ex.getMessage());
				}
			}
			else {
				// No explicit match found: we're either supposed to autowire or
				// have to fail creating an argument array for the given constructor.
				if (mergedBeanDefinition.getResolvedAutowireMode() != RootBeanDefinition.AUTOWIRE_CONSTRUCTOR) {
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
							"Ambiguous " + methodType + " argument types - " +
							"did you specify the correct bean references as " + methodType + " arguments?");
				}
				Map matchingBeans = findMatchingBeans(argTypes[j]);
				if (matchingBeans == null || matchingBeans.size() != 1) {
					int matchingBeansCount = (matchingBeans != null ? matchingBeans.size() : 0);
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
							"There are " + matchingBeansCount + " beans of type [" + argTypes[j] +
							"] for autowiring " + methodType + ". There should have been 1 to be able to " +
							"autowire " + methodType + " of bean '" + beanName + "'.");
				}
				String autowiredBeanName = (String) matchingBeans.keySet().iterator().next();
				Object autowiredBean = matchingBeans.values().iterator().next();
				args.rawArguments[j] = autowiredBean;
				args.arguments[j] = autowiredBean;
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Autowiring by type from bean name '" + beanName +
							"' via " + methodType + " to bean named '" + autowiredBeanName + "'");
				}
			}
		}
		return args;
	}


	/**
	 * Populate the bean instance in the given BeanWrapper with the property values
	 * from the bean definition.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param bw BeanWrapper with bean instance
	 */
	protected void populateBean(String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw)
			throws BeansException {

		PropertyValues pvs = mergedBeanDefinition.getPropertyValues();

		if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
				mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
			MutablePropertyValues mpvs = new MutablePropertyValues(pvs);

			// Add property values based on autowire by name if applicable.
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mergedBeanDefinition, bw, mpvs);
			}

			// Add property values based on autowire by type if applicable.
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mergedBeanDefinition, bw, mpvs);
			}

			pvs = mpvs;
		}

		checkDependencies(beanName, mergedBeanDefinition, bw, pvs);
		applyPropertyValues(beanName, mergedBeanDefinition, bw, pvs);
	}

	/**
	 * Fill in any missing property values with references to
	 * other beans in this factory if autowire is set to "byName".
	 * @param beanName name of the bean we're wiring up.
	 * Useful for debugging messages; not used functionally.
	 * @param mergedBeanDefinition bean definition to update through autowiring
	 * @param bw BeanWrapper from which we can obtain information about the bean
	 * @param pvs the PropertyValues to register wired objects with
	 */
	protected void autowireByName(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, MutablePropertyValues pvs)
			throws BeansException {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mergedBeanDefinition, bw);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.addPropertyValue(propertyName, bean);
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(propertyName, beanName);
				}
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
	 * Abstract method defining "autowire by type" (bean properties by type) behavior.
	 * <p>This is like PicoContainer default, in which there must be exactly one bean
	 * of the property type in the bean factory. This makes bean factories simple to
	 * configure for small namespaces, but doesn't work as well as standard Spring
	 * behavior for bigger applications.
	 * @param beanName name of the bean to autowire by type
	 * @param mergedBeanDefinition bean definition to update through autowiring
	 * @param bw BeanWrapper from which we can obtain information about the bean
	 * @param pvs the PropertyValues to register wired objects with
	 */
	protected void autowireByType(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, MutablePropertyValues pvs)
			throws BeansException {

		String[] propertyNames = unsatisfiedNonSimpleProperties(mergedBeanDefinition, bw);
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];
			// look for a matching type
			Class requiredType = bw.getPropertyDescriptor(propertyName).getPropertyType();
			Map matchingBeans = findMatchingBeans(requiredType);
			if (matchingBeans != null && matchingBeans.size() == 1) {
				String autowiredBeanName = (String) matchingBeans.keySet().iterator().next();
				Object autowiredBean = matchingBeans.values().iterator().next();
				pvs.addPropertyValue(propertyName, autowiredBean);
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" +
							propertyName + "' to bean named '" + autowiredBeanName + "'");
				}
			}
			else if (matchingBeans != null && matchingBeans.size() > 1) {
				throw new UnsatisfiedDependencyException(
						mergedBeanDefinition.getResourceDescription(), beanName, propertyName,
						"There are " + matchingBeans.size() + " beans of type [" + requiredType +
						"] for autowire by type. There should have been 1 to be able to autowire property '" +
						propertyName + "' of bean '" + beanName + "'.");
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
	 * Return an array of non-simple bean properties that are unsatisfied.
	 * These are probably unsatisfied references to other beans in the
	 * factory. Does not include simple properties like primitives or Strings.
	 * @param mergedBeanDefinition the bean definition the bean was created with
	 * @param bw the BeanWrapper the bean was created with
	 * @return an array of bean property names
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	protected String[] unsatisfiedNonSimpleProperties(RootBeanDefinition mergedBeanDefinition, BeanWrapper bw) {
		Set result = new TreeSet();
		PropertyValues pvs = mergedBeanDefinition.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null && !isExcludedFromDependencyCheck(pds[i]) &&
					!pvs.contains(pds[i].getName()) && !BeanUtils.isSimpleProperty(pds[i].getPropertyType())) {
				result.add(pds[i].getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName the name of the bean
	 * @param mergedBeanDefinition the bean definition the bean was created with
	 * @param bw the BeanWrapper the bean was created with
	 * @param pvs the property values to be applied to the bean
	 * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 */
	protected void checkDependencies(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, PropertyValues pvs)
			throws UnsatisfiedDependencyException {

		int dependencyCheck = mergedBeanDefinition.getDependencyCheck();
		if (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_NONE) {
			return;
		}

		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null && !isExcludedFromDependencyCheck(pds[i]) &&
					!pvs.contains(pds[i].getName())) {
				boolean isSimple = BeanUtils.isSimpleProperty(pds[i].getPropertyType());
				boolean unsatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL) ||
					(isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
					(!isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, pds[i].getName(),
							"Set this property value or disable dependency checking for this bean.");
				}
			}
		}
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB and
	 * properties whose type matches an ignored dependency type or which
	 * are defined by an ignored dependency interface.
	 * @param pd the PropertyDescriptor of the bean property
	 * @return whether the bean property is excluded
	 * @see AutowireUtils#isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 * @see #ignoreDependencyType(Class)
	 * @see #ignoreDependencyInterface(Class)
	 * @see AutowireUtils#isExcludedFromDependencyCheck
	 * @see AutowireUtils#isSetterDefinedInInterface
	 */
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				getIgnoredDependencyTypes().contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInInterface(pd, getIgnoredDependencyInterfaces()));
	}

	/**
	 * Apply the given property values, resolving any runtime references
	 * to other beans in this bean factory. Must use deep copy, so we
	 * don't permanently modify this property.
	 * @param beanName bean name passed for better exception information
	 * @param bw BeanWrapper wrapping the target object
	 * @param pvs new property values
	 */
	private void applyPropertyValues(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, PropertyValues pvs)
			throws BeansException {

		if (pvs == null) {
			return;
		}

		BeanDefinitionValueResolver valueResolver =
				new BeanDefinitionValueResolver(this, beanName, mergedBeanDefinition);

		// Create a deep copy, resolving any references for values.
		MutablePropertyValues deepCopy = new MutablePropertyValues();
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (int i = 0; i < pvArray.length; i++) {
			PropertyValue pv = pvArray[i];
			Object resolvedValue =
					valueResolver.resolveValueIfNecessary("bean property '" + pv.getName() + "'", pv.getValue());
			deepCopy.addPropertyValue(pvArray[i].getName(), resolvedValue);
		}

		// Set our (possibly massaged) deep copy.
		try {
			// Synchronize if custom editors are registered.
			// Necessary because PropertyEditors are not thread-safe.
			if (!getCustomEditors().isEmpty()) {
				synchronized (this) {
					bw.setPropertyValues(deepCopy);
				}
			}
			else {
				bw.setPropertyValues(deepCopy);
			}
		}
		catch (BeansException ex) {
			// Improve the message by showing the context.
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName, "Error setting property values", ex);
		}
	}


	/**
	 * Give a bean a chance to react now all its properties are set,
	 * and a chance to know about its owning bean factory (this object).
	 * This means checking whether the bean implements InitializingBean or defines
	 * a custom init method, and invoking the necessary callback(s) if it does.
	 * @param beanName the bean has in the factory. Used for debug output.
	 * @param bean new bean instance we may need to initialize
	 * @param mergedBeanDefinition the bean definition that the bean was created with
	 * (can also be <code>null</code>, if given an existing bean instance)
	 * @throws Throwable if thrown by init methods or by the invocation process
	 * @see #invokeCustomInitMethod
	 */
	protected void invokeInitMethods(String beanName, Object bean, RootBeanDefinition mergedBeanDefinition)
			throws Throwable {

		if (bean instanceof InitializingBean) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			((InitializingBean) bean).afterPropertiesSet();
		}

		if (mergedBeanDefinition != null && mergedBeanDefinition.getInitMethodName() != null) {
			invokeCustomInitMethod(
					beanName, bean, mergedBeanDefinition.getInitMethodName(), mergedBeanDefinition.isEnforceInitMethod());
		}
	}

	/**
	 * Invoke the specified custom init method on the given bean.
	 * Called by invokeInitMethods.
	 * <p>Can be overridden in subclasses for custom resolution of init
	 * methods with arguments.
	 * @param beanName the bean has in the factory. Used for debug output.
	 * @param bean new bean instance we may need to initialize
	 * @param initMethodName the name of the custom init method
	 * @param enforceInitMethod indicates whether the defined init method needs to exist
	 * @see #invokeInitMethods
	 */
	protected void invokeCustomInitMethod(
			String beanName, Object bean, String initMethodName, boolean enforceInitMethod) throws Throwable {

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking custom init method '" + initMethodName +
					"' on bean with name '" + beanName + "'");
		}
		Method initMethod = BeanUtils.findMethod(bean.getClass(), initMethodName, null);
		if (initMethod == null) {
			if (enforceInitMethod) {
				throw new NoSuchMethodException("Couldn't find an init method named '" + initMethodName +
						"' on bean with name '" + beanName + "'");
			}
			else {
				// Ignore non-existent default lifecycle methods.
				return;
			}
		}
		if (!Modifier.isPublic(initMethod.getModifiers())) {
			initMethod.setAccessible(true);
		}
		try {
			initMethod.invoke(bean, (Object[]) null);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	//---------------------------------------------------------------------
	// Abstract method to be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Find bean instances that match the required type. Called by autowiring.
	 * If a subclass cannot obtain information about bean names by type,
	 * a corresponding exception should be thrown.
	 * @param requiredType the type of the beans to look up
	 * @return a Map of bean names and bean instances that match the required type,
	 * or <code>null</code> if none found
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected abstract Map findMatchingBeans(Class requiredType) throws BeansException;


	//---------------------------------------------------------------------
	// Private inner class for internal use
	//---------------------------------------------------------------------

	private static class ArgumentsHolder {

		public Object rawArguments[];

		public Object arguments[];

		public ArgumentsHolder(int size) {
			this.rawArguments = new Object[size];
			this.arguments = new Object[size];
		}

		public ArgumentsHolder(Object[] args) {
			this.rawArguments = args;
			this.arguments = args;
		}

		public int getTypeDifferenceWeight(Class[] argTypes) {
			// If valid arguments found, determine type difference weight.
			// Try type difference weight on both the converted arguments and
			// the raw arguments. If the raw weight is better, use it.
			// Decrease raw weight by 1024 to prefer it over equal converted weight.
			int typeDiffWeight = AutowireUtils.getTypeDifferenceWeight(argTypes, this.arguments);
			int rawTypeDiffWeight = AutowireUtils.getTypeDifferenceWeight(argTypes, this.rawArguments) - 1024;
			return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
		}
	}

}
