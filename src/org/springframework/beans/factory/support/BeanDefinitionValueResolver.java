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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.CollectionFactory;

/**
 * Helper class for use by BeanFactory implementations,
 * resolving values contained in BeanDefinition objects
 * into the actual values applied to the target bean instance.
 *
 * <p>Works on an AbstractBeanFactory and a plain BeanDefinition object.
 * Used by AbstractAutowireCapableBeanFactory.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AbstractAutowireCapableBeanFactory
 */
class BeanDefinitionValueResolver {

	/**
	 * Separator for generated bean names. If a class name or parent name is not
	 * unique, "#1", "#2" etc will be appended, until the name becomes unique.
	 */
	public static final String GENERATED_BEAN_NAME_SEPARATOR =
			BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR;


	protected final Log logger = LogFactory.getLog(getClass());

	private final AbstractBeanFactory beanFactory;

	private final String beanName;

	private final BeanDefinition beanDefinition;


	/**
	 * Create a new BeanDefinitionValueResolver for the given BeanFactory
	 * and BeanDefinition.
	 * @param beanFactory the BeanFactory to resolve against
	 * @param beanName the name of the bean that we work on
	 * @param beanDefinition the BeanDefinition of the bean that we work on
	 */
	public BeanDefinitionValueResolver(
			AbstractBeanFactory beanFactory, String beanName, BeanDefinition beanDefinition) {

		this.beanName = beanName;
		this.beanDefinition = beanDefinition;
		this.beanFactory = beanFactory;
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
	 * <li>An ordinary object or <code>null</code>, in which case it's left alone.
	 */
	public Object resolveValueIfNecessary(String argName, Object value) throws BeansException {
		// We must check each value to see whether it requires a runtime reference
		// to another bean to be resolved.
		if (value instanceof BeanDefinitionHolder) {
			// Resolve BeanDefinitionHolder: contains BeanDefinition with name and aliases.
			BeanDefinitionHolder bdHolder = (BeanDefinitionHolder) value;
			return resolveInnerBeanDefinition(argName, bdHolder.getBeanName(), bdHolder.getBeanDefinition());
		}
		else if (value instanceof BeanDefinition) {
			// Resolve plain BeanDefinition, without contained name: use dummy name.
			BeanDefinition bd = (BeanDefinition) value;
			return resolveInnerBeanDefinition(argName, "(inner bean)", bd);
		}
		else if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			return resolveReference(argName, ref);
		}
		else if (value instanceof ManagedList) {
			// May need to resolve contained runtime references.
			return resolveManagedList(argName, (List) value);
		}
		else if (value instanceof ManagedSet) {
			// May need to resolve contained runtime references.
			return resolveManagedSet(argName, (Set) value);
		}
		else if (value instanceof ManagedMap) {
			// May need to resolve contained runtime references.
			return resolveManagedMap(argName, (Map) value);
		}
		else if (value instanceof ManagedProperties) {
			Properties copy = new Properties();
			copy.putAll((Properties) value);
			return copy;
		}
		else if (value instanceof TypedStringValue) {
			// Convert value to target type here.
			TypedStringValue typedStringValue = (TypedStringValue) value;
			try {
				Class resolvedTargetType = resolveTargetType(typedStringValue);
				return this.beanFactory.doTypeConversionIfNecessary(typedStringValue.getValue(), resolvedTargetType);
			}
			catch (Throwable ex) {
				// Improve the message by showing the context.
				throw new BeanCreationException(
						this.beanDefinition.getResourceDescription(), this.beanName,
						"Error converting typed String value for " + argName, ex);
			}
		}
		else {
			// No need to resolve value...
			return value;
		}
	}

	protected Class resolveTargetType(TypedStringValue value) throws ClassNotFoundException {
		if (value.hasTargetType()) {
			return value.getTargetType();
		}
		return value.resolveTargetType(this.beanFactory.getBeanClassLoader());
	}

