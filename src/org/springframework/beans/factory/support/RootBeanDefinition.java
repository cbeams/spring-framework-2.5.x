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

import java.lang.reflect.Constructor;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;

/** 
 * Root bean definitions have a class plus optionally constructor argument
 * values and property values. This is the most common type of bean definition.
 *
 * <p>The autowire constants match the ones defined in the
 * AutowireCapableBeanFactory interface, adding AUTOWIRE_NO.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: RootBeanDefinition.java,v 1.22 2004-06-23 21:14:39 johnsonr Exp $
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

	public static final int AUTOWIRE_NO = 0;

	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;


	public static final int DEPENDENCY_CHECK_NONE = 0;

	public static final int DEPENDENCY_CHECK_OBJECTS = 1;

	public static final int DEPENDENCY_CHECK_SIMPLE = 2;

	public static final int DEPENDENCY_CHECK_ALL = 3;


	private Object beanClass;

	private ConstructorArgumentValues constructorArgumentValues;

	private int autowireMode = AUTOWIRE_NO;

	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	private String[] dependsOn;

	private String initMethodName;

	private String destroyMethodName;
	
	private String staticFactoryMethod;


	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode) {
		super(null);
		this.beanClass = beanClass;
		setAutowireMode(autowireMode);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode, boolean dependencyCheck) {
		super(null);
		this.beanClass = beanClass;
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
		super(pvs);
		this.beanClass = beanClass;
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 * @param singleton the singleton status of the bean
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
		super(pvs);
		this.beanClass = beanClass;
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(pvs);
		this.beanClass = beanClass;
		this.constructorArgumentValues = cargs;
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(pvs);
		this.beanClass = beanClassName;
		this.constructorArgumentValues = cargs;
	}

	/**
	 * Deep copy constructor.
	 */
	public RootBeanDefinition(RootBeanDefinition other) {
		super(new MutablePropertyValues(other.getPropertyValues()));
		this.beanClass = other.beanClass;
		this.constructorArgumentValues = other.constructorArgumentValues;
		setSingleton(other.isSingleton());
		setLazyInit(other.isLazyInit());
		setDependsOn(other.getDependsOn());
		setDependencyCheck(other.getDependencyCheck());
		setAutowireMode(other.getAutowireMode());
		setInitMethodName(other.getInitMethodName());
		setDestroyMethodName(other.getDestroyMethodName());
	}


	public ConstructorArgumentValues getConstructorArgumentValues() {
		return constructorArgumentValues;
	}

	/**
	 * Return if there are constructor argument values for this bean.
	 */
	public boolean hasConstructorArgumentValues() {
		return (constructorArgumentValues != null && !constructorArgumentValues.isEmpty());
	}

	/**
	 * Returns the class of the wrapped bean.
	 * @throws IllegalStateException if the bean definition does not carry
	 * a resolved bean class
	 */
	public final Class getBeanClass() throws IllegalStateException {
		if (!(this.beanClass instanceof Class)) {
			throw new IllegalStateException("Bean definition does not carry a resolved bean class");
		}
		return (Class) this.beanClass;
	}

	/**
	 * Returns the class name of the wrapped bean.
	 */
	public final String getBeanClassName() {
		if (this.beanClass instanceof Class) {
			return ((Class) this.beanClass).getName();
		}
		else {
			return (String) this.beanClass;
		}
	}

	/**
	 * Set the autowire code. This determines whether any automagical detection
	 * and setting of bean references will happen. Default is AUTOWIRE_NO,
	 * which means there's no autowire.
	 * @param autowireMode the autowire to set.
	 * Must be one of the constants defined in this class.
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 */
	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	/**
	 * Return the autowire mode as specified in the bean definition.
	 */
	public int getAutowireMode() {
		return autowireMode;
	}

	/**
	 * Return the resolved autowire code,
	 * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public int getResolvedAutowireMode() {
		if (this.autowireMode == AUTOWIRE_AUTODETECT) {
			// Work out whether to apply setter autowiring or constructor autowiring.
			// If it has a no-arg constructor it's deemed to be setter autowiring,
			// otherwise we'll try constructor autowiring.
			Constructor[] constructors = getBeanClass().getConstructors();
			for (int i = 0; i < constructors.length; i++) {
				if (constructors[i].getParameterTypes().length == 0) {
					return AUTOWIRE_BY_TYPE;
				}
			}
			return AUTOWIRE_CONSTRUCTOR;
		}
		else {
			return this.autowireMode;
		}
	}

	/**
	 * Set the dependency check code.
	 * @param dependencyCheck the code to set.
	 * Must be one of the four constants defined in this class.
	 * @see #DEPENDENCY_CHECK_NONE
	 * @see #DEPENDENCY_CHECK_OBJECTS
	 * @see #DEPENDENCY_CHECK_SIMPLE
	 * @see #DEPENDENCY_CHECK_ALL
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

	/**
	 * Return the dependency check code.
	 */
	public int getDependencyCheck() {
		return dependencyCheck;
	}

	/**
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized before.
	 * <p>Note that dependencies are normally expressed through bean properties or
	 * constructor arguments. This property should just be necessary for other kinds
	 * of dependencies like statics (*ugh*) or database preparation on startup.
	 */
	public void setDependsOn(String[] dependsOn) {
		this.dependsOn = dependsOn;
	}

	/**
	 * Return the bean names that this bean depends on.
	 */
	public String[] getDependsOn() {
		return dependsOn;
	}

	/**
	 * Set the name of the initializer method. The default is null
	 * in which case there is no initializer method.
	 */
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	/**
	 * Return the name of the initializer method.
	 */
	public String getInitMethodName() {
		return this.initMethodName;
	}

	/**
	 * Set the name of the destroy method. The default is null
	 * in which case there is no destroy method.
	 */
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	/**
	 * Return the name of the destroy method.
	 */
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}
	
	public void setStaticFactoryMethod(String staticFactoryMethod) {
		this.staticFactoryMethod = staticFactoryMethod;
	}
	
	/**
	 * String specifying a factory method, if one is specified.
	 * The method will be invoked with constructor arguments, or with
	 * no arguments if none is specified. The static method will be invoked
	 * on the beanClass specified.
	 * @return static factory method string, or null if normal
	 * constructor creation should be used.
	 */
	public String getStaticFactoryMethod() {
		return this.staticFactoryMethod;
	}

	public void validate() throws BeanDefinitionValidationException {
		super.validate();
				
		if (this.beanClass == null) {
			throw new BeanDefinitionValidationException("beanClass must be set in RootBeanDefinition");
		}
		if (this.beanClass instanceof Class) {
			if (FactoryBean.class.isAssignableFrom(getBeanClass()) && !isSingleton()) {
				throw new BeanDefinitionValidationException("FactoryBean must be defined as singleton - " +
						"FactoryBeans themselves are not allowed to be prototypes");
			}
		}
		
		// Check for illegal combinations of attributes
		if (!getMethodOverrides().isEmpty() && staticFactoryMethod != null) {
			throw new  BeanDefinitionValidationException("Cannot combine static factory method with method overrides: " +
					"the static factory method must create the instance");
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Root bean with class [");
		sb.append(getBeanClassName()).append(']');
		if (getResourceDescription() != null) {
			sb.append(" defined in ").append(getResourceDescription());
		}
		return sb.toString();
	}

}
