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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * Concrete implementation of ListableBeanFactory.
 * Can be used as a standalone bean factory,
 * or as a superclass for custom bean factories.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	/* Whether to allow re-registration of a different definition with the same name */
	private boolean allowBeanDefinitionOverriding = true;

	/** Map of bean definition objects, keyed by bean name */
	private final Map beanDefinitionMap = new HashMap();

	/** List of bean definition names, in registration order */
	private final List beanDefinitionNames = new LinkedList();


	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 */
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Set if it should be allowed to override bean definitions by registering a
	 * different definition with the same name, automatically replacing the former.
	 * If not, an exception will be thrown. Default is true.
	 */
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanDefinitionNames(null);
	}

	/**
	 * Note that this method is slow. Don't invoke it too often:
	 * it's best used only in application initialization.
	 */
	public String[] getBeanDefinitionNames(Class type) {
		List matches = new ArrayList();
		Iterator it = this.beanDefinitionNames.iterator();
		while (it.hasNext()) {
			String beanName = (String) it.next();
			if (isBeanDefinitionTypeMatch(beanName, type)) {
				matches.add(beanName);
			}
		}
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	/**
	 * Determine whether the bean definition with the given name matches
	 * the given type.
	 * @param beanName the name of the bean to check
	 * @param type class or interface to match, or null for all bean names
	 * @return whether the type matches
	 */
	protected boolean isBeanDefinitionTypeMatch(String beanName, Class type) {
		if (type == null) {
			return true;
		}
		RootBeanDefinition rbd = getMergedBeanDefinition(beanName, false);
		return (rbd.hasBeanClass() && type.isAssignableFrom(rbd.getBeanClass()));
	}

	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
			throws BeansException {

		String[] beanNames = getBeanDefinitionNames(type);
		Map result = new HashMap();
		for (int i = 0; i < beanNames.length; i++) {
			if (includePrototypes || isSingleton(beanNames[i])) {
				try {
					result.put(beanNames[i], getBean(beanNames[i]));
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + beanNames[i] + "'");
					}
					// ignore
				}
				catch (BeanIsAbstractException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to abstract bean definition '" + beanNames[i] + "'");
					}
					// ignore
				}
			}
		}

		String[] singletonNames = getSingletonNames(type);
		for (int i = 0; i < singletonNames.length; i++) {
			if (!containsBeanDefinition(singletonNames[i])) {
				// directly registered singleton
				try {
					result.put(singletonNames[i], getBean(singletonNames[i]));
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + singletonNames[i] + "'");
					}
					// ignore
				}
			}
		}

		if (includeFactoryBeans) {
			String[] factoryNames = getBeanDefinitionNames(FactoryBean.class);
			for (int i = 0; i < factoryNames.length; i++) {
				try {
					FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + factoryNames[i]);
					Class objectType = factory.getObjectType();
					if ((objectType == null && factory.isSingleton()) ||
							((factory.isSingleton() || includePrototypes) &&
							objectType != null && type.isAssignableFrom(objectType))) {
						Object createdObject = getBean(factoryNames[i]);
						if (type.isInstance(createdObject)) {
							result.put(factoryNames[i], createdObject);
						}
					}
				}
				catch (BeanCurrentlyInCreationException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to currently created bean '" + factoryNames[i] + "'");
					}
					// ignore
				}
				catch (BeanIsAbstractException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring match to abstract bean definition '" + beanNames[i] + "'");
					}
					// ignore
				}
				catch (BeanCreationException ex) {
					// We're currently creating that FactoryBean.
					// Sensible to ignore it, as we are just looking for a certain type.
					if (logger.isDebugEnabled()) {
						logger.debug("Ignoring FactoryBean creation failure when looking for matching beans", ex);
					}
					// ignore
				}
			}
		}

		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory
	//---------------------------------------------------------------------

	public void preInstantiateSingletons() throws BeansException {
		if (logger.isInfoEnabled()) {
			logger.info("Pre-instantiating singletons in factory [" + this + "]");
		}
		try {
			for (Iterator it = this.beanDefinitionNames.iterator(); it.hasNext();) {
				String beanName = (String) it.next();
				if (containsBeanDefinition(beanName)) {
					RootBeanDefinition bd = getMergedBeanDefinition(beanName, false);
					if (bd.hasBeanClass() && !bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
						if (FactoryBean.class.isAssignableFrom(bd.getBeanClass())) {
							FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanName);
							if (factory.isSingleton()) {
								getBean(beanName);
							}
						}
						else {
							getBean(beanName);
						}
					}
				}
			}
		}
		catch (BeansException ex) {
			// destroy already created singletons to avoid dangling resources
			try {
				destroySingletons();
			}
			catch (Throwable ex2) {
				logger.error("preInstantiateSingletons failed but couldn't destroy already created singletons", ex2);
			}
			throw ex;
		}
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	public void registerBeanDefinition(String name, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), name,
				                                       "Validation of bean definition with name failed", ex);
			}
		}
		Object oldBeanDefinition = this.beanDefinitionMap.get(name);
		if (oldBeanDefinition != null) {
			if (!this.allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException("Cannot register bean definition [" + beanDefinition + "] for bean '" +
																							 name + "': there's already [" + oldBeanDefinition + "] bound");
			}
			else {
				if (logger.isInfoEnabled()) {
					logger.info("Overriding bean definition for bean '" + name +
											"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
				}
			}
		}
		else {
			this.beanDefinitionNames.add(name);
		}
		this.beanDefinitionMap.put(name, beanDefinition);
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
		BeanDefinition bd = (BeanDefinition) this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return bd;
	}

	protected Map findMatchingBeans(Class requiredType) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this, requiredType, true, true);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(" defining beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]");
		if (getParentBeanFactory() == null) {
			sb.append("; Root of BeanFactory hierarchy");
		}
		else {
			sb.append("; parent=<" + getParentBeanFactory() + ">");
		}
		return sb.toString();
	}

}
