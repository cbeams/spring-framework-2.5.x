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
import java.util.Iterator;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.ClassUtils;

/**
 * Base class for bean definition objects, factoring out common
 * properties of RootBeanDefinition and ChildBeanDefinition.
 *
 * <p>The autowire constants match the ones defined in the
 * AutowireCapableBeanFactory interface, adding AUTOWIRE_NO.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
public abstract class AbstractBeanDefinition implements BeanDefinition {

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

	private boolean abstractFlag = false;

	private boolean singleton = true;

	private boolean lazyInit = false;

	private ConstructorArgumentValues constructorArgumentValues;

	private MutablePropertyValues propertyValues;

	private MethodOverrides methodOverrides = new MethodOverrides();

	private String initMethodName;

	private String destroyMethodName;

	private String factoryMethodName;
	
	private String factoryBeanName;

	private int autowireMode = AUTOWIRE_NO;

	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	private String[] dependsOn;

	private String resourceDescription;


	protected AbstractBeanDefinition() {
		setConstructorArgumentValues(new ConstructorArgumentValues());
		setPropertyValues(new MutablePropertyValues());
	}

	/**
	 * Deep copy constructor.
	 */
	protected AbstractBeanDefinition(AbstractBeanDefinition other) {
		this.beanClass = other.beanClass;

		setAbstract(other.isAbstract());
		setSingleton(other.isSingleton());
		setLazyInit(other.isLazyInit());

		setConstructorArgumentValues(new ConstructorArgumentValues(other.getConstructorArgumentValues()));
		setPropertyValues(new MutablePropertyValues(other.getPropertyValues()));
		setMethodOverrides(new MethodOverrides(other.getMethodOverrides()));

		setInitMethodName(other.getInitMethodName());
		setDestroyMethodName(other.getDestroyMethodName());
		setFactoryMethodName(other.getFactoryMethodName());
		setFactoryBeanName(other.getFactoryBeanName());

		setDependsOn(other.getDependsOn());
		setAutowireMode(other.getAutowireMode());
		setDependencyCheck(other.getDependencyCheck());

		setResourceDescription(other.getResourceDescription());
	}

	/**
	 * Override settings in this bean definition from the given bean definition.
	 * <p><ul>
	 * <li>Will override beanClass if specified in the given bean definition.
	 * <li>Will always take abstract, singleton, lazyInit from the given bean definition.
	 * <li>Will add constructorArgumentValues, propertyValues, methodOverrides to
	 * existing ones.
	 * <li>Will override initMethodName, destroyMethodName, staticFactoryMethodName
	 * if specified.
	 * <li>Will always take dependsOn, autowireMode, dependencyCheck from the
	 * given bean definition.
	 * </ul>
	 */
	public void overrideFrom(AbstractBeanDefinition other) {
		if (other.beanClass != null) {
			this.beanClass = other.beanClass;
		}

		setAbstract(other.isAbstract());
		setSingleton(other.isSingleton());
		setLazyInit(other.isLazyInit());

		getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
		getPropertyValues().addPropertyValues(other.getPropertyValues());
		getMethodOverrides().addOverrides(other.getMethodOverrides());

		if (other.getInitMethodName() != null) {
			setInitMethodName(other.getInitMethodName());
		}
		if (other.getDestroyMethodName() != null) {
			setDestroyMethodName(other.getDestroyMethodName());
		}
		if (other.getFactoryMethodName() != null) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		if (other.getFactoryBeanName() != null) {
			setFactoryBeanName(other.getFactoryBeanName());
		}

		setDependsOn(other.getDependsOn());
		setAutowireMode(other.getAutowireMode());
		setDependencyCheck(other.getDependencyCheck());

		setResourceDescription(other.getResourceDescription());
	}


	/**
	 * Return whether this definitions specifies a bean class.
	 */
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}

	/**
	 * Specify the class for this bean.
	 */
	public void setBeanClass(Class beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * Return the class of the wrapped bean.
	 * @throws IllegalStateException if the bean definition
	 * does not carry a resolved bean class
	 */
	public Class getBeanClass() throws IllegalStateException {
		if (!(this.beanClass instanceof Class)) {
			throw new IllegalStateException("Bean definition does not carry a resolved bean class");
		}
		return (Class) this.beanClass;
	}

	/**
	 * Specify the class name for this bean.
	 */
	public void setBeanClassName(String beanClassName) {
		this.beanClass = beanClassName;
	}

	/**
	 * Return the class name of the wrapped bean.
	 */
	public String getBeanClassName() {
		if (this.beanClass instanceof Class) {
			return ((Class) this.beanClass).getName();
		}
		else {
			return (String) this.beanClass;
		}
	}

	/**
	 * Set if this bean is "abstract", i.e. not meant to be instantiated itself but
	 * rather just serving as parent for concrete child bean definitions.
	 * <p>Default is false. Specify true to tell the bean factory to not try to
	 * instantiate that particular bean in any case.
	 */
	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}

	/**
	 * Return whether this bean is "abstract", i.e. not meant to be instantiated
	 * itself but rather just serving as parent for concrete child bean definitions.
	 */
	public boolean isAbstract() {
		return abstractFlag;
	}

	/**
	 * Set if this a <b>Singleton</b>, with a single, shared instance returned
	 * on all calls. If false, the BeanFactory will apply the <b>Prototype</b>
	 * design pattern, with each caller requesting an instance getting an
	 * independent instance. How this is defined will depend on the BeanFactory.
	 * <p>"Singletons" are the commoner type, so the default is true.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 */
	public boolean isSingleton() {
		return singleton;
	}

	/**
	 * Set whether this bean should be lazily initialized.
	 * Only applicable to a singleton bean.
	 * If false, it will get instantiated on startup by bean factories
	 * that perform eager initialization of singletons.
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 */
	public boolean isLazyInit() {
		return lazyInit;
	}

	/**
	 * Specify constructor argument values for this bean.
	 */
	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues = (constructorArgumentValues != null) ?
		    constructorArgumentValues : new ConstructorArgumentValues();
	}

	/**
	 * Return constructor argument values for this bean, if any.
	 */
	public ConstructorArgumentValues getConstructorArgumentValues() {
		return constructorArgumentValues;
	}

	/**
	 * Return if there are constructor argument values defined for this bean.
	 */
	public boolean hasConstructorArgumentValues() {
		return (constructorArgumentValues != null && !constructorArgumentValues.isEmpty());
	}

	/**
	 * Specify property values for this bean, if any.
	 */
	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = (propertyValues != null) ? propertyValues : new MutablePropertyValues();
	}

	/**
	 * Return property values for this bean, if any.
	 */ 
	public MutablePropertyValues getPropertyValues() {
		return propertyValues;
	}

	/**
	 * Specify method overrides for the bean, if any.
	 */
	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = (methodOverrides != null) ? methodOverrides : new MethodOverrides();
	}

	/**
	 * Return information about methods to be overridden by the IoC
	 * container. This will be empty if there are no method overrides.
	 * Never returns null.
	 */
	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
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

	/**
	 * Specify a factory method, if any. This method will be invoked with
	 * constructor arguments, or with no arguments if none are specified.
	 * The static method will be invoked on the specifed beanClass.
	 * @param factoryMethodName static factory method name, or null if
	 * normal constructor creation should be used
	 * @see #getBeanClass
	 */
	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	/**
	 * Return a factory method, if any.
	 */
	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	/**
	 * Specify the factory bean to use, if any.
	 */
	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	/**
	 * Returns the factory bean name, if any.
	 */
	public String getFactoryBeanName() {
		return factoryBeanName;
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
	 * Set a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription = resourceDescription;
	}

	/**
	 * Return a description of the resource that this bean definition
	 * came from.
	 */
	public String getResourceDescription() {
		return resourceDescription;
	}


	/**
	 * Validate this bean definition.
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void validate() throws BeanDefinitionValidationException {
		if (this.lazyInit && !this.singleton) {
			throw new BeanDefinitionValidationException("Lazy initialization is applicable only to singleton beans");
		}

		if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
			throw new  BeanDefinitionValidationException(
			    "Cannot combine static factory method with method overrides: " +
			    "the static factory method must create the instance");
		}
		
		if (hasBeanClass()) {
			// Check that lookup methods exists
			for (Iterator itr = getMethodOverrides().getOverrides().iterator(); itr.hasNext(); ) {
				MethodOverride mo = (MethodOverride) itr.next();
				validateMethodOverride(mo);
			}
		}
	}

	/**
	 * Validate the given method override.
	 * Checks for existence of a method with the specified name.
	 * @param mo the MethodOverride object to validate
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	protected void validateMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
		if (!ClassUtils.hasAtLeastOneMethodWithName(getBeanClass(), mo.getMethodName())) {
			throw new BeanDefinitionValidationException(
			    "Invalid method override: no method with name '" + mo.getMethodName() +
			    "' on class [" + getBeanClassName() + "]");
		}
	}

}
