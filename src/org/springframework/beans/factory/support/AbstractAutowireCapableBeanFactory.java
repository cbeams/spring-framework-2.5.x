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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.CollectionFactory;

/**
 * Abstract bean factory superclass that implements default bean creation.
 * Implements the AutowireCapableBeanFactory interface.
 *
 * <p>Provides bean creation, initialization and wiring, supporting autowiring and constructor
 * resolution. Handles runtime bean references, managed collections, and bean destruction.
 *
 * <p>The main template method to be implemented by subclasses is <code>findMatchingBeans</code,
 * used for autowiring by type. Note that this class does <i>not</i> implement bean definition
 * registry capabilities (DefaultListableBeanFactory does).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.02.2004
 * @see #findMatchingBeans
 * @see DefaultListableBeanFactory
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
    implements AutowireCapableBeanFactory {

	static {
		// Eagerly load the DisposableBean and DestructionAwareBeanPostProcessor
		// classes to avoid weird classloader issues on application shutdown in
		// WebLogic 8.1. (Reported by Andreas Senft and Eric Ma.)
		DisposableBean.class.getName();
		DestructionAwareBeanPostProcessor.class.getName();
	}


	private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();

	/**
	 * Set that holds all inner beans created by this factory that implement
	 * the DisposableBean interface, to be destroyed on destroySingletons.
	 * @see #destroySingletons
	 */
	private final Map disposableInnerBeans = Collections.synchronizedMap(new HashMap());

	private final Map dependentBeanMap = Collections.synchronizedMap(new HashMap());


	/**
	 * Create a new AbstractAutowireCapableBeanFactory.
	 */
	public AbstractAutowireCapableBeanFactory() {
		super();
	}

	/**
	 * Create a new AbstractAutowireCapableBeanFactory with the given parent.
	 * @param parentBeanFactory parent bean factory, or null if none
	 */
	public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Set the instantiation strategy to use.
	 * Can be called by subclasses during initialization.
	 * Default is CglibSubclassingInstantiationStrategy.
	 * @see CglibSubclassingInstantiationStrategy
	 */
	protected void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}


	//---------------------------------------------------------------------
	// Implementation of AutowireCapableBeanFactory
	//---------------------------------------------------------------------

	public Object autowire(Class beanClass, int autowireMode, boolean dependencyCheck)
			throws BeansException {
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd).getWrappedInstance();
		}
		else {
			Object bean = instantiationStrategy.instantiate(bd, null, this);
			populateBean(bean.getClass().getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}

	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Just constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE allowed");
		}
		RootBeanDefinition bd = new RootBeanDefinition(existingBean.getClass(), autowireMode, dependencyCheck);
		populateBean(existingBean.getClass().getName(), bd, new BeanWrapperImpl(existingBean));
	}

	public void applyBeanPropertyValues(Object existingBean, String name) throws BeansException {
		RootBeanDefinition bd = getMergedBeanDefinition(name, true);
		applyPropertyValues(name, bd, new BeanWrapperImpl(existingBean), bd.getPropertyValues());
	}

	public Object applyBeanPostProcessorsBeforeInitialization(Object bean, String name) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors before initialization of bean '" + name + "'");
		}
		Object result = bean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			result = beanProcessor.postProcessBeforeInitialization(result, name);
			if (result == null) {
				throw new BeanCreationException(
						"postProcessBeforeInitialization method of BeanPostProcessor [" + beanProcessor +
						"] returned null for bean [" + result + "] with name [" + name + "]");
			}
		}
		return result;
	}

	public Object applyBeanPostProcessorsAfterInitialization(Object bean, String name) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking BeanPostProcessors after initialization of bean '" + name + "'");
		}
		Object result = bean;
		for (Iterator it = getBeanPostProcessors().iterator(); it.hasNext();) {
			BeanPostProcessor beanProcessor = (BeanPostProcessor) it.next();
			result = beanProcessor.postProcessAfterInitialization(result, name);
			if (result == null) {
				throw new BeanCreationException(
				    "postProcessAfterInitialization method of BeanPostProcessor [" + beanProcessor +
				    "] returned null for bean [" + result + "] with name [" + name + "]");
			}
		}
		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	/**
	 * Delegates to full createBean version with allowEagerCaching=true.
	 * @see #createBean(String, RootBeanDefinition, Object[], boolean)
	 */
	protected Object createBean(String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args)
	    throws BeansException {
		return createBean(beanName, mergedBeanDefinition, args, true);
	}

	/**
	 * Create a bean instance for the given bean definition.
	 * @param beanName name of the bean
	 * @param mergedBeanDefinition the bean definition for the bean
	 * @param allowEagerCaching whether eager caching of singletons is allowed
	 * (typically true for normal beans, but false for inner beans)
	 * @param args arguments to use if this is a prototype constructed by a factory method.
	 * In this case, this will override any args specified in the bean definitions.
	 * This parameter should be null otherwise.
	 * @return a new instance of the bean
	 * @throws BeansException in case of errors
	 */
	protected Object createBean(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args, boolean allowEagerCaching)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Creating instance of bean '" + beanName +
					"' with merged definition [" + mergedBeanDefinition + "]");
		}

		if (mergedBeanDefinition.getDependsOn() != null) {
			for (int i = 0; i < mergedBeanDefinition.getDependsOn().length; i++) {
				// guarantee initialization of beans that the current one depends on
				getBean(mergedBeanDefinition.getDependsOn()[i]);
			}
		}

		BeanWrapper instanceWrapper = null;
		Object bean = null;
		String errorMessage = null;
		boolean eagerlyCached = false;

		try {
			// instantiate bean
			errorMessage = "Instantiation of bean failed";

			if (mergedBeanDefinition.getFactoryMethodName() != null)  {
				instanceWrapper = instantiateUsingFactoryMethod(beanName, mergedBeanDefinition, args);
			}
			else if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
					mergedBeanDefinition.hasConstructorArgumentValues() )  {
				instanceWrapper = autowireConstructor(beanName, mergedBeanDefinition);
			}
			else {
				// use no-arg constructor
				Object beanInstance = this.instantiationStrategy.instantiate(mergedBeanDefinition, beanName, this);
				instanceWrapper = new BeanWrapperImpl(beanInstance);
				initBeanWrapper(instanceWrapper);
			}
			bean = instanceWrapper.getWrappedInstance();

			// Eagerly cache singletons to be able to resolve circular references
			// even when triggered by lifecycle interfaces like BeanFactoryAware.
			if (allowEagerCaching && mergedBeanDefinition.isSingleton()) {
				addSingleton(beanName, bean);
				eagerlyCached = true;
			}

			// initialize bean
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

			bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);
			invokeInitMethods(beanName, mergedBeanDefinition, bean);
			bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
		}
		catch (BeanCreationException ex) {
			if (eagerlyCached) {
				removeSingleton(beanName);
			}
			throw ex;
		}
		catch (Throwable ex) {
			if (eagerlyCached) {
				removeSingleton(beanName);
			}
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName, errorMessage, ex);
		}

		// Register bean as dependent on specified "dependsOn" beans.
		// This information is used on shutdown, to destroy dependent beans
		// before the beans that they depend on.
		if (mergedBeanDefinition.isSingleton()) {
			String[] dependsOn = mergedBeanDefinition.getDependsOn();
			if (dependsOn != null) {
				for (int i = 0; i < dependsOn.length; i++) {
					registerDependentBean(dependsOn[i], beanName);
				}
			}
		}

		return bean;
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the
	 * mergedBeanDefinition parameter specifies a class, rather than a factoryBean, or
	 * an instance variable on a factory object itself configured using Dependency Injection.
	 * <p>Implementation requires iterating over the static or instance methods with the
	 * name specified in the RootBeanDefinition (the method may be overloaded) and trying
	 * to match with the parameters. We don't have the types attached to constructor args,
	 * so trial and error is the only way to go here. The args array may contain argument
	 * values passed in programmatically via the overloaded getBean() method.
	 */
	protected BeanWrapper instantiateUsingFactoryMethod(
			String beanName, RootBeanDefinition mergedBeanDefinition, Object[] args) throws BeansException {

		ConstructorArgumentValues cargs = mergedBeanDefinition.getConstructorArgumentValues();
		ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();
		int expectedArgCount = 0;

		// We don't have arguments passed in programmatically, so we need to resolve the
		// arguments specified in the constructor arguments held in the bean definition.
		if (args == null) {
			expectedArgCount = cargs.getArgumentCount();
			resolveConstructorArguments(beanName, mergedBeanDefinition, cargs, resolvedValues);
		}
		else {
			// If we have constructor args, don't need to resolve them.
			expectedArgCount = args.length;
		}

		BeanWrapperImpl bw = new BeanWrapperImpl();
		initBeanWrapper(bw);

		boolean isStatic = true;
		Class factoryClass = null;
		if (mergedBeanDefinition.getFactoryBeanName() != null) {
			// It's an instance method on the factory bean's class.
			factoryClass = getBean(mergedBeanDefinition.getFactoryBeanName()).getClass();
			isStatic = false;
		}
		else {
			// It's a static factory method on the bean class.
			factoryClass = mergedBeanDefinition.getBeanClass();
		}

		// try all methods with this name to see if they match constructor arguments
		for (int i = 0; i < factoryClass.getMethods().length; i++) {
			Method factoryMethod = factoryClass.getMethods()[i];
			if (Modifier.isStatic(factoryMethod.getModifiers()) == isStatic &&
					factoryMethod.getName().equals(mergedBeanDefinition.getFactoryMethodName()) &&
					factoryMethod.getParameterTypes().length == expectedArgCount) {

				Class[] argTypes = factoryMethod.getParameterTypes();

				try {
					// try to create the required arguments
					if (args == null) {
						args = createArgumentArray(beanName, mergedBeanDefinition, resolvedValues, bw, argTypes);
					}
				}
				catch (Exception ex) {
					// If we failed to match this method, swallow the exception and keep trying new overloaded
					// factory methods...
					continue;
				}

				// If we get here, we found a factory method
				Object beanInstance =
						this.instantiationStrategy.instantiate(mergedBeanDefinition, beanName, this, factoryMethod, args);

				// TODO: If we got to here, we could cache the resolved Method in the RootBeanDefinition for
				// efficiency on future creation, but that would need to be synchronized.

				bw.setWrappedInstance(beanInstance);
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' instantiated via factory method [" + factoryMethod + "]");
				}
				return bw;
			}
		}	// for each method

		// If we get here, we didn't match any method.
		throw new BeanDefinitionStoreException(
				"Cannot find matching factory method '" + mergedBeanDefinition.getFactoryMethodName() +
				"' on class " + factoryClass);
	}


	/**
	 * "autowire constructor" (with constructor arguments by type) behavior.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based
	 * dependency resolution.
	 * @param beanName name of the bean to autowire by type
	 * @param mergedBeanDefinition bean definition to update through autowiring
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

		Constructor[] constructors = mergedBeanDefinition.getBeanClass().getDeclaredConstructors();
		Arrays.sort(constructors, new Comparator() {
			public int compare(Object o1, Object o2) {
				Constructor c1 = (Constructor) o1;
				Constructor c2 = (Constructor) o2;
				boolean p1 = Modifier.isPublic(c1.getModifiers());
				boolean p2 = Modifier.isPublic(c2.getModifiers());
				if (p1 != p2) {
					return (p1 ? -1 : 1);
				}
				int c1pl = c1.getParameterTypes().length;
				int c2pl = c2.getParameterTypes().length;
				return (new Integer(c1pl)).compareTo(new Integer(c2pl)) * -1;
			}
		});

		Constructor constructorToUse = null;
		Object[] argsToUse = null;
		int minTypeDiffWeight = Integer.MAX_VALUE;
		for (int i = 0; i < constructors.length; i++) {
			try {
				Constructor constructor = constructors[i];
				if (constructor.getParameterTypes().length < minNrOfArgs) {
					throw new BeanCreationException(mergedBeanDefinition.getResourceDescription(), beanName,
							minNrOfArgs + " constructor arguments specified but no matching constructor found in bean '" +
							beanName + "' (hint: specify index arguments for simple parameters to avoid type ambiguities)");
				}
				Class[] argTypes = constructor.getParameterTypes();
				Object[] args = createArgumentArray(beanName, mergedBeanDefinition, resolvedValues, bw, argTypes);

				int typeDiffWeight = getTypeDifferenceWeight(argTypes, args);
				if (typeDiffWeight < minTypeDiffWeight) {
					constructorToUse = constructor;
					argsToUse = args;
					minTypeDiffWeight = typeDiffWeight;
				}
			}
			catch (BeansException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring constructor [" + constructors[i] + "] of bean '" + beanName +
							"': could not satisfy dependencies. Detail: " + ex.getMessage());
				}
				if (i == constructors.length - 1 && constructorToUse == null) {
					// all constructors tried
					throw ex;
				}
				else {
					// swallow and try next constructor
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
		if (logger.isInfoEnabled()) {
			logger.info("Bean '" + beanName + "' instantiated via constructor [" + constructorToUse + "]");
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

		int minNrOfArgs;
		minNrOfArgs = cargs.getArgumentCount();
		for (Iterator it = cargs.getIndexedArgumentValues().entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			int index = ((Integer) entry.getKey()).intValue();
			if (index < 0) {
				throw new BeanCreationException(
						mergedBeanDefinition.getResourceDescription(), beanName,
						"Invalid constructor argument index: " + index);
			}
			if (index > minNrOfArgs) {
				minNrOfArgs = index + 1;
			}
			String argName = "constructor argument with index " + index;
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) entry.getValue();
			Object resolvedValue =
					resolveValueIfNecessary(beanName, mergedBeanDefinition, argName, valueHolder.getValue());
			resolvedValues.addIndexedArgumentValue(index, resolvedValue, valueHolder.getType());
		}
		for (Iterator it = cargs.getGenericArgumentValues().iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) it.next();
			String argName = "constructor argument";
			Object resolvedValue =
					resolveValueIfNecessary(beanName, mergedBeanDefinition, argName, valueHolder.getValue());
			resolvedValues.addGenericArgumentValue(resolvedValue, valueHolder.getType());
		}
		return minNrOfArgs;
	}

	/**
	 * Create an array of arguments to invoke a Constructor or static factory method,
	 * given the resolved constructor arguments values.
	 */
	private Object[] createArgumentArray(
			String beanName, RootBeanDefinition mergedBeanDefinition,
			ConstructorArgumentValues resolvedValues, BeanWrapperImpl bw, Class[] argTypes) {

		Object[] args = new Object[argTypes.length];
		Set usedValueHolders = new HashSet(argTypes.length);

		for (int j = 0; j < argTypes.length; j++) {
			ConstructorArgumentValues.ValueHolder valueHolder = resolvedValues.getArgumentValue(j, argTypes[j]);
			if (valueHolder != null && !usedValueHolders.contains(valueHolder)) {
				// Do not consider the same value definition multiple times!
				usedValueHolders.add(valueHolder);

				// Synchronize if custom editors are registered.
				// Necessary because PropertyEditors are not thread-safe.
				if (!getCustomEditors().isEmpty()) {
					synchronized (getCustomEditors()) {
						args[j] = bw.doTypeConversionIfNecessary(valueHolder.getValue(), argTypes[j]);
					}
				}
				else {
					args[j] = bw.doTypeConversionIfNecessary(valueHolder.getValue(), argTypes[j]);
				}
			}

			else {
				if (mergedBeanDefinition.getResolvedAutowireMode() != RootBeanDefinition.AUTOWIRE_CONSTRUCTOR) {
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
							"Did you specify the correct bean references as generic constructor arguments?");
				}
				Map matchingBeans = findMatchingBeans(argTypes[j]);
				if (matchingBeans == null || matchingBeans.size() != 1) {
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, j, argTypes[j],
							"There are " + matchingBeans.size() + " beans of type [" + argTypes[j] +
							"] for autowiring constructor. There should have been 1 to be able to " +
							"autowire constructor of bean '" + beanName + "'.");
				}
				String autowiredBeanName = (String) matchingBeans.keySet().iterator().next();
				Object autowiredBean = matchingBeans.values().iterator().next();
				args[j] = autowiredBean;
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isInfoEnabled()) {
					logger.info("Autowiring by type from bean name '" + beanName +
							"' via constructor to bean named '" + matchingBeans.keySet().iterator().next() + "'");
				}
			}
		}
		return args;
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
			if (!BeanUtils.isAssignable(argTypes[i], args[i])) {
				return Integer.MAX_VALUE;
			}
			if (args[i] != null) {
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

			// add property values based on autowire by name if it's applied
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mergedBeanDefinition, bw, mpvs);
			}

			// add property values based on autowire by type if it's applied
			if (mergedBeanDefinition.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mergedBeanDefinition, bw, mpvs);
			}

			pvs = mpvs;
		}

		dependencyCheck(beanName, mergedBeanDefinition, bw, pvs);
		applyPropertyValues(beanName, mergedBeanDefinition, bw, pvs);
	}

	/**
	 * Fills in any missing property values with references to
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

		String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, bw);
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

		String[] propertyNames = unsatisfiedObjectProperties(mergedBeanDefinition, bw);
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
					logger.debug("Autowiring by type from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" +
							matchingBeans.keySet().iterator().next() + "'");
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
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName name of the bean
	 */
	protected void dependencyCheck(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, PropertyValues pvs)
			throws UnsatisfiedDependencyException {

		int dependencyCheck = mergedBeanDefinition.getDependencyCheck();
		if (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_NONE) {
			return;
		}

		Set ignoreTypes = getIgnoredDependencyTypes();
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
					throw new UnsatisfiedDependencyException(
							mergedBeanDefinition.getResourceDescription(), beanName, pds[i].getName(),
							"Set this property value or disable dependency checking for this bean.");
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
	 * to other beans in this bean factory. Must use deep copy, so we
	 * don't permanently modify this property.
	 * @param beanName bean name passed for better exception information
	 * @param bw BeanWrapper wrapping the target object
	 * @param pvs new property values
	 */
	protected void applyPropertyValues(
			String beanName, RootBeanDefinition mergedBeanDefinition, BeanWrapper bw, PropertyValues pvs)
			throws BeansException {

		if (pvs == null) {
			return;
		}

		MutablePropertyValues deepCopy = new MutablePropertyValues(pvs);
		PropertyValue[] pvals = deepCopy.getPropertyValues();
		for (int i = 0; i < pvals.length; i++) {
			Object value = resolveValueIfNecessary(
					beanName, mergedBeanDefinition, pvals[i].getName(), pvals[i].getValue());
			PropertyValue pv = new PropertyValue(pvals[i].getName(), value);
			// update mutable copy
			deepCopy.setPropertyValueAt(pv, i);
		}

		// set our (possibly massaged) deepCopy
		try {
			// synchronize if custom editors are registered
			// necessary because PropertyEditors are not thread-safe
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
			// improve the message by showing the context
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName, "Error setting property values", ex);
		}
	}

	/**
	 * Given a PropertyValue, return a value, resolving any references to other
	 * beans in the factory if necessary. The value could be:
	 * <li>A BeanDefinition, which leads to the creation of a corresponding
	 * new bean instance. Singleton flags and names of such "inner beans"
	 * are always ignored: Inner beans are anonymous prototypes.
	 * <li>A RuntimeBeanReference, which must be resolved.
	 * <li>A ManagedList. This is a special collection that may contain
	 * RuntimeBeanReferences or Collections that will need to be resolved.
	 * <li>A ManagedSet. May also contain RuntimeBeanReferences or
	 * Collections that will need to be resolved.
	 * <li>A ManagedMap. In this case the value may be a RuntimeBeanReference
	 * or Collection that will need to be resolved.
	 * <li>An ordinary object or null, in which case it's left alone.
	 */
	protected Object resolveValueIfNecessary(
			String beanName, RootBeanDefinition mergedBeanDefinition, String argName, Object value)
			throws BeansException {

		// We must check each PropertyValue to see whether it
		// requires a runtime reference to another bean to be resolved.
		// If it does, we'll attempt to instantiate the bean and set the reference.
		if (value instanceof BeanDefinitionHolder) {
			// Resolve BeanDefinitionHolder: contains BeanDefinition with name and aliases.
			BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
			return resolveInnerBeanDefinition(beanName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
		}
		else if (value instanceof BeanDefinition) {
			// Resolve plain BeanDefinition, without contained name: use dummy name.
			BeanDefinition bd = (BeanDefinition) value;
			return resolveInnerBeanDefinition(
					beanName, "(inner bean of type " + bd.getBeanClass().getName() + ")", bd);
		}
		else if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(beanName, mergedBeanDefinition, argName, ref);
		}
		else if (value instanceof ManagedList) {
			// Convert from managed list. This is a special container that may
			// contain runtime bean references. May need to resolve references.
			return resolveManagedList(beanName, mergedBeanDefinition, argName, (List) value);
		}
		else if (value instanceof ManagedSet) {
			// Convert from managed set. This is a special container that may
			// contain runtime bean references. May need to resolve references.
			return resolveManagedSet(beanName, mergedBeanDefinition, argName, (Set) value);
		}
		else if (value instanceof ManagedMap) {
			// Convert from managed map. This is a special container that may
			// contain runtime bean references. May need to resolve references.
			return resolveManagedMap(beanName, mergedBeanDefinition, argName, (Map) value);
		}
		else {
			// no need to resolve value
			return value;
		}
	}

	/**
	 * Resolve an inner bean definition.
	 */
	protected Object resolveInnerBeanDefinition(String beanName, String innerBeanName, BeanDefinition innerBd)
	    throws BeansException {
		RootBeanDefinition mergedInnerBd = getMergedBeanDefinition(innerBeanName, innerBd);
		Object innerBean = createBean(innerBeanName, mergedInnerBd, null, false);
		if (mergedInnerBd.isSingleton()) {
			if (innerBean instanceof DisposableBean) {
				registerDependentBean(innerBeanName, beanName);
				// keep reference to inner bean, to be able to destroy it on factory shutdown
				this.disposableInnerBeans.put(innerBeanName, innerBean);
			}
		}
		return getObjectForSharedInstance(innerBeanName, innerBean);
	}

	/**
	 * Resolve a reference to another bean in the factory.
	 */
	protected Object resolveReference(
			String beanName, RootBeanDefinition mergedBeanDefinition, String argName, RuntimeBeanReference ref)
			throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Resolving reference from property '" + argName + "' in bean '" +
					beanName + "' to bean '" + ref.getBeanName() + "'");
		}
		try {
			if (ref.isToParent()) {
				if (getParentBeanFactory() == null) {
					throw new BeanCreationException(
							mergedBeanDefinition.getResourceDescription(), beanName,
							"Can't resolve reference to bean '" + ref.getBeanName() +
							"' in parent factory: no parent factory available");
				}
				return getParentBeanFactory().getBean(ref.getBeanName());
			}
			else {
				if (mergedBeanDefinition.isSingleton()) {
					registerDependentBean(ref.getBeanName(), beanName);
				}
				return getBean(ref.getBeanName());
			}
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					mergedBeanDefinition.getResourceDescription(), beanName,
					"Can't resolve reference to bean '" + ref.getBeanName() +
					"' while setting property '" + argName + "'", ex);
		}
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	protected List resolveManagedList(
			String beanName, RootBeanDefinition mergedBeanDefinition, String argName, List ml)
			throws BeansException {

		List resolved = new ArrayList(ml.size());
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(
			    resolveValueIfNecessary(
							beanName, mergedBeanDefinition,
							argName + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
							ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	protected Set resolveManagedSet(
			String beanName, RootBeanDefinition mergedBeanDefinition, String argName, Set ms)
			throws BeansException {

		Set resolved = new HashSet(ms.size());
		int i = 0;
		for (Iterator it = ms.iterator(); it.hasNext();) {
			resolved.add(
			    resolveValueIfNecessary(
							beanName, mergedBeanDefinition,
							argName + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
							it.next()));
			i++;
		}
		return resolved;
	}

	/**
	 * For each element in the ManagedMap, resolve reference if necessary.
	 */
	protected Map resolveManagedMap(
			String beanName, RootBeanDefinition mergedBeanDefinition, String argName, Map mm)
			throws BeansException {

		Map resolved = CollectionFactory.createLinkedMapIfPossible(mm.size());
		Iterator keys = mm.keySet().iterator();
		while (keys.hasNext()) {
			Object key = keys.next();
			resolved.put(
			    key,
			    resolveValueIfNecessary(
							beanName, mergedBeanDefinition,
							argName + BeanWrapper.PROPERTY_KEY_PREFIX + key + BeanWrapper.PROPERTY_KEY_SUFFIX,
							mm.get(key)));
		}
		return resolved;
	}

	/**
	 * Register a dependent bean for the given bean,
	 * to be destroyed before the given bean is destroyed.
	 * @param beanName the name of the bean
	 * @param dependentBeanName the name of the dependent bean
	 */
	protected final void registerDependentBean(String beanName, String dependentBeanName) {
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
	 * Give a bean a chance to react now all its properties are set,
	 * and a chance to know about its owning bean factory (this object).
	 * This means checking whether the bean implements InitializingBean
	 * and/or BeanFactoryAware, and invoking the necessary callback(s) if it does.
	 * @param bean new bean instance we may need to initialize
	 * @param beanName the bean has in the factory. Used for debug output.
	 * @throws Throwable if thrown by init methods or by the invocation process
	 */
	protected void invokeInitMethods(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean)
			throws Throwable {

		if (bean instanceof InitializingBean) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking afterPropertiesSet() on bean with beanName '" + beanName + "'");
			}
			((InitializingBean) bean).afterPropertiesSet();
		}

		if (mergedBeanDefinition.getInitMethodName() != null) {
			invokeCustomInitMethod(beanName, bean, mergedBeanDefinition.getInitMethodName(),
					mergedBeanDefinition.getResourceDescription());
		}
	}

	/**
	 * Invoke the specified custom init method on the given bean.
	 * <p>Can be overridden in subclasses for custom resolution of init
	 * methods with arguments.
	 */
	protected void invokeCustomInitMethod(String beanName, Object bean, String initMethodName,
			String resourceDescription) throws Throwable {

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking custom init method '" + initMethodName +
					"' on bean with beanName '" + beanName + "'");
		}
		try {
			Method initMethod = BeanUtils.findMethod(bean.getClass(), initMethodName, null);
			if (initMethod == null) {
				throw new NoSuchMethodException("Couldn't find an init method named '" + initMethodName +
						"' on bean with name '" + beanName + "'");
			}
			if (!Modifier.isPublic(initMethod.getModifiers())) {
				initMethod.setAccessible(true);
			}
			initMethod.invoke(bean, null);
		}
		catch (InvocationTargetException ex) {
			throw ex.getTargetException();
		}
	}


	public void destroySingletons() {
		super.destroySingletons();

		if (logger.isInfoEnabled()) {
			logger.info("Destroying inner beans in factory {" + this + "}");
		}
		synchronized (this.disposableInnerBeans) {
			for (Iterator it = new HashSet(this.disposableInnerBeans.keySet()).iterator(); it.hasNext();) {
				destroyDisposableInnerBean((String) it.next());
			}
		}
	}

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
					destroyDisposableInnerBean(dependentBeanName);
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
			RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
			if (bd.getDestroyMethodName() != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invoking custom destroy method '" + bd.getDestroyMethodName() +
							"' on bean with name '" + beanName + "'");
				}
				invokeCustomDestroyMethod(beanName, bean, bd.getDestroyMethodName());
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// ignore, from manually registered singleton
		}
	}

	/**
	 * Destroy the given inner bean. Delegates to destroyBean if a corresponding
	 * disposable inner bean instance is found.
	 * @param innerBeanName name of the inner bean
	 * @see #destroyBean
	 */
	private void destroyDisposableInnerBean(String innerBeanName) {
		Object innerBeanInstance = this.disposableInnerBeans.remove(innerBeanName);
		if (innerBeanInstance != null) {
			destroyBean(innerBeanName, innerBeanInstance);
		}
	}

	/**
	 * Invoke the specified custom destroy method on the given bean.
	 * <p>This implementation invokes a no-arg method if found, else checking
	 * for a method with a single boolean argument (passing in "true",
	 * assuming a "force" parameter), else logging an error.
	 * <p>Can be overridden in subclasses for custom resolution of destroy
	 * methods with arguments.
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
	// Abstract method to be implemented by concrete subclasses
	//---------------------------------------------------------------------

	/**
	 * Find bean instances that match the required type. Called by autowiring.
	 * If a subclass cannot obtain information about bean names by type,
	 * a corresponding exception should be thrown.
	 * @param requiredType the type of the beans to look up
	 * @return a Map of bean names and bean instances that match the required type,
	 * or null if none found
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected abstract Map findMatchingBeans(Class requiredType) throws BeansException;

}
