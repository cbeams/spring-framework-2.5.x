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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.propertyeditors.ClassEditor;

/**
 * Convenient superclass for aspects/persistence API
 * configuration classes that should be able to autowire
 * objects into a factory.
 * 
 * Could also use attributes on persistent classes to identify those eligible for autowiring, or
 * even the prototype bean name.
 * 
 * @author Rod Johnson
 */
public abstract class DependencyInjectionAspectSupport implements InitializingBean, BeanFactoryAware {

	protected final Log log = LogFactory.getLog(getClass());
	
	private BeanFactory beanFactory;
	
	private AutowireCapableBeanFactory aabf;
	
	private Map managedClassToPrototypeMap = new HashMap();
	
	private int defaultAutowireMode = 0;
	
	/**
	 * List of Class
	 */
	protected List autowireByTypeClasses = new LinkedList();
	
	/**
	 * List of Class
	 */
	private List autowireByNameClasses = new LinkedList();

	/**
	 * @return Returns the autowireAll.
	 */
	public int getDefaultAutowireMode() {
		return defaultAutowireMode;
	}
	
	/**
	 * Convenient property enabling autowiring of all instances. We might want this in an
	 * AspectJ aspect subclass, for example, relying on the AspectJ aspect to target the
	 * advice.
	 * @param autowireAll The autowireAll to set.
	 */
	public void setDefaultAutowireMode(int mode) {
		if (mode != 0 && mode != AutowireCapableBeanFactory.AUTOWIRE_BY_NAME && mode != AbstractAutowireCapableBeanFactory.AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("defaultAutowireMode must be a constant on AutowireCapableBeanFactory: AUTOWIRE_BY_TYPE or AUTOWIRE_BY_NAME");
		}
		defaultAutowireMode = mode;
	}
	
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof AutowireCapableBeanFactory) {
			this.aabf = (AutowireCapableBeanFactory) beanFactory;
		}
	}

	public void setAutowireByTypeClasses(List autowireByTypeClasses) {
		this.autowireByTypeClasses = autowireByTypeClasses;
	}
	
	public List getAutowireByTypeClasses() {
		return autowireByTypeClasses;
	}

	public void addAutowireByTypeClass(Class clazz) {
		this.autowireByTypeClasses.add(clazz);
	}
	
	
	public void setAutowireByNameClasses(List autowireByNameClasses) {
		this.autowireByNameClasses = autowireByNameClasses;
	}
	
	public List getAutowireByNameClasses() {
		return autowireByNameClasses;
	}


	public void addAutowireByNameClass(Class clazz) {
		this.autowireByNameClasses.add(clazz);
	}

	public void setManagedClassNamesToPrototypeNames(Properties persistentClassBeanNames) {
		ClassEditor ce = new ClassEditor();
	
		for (Iterator i = persistentClassBeanNames.keySet().iterator(); i.hasNext();) {
			String className = (String) i.next();
			String beanName = persistentClassBeanNames.getProperty(className);
			ce.setAsText(className);
			Class clazz = (Class) ce.getValue();
			addManagedClassToPrototypeMapping(clazz, beanName);
		}
	}
	
	public Map getManagedClassToPrototypeNames() {
		return this.managedClassToPrototypeMap;
	}

	public void addManagedClassToPrototypeMapping(Class clazz, String beanName) {
		this.managedClassToPrototypeMap.put(clazz, beanName);
	}

	/**
	 * Check that mandatory properties were set
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public final void afterPropertiesSet() {
		if (beanFactory == null) {
			throw new IllegalArgumentException("beanFactory is required");
		}
	
		validateConfiguration();
		validateProperties();
	}
	
	protected abstract void validateProperties();

	protected void validateConfiguration() {
		if (managedClassToPrototypeMap.isEmpty() && autowireByTypeClasses.isEmpty() && autowireByNameClasses.isEmpty() && defaultAutowireMode == 0) {
			throw new IllegalArgumentException("Must set persistent class information");
		}
		
		if ((defaultAutowireMode !=0 || !autowireByTypeClasses.isEmpty() || !autowireByNameClasses.isEmpty()) && aabf == null) {
			throw new IllegalArgumentException("Autowiring supported only when running in an AutowireCapableBeanFactory");
		}
		
		// Check that all persistent classes map to prototype definitions
		for (Iterator itr = managedClassToPrototypeMap.keySet().iterator(); itr.hasNext(); ) {
			String beanName = (String) managedClassToPrototypeMap.get(itr.next());
			if (!beanFactory.containsBean(beanName)) {
				throw new IllegalArgumentException("No bean with name '" + beanName + "' defined in factory");
			}
			if (beanFactory.isSingleton(beanName)) {
				throw new IllegalArgumentException("Bean name '" + beanName + "' must be a prototype, with singleton=\"false\"");
			}
		}
		log.info("Validated " + managedClassToPrototypeMap.size() + " persistent class to prototype mappings");
	}

	/**
	 * Subclasses can call this to autowire properties on an existing object
	 * @param o
	 * @param autowireMode
	 * @throws BeansException
	 */
	protected void autowireProperties(Object o) throws NoAutowiringConfigurationForClassException, BeansException {
		if (autowireByTypeClasses.contains(o.getClass())) {
			aabf.autowireBeanProperties(o, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		}
		else if (autowireByNameClasses.contains(o.getClass())) {
			aabf.autowireBeanProperties(o, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		}
		else if (defaultAutowireMode == AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE) {
			aabf.autowireBeanProperties(o, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		}
		else if (defaultAutowireMode == AutowireCapableBeanFactory.AUTOWIRE_BY_NAME) {
			aabf.autowireBeanProperties(o, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		}
		else {
			throw new NoAutowiringConfigurationForClassException(o.getClass());
		}
		log.info("Autowired properties of persistent object with class=" + o.getClass().getName());
	}

	/**
	 * Subclasses will call this to create an object of the requisite class
	 * @param clazz
	 * @return
	 * @throws NoAutowiringConfigurationForClassException
	 */
	protected Object createAndConfigure(Class clazz) throws NoAutowiringConfigurationForClassException {
		Object o = null;
		String name = (String) managedClassToPrototypeMap.get(clazz);
		if (name != null) {
			o = beanFactory.getBean(name);
		}
		else {
			// Fall back to trying autowiring			
			o = BeanUtils.instantiateClass(clazz);
			autowireProperties(o);
		}
		
		if (o == null) {
			throw new NoAutowiringConfigurationForClassException(clazz);
		}
		else {
			return o;
		}
	}
	
	protected class NoAutowiringConfigurationForClassException extends Exception {
		public NoAutowiringConfigurationForClassException(Class clazz) {
			super(clazz + " cannot be autowired");
		}
	}

	
}
