/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanCircularReferenceException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * Concrete implementation of ListableBeanFactory.
 * Can be used as a standalone bean factory,
 * or as a superclass for custom bean factories.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @version $Id: DefaultListableBeanFactory.java,v 1.15 2004-02-04 17:23:46 jhoeller Exp $
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	/* Whether to allow re-registration of a different definition with the same name */
	private boolean allowBeanDefinitionOverriding = true;

	/** Map of BeanDefinition objects, keyed by prototype name */
	private Map beanDefinitionMap = new HashMap();


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
		Set keys = this.beanDefinitionMap.keySet();
		Set matches = new HashSet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			if (type == null || type.isAssignableFrom(getMergedBeanDefinition(name, false).getBeanClass())) {
				matches.add(name);
			}
		}
		matches.addAll(Arrays.asList(getSingletonNames(type)));
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	public boolean containsBeanDefinition(String name) {
		return this.beanDefinitionMap.containsKey(name);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) throws BeansException {
		String[] beanNames = getBeanDefinitionNames(type);
		Map result = new HashMap();
		for (int i = 0; i < beanNames.length; i++) {
			if (includePrototypes || isSingleton(beanNames[i])) {
				result.put(beanNames[i], getBean(beanNames[i]));
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
				catch (FactoryBeanCircularReferenceException ex) {
					// we're currently creating that FactoryBean
					// sensible to ignore it, as we are just looking for a certain type
					logger.debug("Ignoring exception on FactoryBean type check", ex);
				}
			}
		}
		return result;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableListableBeanFactory
	//---------------------------------------------------------------------

	public void preInstantiateSingletons() {
		// Ensure that unreferenced singletons are instantiated
		if (logger.isInfoEnabled()) {
			logger.info("Pre-instantiating singletons in factory [" + this + "]");
		}
		String[] beanNames = getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			if (containsBeanDefinition(beanNames[i])) {
				RootBeanDefinition bd = getMergedBeanDefinition(beanNames[i], false);
				if (bd.isSingleton() && !bd.isLazyInit()) {
					if (FactoryBean.class.isAssignableFrom(bd.getBeanClass())) {
						FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanNames[i]);
						if (factory.isSingleton()) {
							getBean(beanNames[i]);
						}
					}
					else {
						getBean(beanNames[i]);
					}
				}
			}
		}
	}
	

	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	public void registerBeanDefinition(String name, AbstractBeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		try {
			beanDefinition.validate();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException("Validation of bean definition with name '" + name + "' failed", ex);
		}
		Object oldBeanDefinition = this.beanDefinitionMap.get(name);
		if (oldBeanDefinition != null) {
			if (!this.allowBeanDefinitionOverriding) {
				throw new BeanDefinitionStoreException("Cannot register bean definition [" + beanDefinition + "] for bean '" +
																							 name + "': there's already [" + oldBeanDefinition + "] bound");
			}
			else {
				logger.info("Overriding bean definition for bean '" + name +
										"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
			}
		}
		this.beanDefinitionMap.put(name, beanDefinition);
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	public AbstractBeanDefinition getBeanDefinition(String beanName) throws BeansException {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return bd;
	}

	protected String[] getDependingBeanNames(String beanName) throws BeansException {
		List dependingBeanNames = new ArrayList();
		String[] beanDefinitionNames = getBeanDefinitionNames();
		for (int i = 0; i < beanDefinitionNames.length; i++) {
			if (containsBeanDefinition(beanDefinitionNames[i])) {
				RootBeanDefinition bd = getMergedBeanDefinition(beanDefinitionNames[i], false);
				if (bd.getDependsOn() != null) {
					List dependsOn = Arrays.asList(bd.getDependsOn());
					if (dependsOn.contains(beanName)) {
						logger.debug("Found depending bean '" + beanDefinitionNames[i] + "' for bean '" + beanName + "'");
						dependingBeanNames.add(beanDefinitionNames[i]);
					}
				}
			}
		}
		return (String[]) dependingBeanNames.toArray(new String[dependingBeanNames.size()]);
	}

	protected Map findMatchingBeans(Class requiredType) {
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this, requiredType, true, true);
	}


	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + " defining beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]");
		if (getParentBeanFactory() == null) {
			sb.append("; Root of BeanFactory hierarchy");
		}
		else { 
			sb.append("; parent=<" + getParentBeanFactory() + ">");
		}
		return sb.toString();
	}

}