	/**
	 * Resolve an inner bean definition.
	 */
	private Object resolveInnerBeanDefinition(
			String argName, String innerBeanName, BeanDefinition innerBd) throws BeansException {

		if (logger.isDebugEnabled()) {
			logger.debug("Resolving inner bean definition '" + innerBeanName + "' of bean '" + this.beanName + "'");
		}
		try {
			RootBeanDefinition mergedInnerBd = this.beanFactory.getMergedBeanDefinition(innerBeanName, innerBd);
			// Check given bean name whether it is unique. If not already unique,
			// add counter - increasing the counter until the name is unique.
			String actualInnerBeanName = adaptInnerBeanName(innerBeanName);
			Object innerBean = this.beanFactory.createBean(actualInnerBeanName, mergedInnerBd, null);
			if (mergedInnerBd.isSingleton()) {
				this.beanFactory.registerDependentBean(actualInnerBeanName, this.beanName);
			}
			return this.beanFactory.getObjectForBeanInstance(innerBean, actualInnerBeanName, mergedInnerBd);
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot create inner bean '" + innerBeanName + "' while setting " + argName, ex);
		}
	}

	/**
	 * Check given bean name whether it is unique. If not already unique,
	 * add counter - increasing the counter until the name is unique.
	 * @param innerBeanName the original name for the inner bean
	 * @return the adapted name for the inner bean
	 */
	private String adaptInnerBeanName(String innerBeanName) {
		String actualInnerBeanName = innerBeanName;
		int counter = 0;
		while (this.beanFactory.isBeanNameUsed(actualInnerBeanName)) {
			counter++;
			actualInnerBeanName = innerBeanName + GENERATED_BEAN_NAME_SEPARATOR + counter;
		}
		return actualInnerBeanName;
	}

	/**
	 * Resolve a reference to another bean in the factory.
	 */
	private Object resolveReference(String argName, RuntimeBeanReference ref) throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Resolving reference from property '" + argName + "' in bean '" +
					this.beanName + "' to bean '" + ref.getBeanName() + "'");
		}
		try {
			if (ref.isToParent()) {
				if (this.beanFactory.getParentBeanFactory() == null) {
					throw new BeanCreationException(
							this.beanDefinition.getResourceDescription(), this.beanName,
							"Can't resolve reference to bean '" + ref.getBeanName() +
							"' in parent factory: no parent factory available");
				}
				return this.beanFactory.getParentBeanFactory().getBean(ref.getBeanName());
			}
			else {
				if (this.beanDefinition.isSingleton()) {
					this.beanFactory.registerDependentBean(ref.getBeanName(), this.beanName);
				}
				return this.beanFactory.getBean(ref.getBeanName());
			}
		}
		catch (BeansException ex) {
			throw new BeanCreationException(
					this.beanDefinition.getResourceDescription(), this.beanName,
					"Cannot resolve reference to bean '" + ref.getBeanName() + "' while setting " + argName, ex);
		}
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	private List resolveManagedList(String argName, List ml) throws BeansException {
		List resolved = new ArrayList(ml.size());
		for (int i = 0; i < ml.size(); i++) {
			resolved.add(
			    resolveValueIfNecessary(
							argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
							ml.get(i)));
		}
		return resolved;
	}

	/**
	 * For each element in the ManagedList, resolve reference if necessary.
	 */
	private Set resolveManagedSet(String argName, Set ms) throws BeansException {
		Set resolved = CollectionFactory.createLinkedSetIfPossible(ms.size());
		int i = 0;
		for (Iterator it = ms.iterator(); it.hasNext();) {
			resolved.add(
			    resolveValueIfNecessary(
							argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX + i + BeanWrapper.PROPERTY_KEY_SUFFIX,
							it.next()));
			i++;
		}
		return resolved;
	}

	/**
	 * For each element in the ManagedMap, resolve reference if necessary.
	 */
	private Map resolveManagedMap(String argName, Map mm) throws BeansException {
		Map resolved = CollectionFactory.createLinkedMapIfPossible(mm.size());
		Iterator it = mm.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object resolvedKey = resolveValueIfNecessary(argName, entry.getKey());
			Object resolvedValue = resolveValueIfNecessary(
					argName + " with key " + BeanWrapper.PROPERTY_KEY_PREFIX + entry.getKey() + BeanWrapper.PROPERTY_KEY_SUFFIX,
					entry.getValue());
			resolved.put(resolvedKey, resolvedValue);
		}
		return resolved;
	}

}
