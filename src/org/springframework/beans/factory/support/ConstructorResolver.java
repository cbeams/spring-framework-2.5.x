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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.MethodParameter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Helper class for resolving constructors and factory methods.
 * Performs constructor resolution through argument matching.
 *
 * <p>Operates on an {@link AbstractBeanFactory} and an {@link InstantiationStrategy}.
 * Used by {@link AbstractAutowireCapableBeanFactory}.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see #autowireConstructor
 * @see #instantiateUsingFactoryMethod
 * @see AbstractAutowireCapableBeanFactory
 */
abstract class ConstructorResolver {

	private final AbstractBeanFactory beanFactory;

	private final InstantiationStrategy instantiationStrategy;


	/**
	 * Create a new ConstructorResolver for the given factory and instantiation strategy.
	 * @param beanFactory the BeanFactory to work with
	 * @param instantiationStrategy the instantiate strategy for creating bean instances
	 */
	public ConstructorResolver(AbstractBeanFactory beanFactory, InstantiationStrategy instantiationStrategy) {
		this.beanFactory = beanFactory;
		this.instantiationStrategy = instantiationStrategy;
	}


	/**
	 * "autowire constructor" (with constructor arguments by type) behavior.
	 * Also applied if explicit constructor argument values are specified,
	 * matching all remaining arguments with beans from the bean factory.
	 * <p>This corresponds to constructor injection: In this mode, a Spring
	 * bean factory is able to host components that expect constructor-based
	 * dependency resolution.
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param chosenCtor a pre-chosen Constructor (or <code>null</code> if none)
	 * @return a BeanWrapper for the new instance
	 */
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor chosenCtor) {
		BeanWrapper bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);

		Constructor constructorToUse = (Constructor) mbd.resolvedConstructorOrFactoryMethod;
		Object[] argsToUse = null;

		if (constructorToUse != null) {
			// Found a cached constructor...
			argsToUse = mbd.resolvedConstructorArguments;
			if (argsToUse == null) {
				Class[] paramTypes = constructorToUse.getParameterTypes();
				Object[] argsToResolve = mbd.preparedConstructorArguments;
				BeanDefinitionValueResolver valueResolver =
						new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, bw);
				argsToUse = new Object[argsToResolve.length];
				for (int i = 0; i < argsToResolve.length; i++) {
					Object argValue = argsToResolve[i];
					if (argValue instanceof BeanMetadataElement) {
						String argName = "constructor argument with index " + i;
						argValue = valueResolver.resolveValueIfNecessary(argName, argValue);
					}
					argsToUse[i] = bw.convertIfNecessary(argValue, paramTypes[i],
							new MethodParameter(constructorToUse, i));
				}
			}
		}

		else {
			// Need to resolve the constructor.
			boolean autowiring = (chosenCtor != null ||
					mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);

			ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
			ConstructorArgumentValues resolvedValues = new ConstructorArgumentValues();
			int minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);

			// Take specified constructor, if any.
			if (chosenCtor != null) {
				Class[] paramTypes = chosenCtor.getParameterTypes();
				argsToUse = createArgumentArray(
						beanName, mbd, resolvedValues, bw, paramTypes, chosenCtor, autowiring).arguments;
				constructorToUse = chosenCtor;
			}

			else {
				Constructor[] candidates = mbd.getBeanClass().getDeclaredConstructors();
				AutowireUtils.sortConstructors(candidates);
				int minTypeDiffWeight = Integer.MAX_VALUE;

				for (int i = 0; i < candidates.length; i++) {
					Constructor candidate = candidates[i];
					Class[] paramTypes = candidate.getParameterTypes();

					if (constructorToUse != null && argsToUse.length > paramTypes.length) {
						// Already found greedy constructor that can be satisfied ->
						// do not look any further, there are only less greedy constructors left.
						break;
					}
					if (paramTypes.length < minNrOfArgs) {
						throw new BeanCreationException(mbd.getResourceDescription(), beanName,
								minNrOfArgs + " constructor arguments specified but no matching constructor found in bean '" +
								beanName + "' " +
								"(hint: specify index and/or type arguments for simple parameters to avoid type ambiguities)");
					}

					// Try to resolve arguments for current constructor.
					try {
						ArgumentsHolder args = createArgumentArray(
								beanName, mbd, resolvedValues, bw, paramTypes, candidate, autowiring);
						int typeDiffWeight = args.getTypeDifferenceWeight(paramTypes);
						// Choose this constructor if it represents the closest match.
						if (typeDiffWeight < minTypeDiffWeight) {
							constructorToUse = candidate;
							argsToUse = args.arguments;
							minTypeDiffWeight = typeDiffWeight;
						}
					}
					catch (UnsatisfiedDependencyException ex) {
						if (this.beanFactory.logger.isTraceEnabled()) {
							this.beanFactory.logger.trace(
									"Ignoring constructor [" + candidate + "] of bean '" + beanName + "': " + ex);
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
							mbd.getResourceDescription(), beanName, "Could not resolve matching constructor");
				}
			}

			mbd.resolvedConstructorOrFactoryMethod = constructorToUse;
		}

		Object beanInstance = this.instantiationStrategy.instantiate(
				mbd, beanName, this.beanFactory, constructorToUse, argsToUse);
		bw.setWrappedInstance(beanInstance);
		return bw;
	}

	/**
	 * Instantiate the bean using a named factory method. The method may be static, if the
	 * bean definition parameter specifies a class, rather than a "factory-bean", or
	 * an instance variable on a factory object itself configured using Dependency Injection.
	 * <p>Implementation requires iterating over the static or instance methods with the
	 * name specified in the RootBeanDefinition (the method may be overloaded) and trying
	 * to match with the parameters. We don't have the types attached to constructor args,
	 * so trial and error is the only way to go here. The explicitArgs array may contain
	 * argument values passed in programmatically via the corresponding getBean method.
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition for the bean
	 * @param explicitArgs argument values passed in programmatically via the getBean
	 * method, or <code>null</code> if none (-> use constructor argument values from bean definition)
	 * @return a BeanWrapper for the new instance
	 */
	public BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {
		BeanWrapper bw = new BeanWrapperImpl();
		this.beanFactory.initBeanWrapper(bw);

		Class factoryClass = null;
		Object factoryBean = null;
		boolean isStatic = true;

		if (mbd.getFactoryBeanName() != null) {
			factoryBean = this.beanFactory.getBean(mbd.getFactoryBeanName());
			factoryClass = factoryBean.getClass();
			isStatic = false;
		}
		else {
			// It's a static factory method on the bean class.
			factoryClass = mbd.getBeanClass();
		}

		Method factoryMethodToUse = (Method) mbd.resolvedConstructorOrFactoryMethod;
		Object[] argsToUse = null;

		if (factoryMethodToUse != null) {
			// Found a cached factory method...
			if (explicitArgs != null) {
				argsToUse = explicitArgs;
			}
			else {
				argsToUse = mbd.resolvedConstructorArguments;
				if (argsToUse == null) {
					Class[] paramTypes = factoryMethodToUse.getParameterTypes();
					Object[] argsToResolve = mbd.preparedConstructorArguments;
					BeanDefinitionValueResolver valueResolver =
							new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, bw);
					argsToUse = new Object[argsToResolve.length];
					for (int i = 0; i < argsToResolve.length; i++) {
						Object argValue = argsToResolve[i];
						if (argValue instanceof BeanMetadataElement) {
							String argName = "factory method argument with index " + i;
							argValue = valueResolver.resolveValueIfNecessary(argName, argValue);
						}
						argsToUse[i] = bw.convertIfNecessary(argValue, paramTypes[i],
								new MethodParameter(factoryMethodToUse, i));
					}
				}
			}
		}

		else {
			// Need to determine the factory method...
			// Try all methods with this name to see if they match the given arguments.
			Method[] candidates = ReflectionUtils.getAllDeclaredMethods(factoryClass);
			boolean autowiring = (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
			int minTypeDiffWeight = Integer.MAX_VALUE;
			ConstructorArgumentValues resolvedValues = null;

			int minNrOfArgs = 0;
			if (explicitArgs != null) {
				minNrOfArgs = explicitArgs.length;
			}
			else {
				// We don't have arguments passed in programmatically, so we need to resolve the
				// arguments specified in the constructor arguments held in the bean definition.
				ConstructorArgumentValues cargs = mbd.getConstructorArgumentValues();
				resolvedValues = new ConstructorArgumentValues();
				minNrOfArgs = resolveConstructorArguments(beanName, mbd, bw, cargs, resolvedValues);
			}

			for (int i = 0; i < candidates.length; i++) {
				Method candidate = candidates[i];
				Class[] paramTypes = candidate.getParameterTypes();

				if (Modifier.isStatic(candidate.getModifiers()) == isStatic &&
						candidate.getName().equals(mbd.getFactoryMethodName()) &&
						paramTypes.length >= minNrOfArgs) {

					ArgumentsHolder args = null;

					if (resolvedValues != null) {
						// Resolved contructor arguments: type conversion and/or autowiring necessary.
						try {
							args = createArgumentArray(
									beanName, mbd, resolvedValues, bw, paramTypes, candidate, autowiring);
						}
						catch (UnsatisfiedDependencyException ex) {
							if (this.beanFactory.logger.isTraceEnabled()) {
								this.beanFactory.logger.trace("Ignoring factory method [" + candidate +
										"] of bean '" + beanName + "': " + ex);
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
						if (paramTypes.length != explicitArgs.length) {
							continue;
						}
						args = new ArgumentsHolder(explicitArgs);
					}

					int typeDiffWeight = args.getTypeDifferenceWeight(paramTypes);
					// Choose this constructor if it represents the closest match.
					if (typeDiffWeight < minTypeDiffWeight) {
						factoryMethodToUse = candidate;
						argsToUse = args.arguments;
						minTypeDiffWeight = typeDiffWeight;
					}
				}
			}

			if (factoryMethodToUse == null) {
				throw new BeanDefinitionStoreException("No matching factory method found: " +
						(mbd.getFactoryBeanName() != null ?
						 "factory bean '" + mbd.getFactoryBeanName() + "'; " : "") +
						"factory method '" + mbd.getFactoryMethodName() + "'");
			}
			mbd.resolvedConstructorOrFactoryMethod = factoryMethodToUse;
		}

		Object beanInstance = this.instantiationStrategy.instantiate(
				mbd, beanName, this.beanFactory, factoryBean, factoryMethodToUse, argsToUse);
		if (beanInstance == null) {
			return null;
		}

		bw.setWrappedInstance(beanInstance);
		return bw;
	}

	/**
	 * Resolve the constructor arguments for this bean into the resolvedValues object.
	 * This may involve looking up other beans.
	 * This method is also used for handling invocations of static factory methods.
	 */
	private int resolveConstructorArguments(
			String beanName, RootBeanDefinition mbd, BeanWrapper bw,
			ConstructorArgumentValues cargs, ConstructorArgumentValues resolvedValues) {

		BeanDefinitionValueResolver valueResolver =
				new BeanDefinitionValueResolver(this.beanFactory, beanName, mbd, bw);

		int minNrOfArgs = cargs.getArgumentCount();

		for (Iterator it = cargs.getIndexedArgumentValues().entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			int index = ((Integer) entry.getKey()).intValue();
			if (index < 0) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName,
						"Invalid constructor argument index: " + index);
			}
			if (index > minNrOfArgs) {
				minNrOfArgs = index + 1;
			}
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) entry.getValue();
			if (valueHolder.isConverted()) {
				resolvedValues.addIndexedArgumentValue(index, valueHolder);
			}
			else {
				String argName = "constructor argument with index " + index;
				Object resolvedValue = valueResolver.resolveValueIfNecessary(argName, valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder =
						new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addIndexedArgumentValue(index, resolvedValueHolder);
			}
		}

		for (Iterator it = cargs.getGenericArgumentValues().iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder =
					(ConstructorArgumentValues.ValueHolder) it.next();
			if (valueHolder.isConverted()) {
				resolvedValues.addGenericArgumentValue(valueHolder);
			}
			else {
				String argName = "constructor argument";
				Object resolvedValue = valueResolver.resolveValueIfNecessary(argName, valueHolder.getValue());
				ConstructorArgumentValues.ValueHolder resolvedValueHolder =
						new ConstructorArgumentValues.ValueHolder(resolvedValue, valueHolder.getType());
				resolvedValueHolder.setSource(valueHolder);
				resolvedValues.addGenericArgumentValue(resolvedValueHolder);
			}
		}

		return minNrOfArgs;
	}

	/**
	 * Create an array of arguments to invoke a constructor or factory method,
	 * given the resolved constructor argument values.
	 */
	private ArgumentsHolder createArgumentArray(
			String beanName, RootBeanDefinition mbd, ConstructorArgumentValues resolvedValues,
			BeanWrapper bw, Class[] paramTypes, Object methodOrCtor, boolean autowiring)
			throws UnsatisfiedDependencyException {

		String methodType = (methodOrCtor instanceof Constructor ? "constructor" : "factory method");

		ArgumentsHolder args = new ArgumentsHolder(paramTypes.length);
		Set usedValueHolders = new HashSet(paramTypes.length);
		boolean resolveNecessary = false;

		for (int index = 0; index < paramTypes.length; index++) {
			// Try to find matching constructor argument value, either indexed or generic.
			ConstructorArgumentValues.ValueHolder valueHolder =
					resolvedValues.getArgumentValue(index, paramTypes[index], usedValueHolders);
			// If we couldn't find a direct match and are not supposed to autowire,
			// let's try the next generic, untyped argument value as fallback:
			// it could match after type conversion (for example, String -> int).
			if (valueHolder == null && !autowiring) {
				valueHolder = resolvedValues.getGenericArgumentValue(null, usedValueHolders);
			}
			if (valueHolder != null) {
				// We found a potential match - let's give it a try.
				// Do not consider the same value definition multiple times!
				usedValueHolders.add(valueHolder);
				args.rawArguments[index] = valueHolder.getValue();
				if (valueHolder.isConverted()) {
					Object convertedValue = valueHolder.getConvertedValue();
					args.arguments[index] = convertedValue;
					args.preparedArguments[index] = convertedValue;
				}
				else {
					try {
						Object originalValue = valueHolder.getValue();
						Object convertedValue = bw.convertIfNecessary(originalValue, paramTypes[index],
								MethodParameter.forMethodOrConstructor(methodOrCtor, index));
						args.arguments[index] = convertedValue;
						ConstructorArgumentValues.ValueHolder sourceHolder =
								(ConstructorArgumentValues.ValueHolder) valueHolder.getSource();
						Object sourceValue = sourceHolder.getValue();
						if (originalValue == sourceValue || sourceValue instanceof TypedStringValue) {
							// Either a converted value or still the original one: store converted value.
							sourceHolder.setConvertedValue(convertedValue);
							args.preparedArguments[index] = convertedValue;
						}
						else {
							resolveNecessary = true;
							args.preparedArguments[index] = sourceValue;
						}
					}
					catch (TypeMismatchException ex) {
						throw new UnsatisfiedDependencyException(
								mbd.getResourceDescription(), beanName, index, paramTypes[index],
								"Could not convert " + methodType + " argument value of type [" +
								ObjectUtils.nullSafeClassName(valueHolder.getValue()) +
								"] to required type [" + paramTypes[index].getName() + "]: " + ex.getMessage());
					}
				}
			}
			else {
				// No explicit match found: we're either supposed to autowire or
				// have to fail creating an argument array for the given constructor.
				if (!autowiring) {
					throw new UnsatisfiedDependencyException(
							mbd.getResourceDescription(), beanName, index, paramTypes[index],
							"Ambiguous " + methodType + " argument types - " +
							"did you specify the correct bean references as " + methodType + " arguments?");
				}
				Map matchingBeans = findAutowireCandidates(beanName, paramTypes[index]);
				if (matchingBeans.size() != 1) {
					throw new UnsatisfiedDependencyException(
							mbd.getResourceDescription(), beanName, index, paramTypes[index],
							"There are " + matchingBeans.size() + " beans of type [" + paramTypes[index].getName() +
							"] available for autowiring: " + matchingBeans.keySet() +
							". There should have been exactly 1 to be able to autowire " +
							methodType + " of bean '" + beanName + "'.");
				}
				Map.Entry entry = (Map.Entry) matchingBeans.entrySet().iterator().next();
				String autowiredBeanName = (String) entry.getKey();
				Object autowiredBean = entry.getValue();
				args.rawArguments[index] = autowiredBean;
				args.arguments[index] = autowiredBean;
				if (mbd.isSingleton()) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				if (this.beanFactory.logger.isDebugEnabled()) {
					this.beanFactory.logger.debug("Autowiring by type from bean name '" + beanName +
							"' via " + methodType + " to bean named '" + autowiredBeanName + "'");
				}
			}
		}

		if (resolveNecessary) {
			mbd.preparedConstructorArguments = args.preparedArguments;
		}
		else {
			mbd.resolvedConstructorArguments = args.arguments;
		}
		return args;
	}


	/**
	 * Find bean instances that match the required type.
	 * Called during autowiring for the specified bean.
	 * <p>If a subclass cannot obtain information about bean names by type,
	 * a corresponding exception should be thrown.
	 * @param beanName the name of the bean that is about to be wired
	 * @param requiredType the type of the autowired constructor argument
	 * @return a Map of candidate names and candidate instances that match
	 * the required type (never <code>null</code>)
	 * @throws BeansException in case of errors
	 * @see #autowireConstructor
	 */
	protected abstract Map findAutowireCandidates(String beanName, Class requiredType) throws BeansException;


	/**
	 * Private inner class for holding argument combinations.
	 */
	private static class ArgumentsHolder {

		public Object rawArguments[];

		public Object arguments[];

		public Object preparedArguments[];

		public ArgumentsHolder(int size) {
			this.rawArguments = new Object[size];
			this.arguments = new Object[size];
			this.preparedArguments = new Object[size];
		}

		public ArgumentsHolder(Object[] args) {
			this.rawArguments = args;
			this.arguments = args;
			this.preparedArguments = args;
		}

		public int getTypeDifferenceWeight(Class[] paramTypes) {
			// If valid arguments found, determine type difference weight.
			// Try type difference weight on both the converted arguments and
			// the raw arguments. If the raw weight is better, use it.
			// Decrease raw weight by 1024 to prefer it over equal converted weight.
			int typeDiffWeight = AutowireUtils.getTypeDifferenceWeight(paramTypes, this.arguments);
			int rawTypeDiffWeight = AutowireUtils.getTypeDifferenceWeight(paramTypes, this.rawArguments) - 1024;
			return (rawTypeDiffWeight < typeDiffWeight ? rawTypeDiffWeight : typeDiffWeight);
		}
	}

}
