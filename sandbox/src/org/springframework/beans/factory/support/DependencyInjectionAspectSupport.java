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
 * <p>
 * There are two ways of doing this: by mapping managed classes to prototype bean definitions
 * in the factory; and by identifying classes of which instances should be autowired into the factory
 * using the autowiring capables of AutowireCapableBeanFactory. If your managed class implements
 * Spring lifecycle interfaces such as BeanFactoryAware or ApplicationContextAware, you must use the
 * former method. With the latter method, only properties will be set, based on automatic satisfaction
 * of dependencies from other beans (singleton or non-singleton) defined in the factory. 
 * 
 * <p>Could also use attributes on persistent classes to identify those eligible for autowiring, or
 * even the prototype bean name.
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 * 
 * @since 1.2
 * @author Rod Johnson
 */
public abstract class DependencyInjectionAspectSupport implements InitializingBean, BeanFactoryAware {

	protected final Log log = LogFactory.getLog(getClass());
	
	private BeanFactory beanFactory;
	
	private AutowireCapableBeanFactory aabf;
	
	/**
	 * Map of Class to prototype name
	 */
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
	
	/**
	 * Expose the owning bean factory to subclasses. 
	 * Call only after initialization is complete.
	 * @return the owning bean factory. Cannot be null in valid use of this class.
	 */
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Set the Classes or class names that will be autowired by type
	 * @param autowireByTypeClasses list of Class or String classname
	 */
	public void setAutowireByTypeClasses(List autowireByTypeClasses) {
		this.autowireByTypeClasses = convertListFromStringsToClassesIfNecessary(autowireByTypeClasses);
	}
	
	/**
	 * Return classes autowired by type
	 * @return list of Class
	 */
	public List getAutowireByTypeClasses() {
		return autowireByTypeClasses;
	}

	public void addAutowireByTypeClass(Class clazz) {
		this.autowireByTypeClasses.add(clazz);
	}
	
	
	/**
	 * Set the Classes or class names that will be autowired by name
	 * @param autowireByNameClasses list of Class or String classname
	 */
	public void setAutowireByNameClasses(List autowireByNameClasses) {
		this.autowireByNameClasses = convertListFromStringsToClassesIfNecessary(autowireByNameClasses);
	}
	
	
	/**
	 * Return classes autowired by name
	 * @return list of Class
	 */
	public List getAutowireByNameClasses() {
		return autowireByNameClasses;
	}


	public void addAutowireByNameClass(Class clazz) {
		this.autowireByNameClasses.add(clazz);
	}
	
	
	/**
	 * Property key is class FQN, value is prototype name to use to obtain a new instance
	 * @param persistentClassBeanNames
	 */
	public void setManagedClassNamesToPrototypeNames(Properties persistentClassBeanNames) {			
		for (Iterator i = persistentClassBeanNames.keySet().iterator(); i.hasNext();) {
			String className = (String) i.next();
			String beanName = persistentClassBeanNames.getProperty(className);			
			addManagedClassToPrototypeMapping(classNameStringToClass(className), beanName);
		}
	}
	
	/**
	 * Utility method to convert a collection from a list of String class name to a list of Classes
	 * @param l list which may contain Class or String
	 * @return list of resolved Class instances
	 */
	private List convertListFromStringsToClassesIfNecessary(List l) {
	    List classes = new ArrayList(l.size());
		for (Iterator itr = l.iterator(); itr.hasNext();) {
            Object next = itr.next();
            if (next instanceof String) {
                next = classNameStringToClass((String) next);
            }
            classes.add(next);
        }
		return classes;
	}

	
	/**
	 * Resolve this FQN
	 * @param className name of the class to resolve
	 * @return the Class
	 */
	private Class classNameStringToClass(String className) {
	    ClassEditor ce = new ClassEditor();
	    ce.setAsText(className);
		return (Class) ce.getValue();
	}
	
	/**
	 * Return a Map of managed classes to prototype names
	 * @return Map with key being FQN and value prototype bean name to use for that class
	 */
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
	
	/**
	 * Subclasses should implement this to validate their configuration	 
	 */
	protected abstract void validateProperties();

	protected void validateConfiguration() {
		if (managedClassToPrototypeMap.isEmpty() && autowireByTypeClasses.isEmpty() && autowireByNameClasses.isEmpty() && defaultAutowireMode == 0) {
			throw new IllegalArgumentException("Must set persistent class information: no managed classes configured and no autowiring configuration or defaults");
		}
		
		if ((defaultAutowireMode != 0 || !autowireByTypeClasses.isEmpty() || !autowireByNameClasses.isEmpty()) && aabf == null) {
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
