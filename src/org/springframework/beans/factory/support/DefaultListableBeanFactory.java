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
 * @since 16 April 2001
 * @version $Id: DefaultListableBeanFactory.java,v 1.3 2003-11-28 16:51:09 jhoeller Exp $
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory
    implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Map of BeanDefinition objects, keyed by prototype name */
	private Map beanDefinitionMap = new HashMap();


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

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


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	public boolean containsBeanDefinition(String beanName) {
		return this.beanDefinitionMap.containsKey(beanName);
	}

	/**
	 * Subclasses or users should call this method to register new bean definitions
	 * with this class. All other registration methods in this class use this method.
	 * <p>This method isn't guaranteed to be threadsafe. It should be called
	 * before any bean instances are accessed.
	 * @param beanName name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeanDefinitionStoreException in the bean definition is invalid
	 */
	public void registerBeanDefinition(String beanName, AbstractBeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		try {
			beanDefinition.validate();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException("Validation of bean definition with name '" + beanName + "' failed", ex);
		}
		Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
		if (oldBeanDefinition != null) {
			throw new BeanDefinitionStoreException("Could not register bean definition [" + beanDefinition +
			                                       "] under bean name '" + beanName + "': there's already bean definition [" +
			                                       oldBeanDefinition + " bound");
		}
		this.beanDefinitionMap.put(beanName, beanDefinition);
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------

	protected AbstractBeanDefinition getBeanDefinition(String beanName) throws BeansException {
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
			RootBeanDefinition bd = getMergedBeanDefinition(beanDefinitionNames[i], false);
			if (bd.getDependsOn() != null) {
				List dependsOn = Arrays.asList(bd.getDependsOn());
				if (dependsOn.contains(beanName)) {
					logger.debug("Found depending bean '" + beanDefinitionNames[i] + "' for bean '" + beanName + "'");
					dependingBeanNames.add(beanDefinitionNames[i]);
				}
			}
		}
		return (String[]) dependingBeanNames.toArray(new String[dependingBeanNames.size()]);
	}

	protected Map findMatchingBeans(Class requiredType) {
		return getBeansOfType(requiredType, true, true);
	}


	public String toString() {
		return getClass().getName() + " with defined beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]";
	}

}
