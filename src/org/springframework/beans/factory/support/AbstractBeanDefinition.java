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
 * @author Rob Harrop
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
public abstract class AbstractBeanDefinition implements BeanDefinition {

	/**
	 * Constant that indicates no autowiring at all.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_NO = 0;

	/**
	 * Constant that indicates autowiring bean properties by name.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

	/**
	 * Constant that indicates autowiring bean properties by type.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

	/**
	 * Constant that indicates autowiring a constructor.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

	/**
	 * Constant that indicates determining an appropriate autowire strategy
	 * through introspection of the bean class.
	 * @see #setAutowireMode
	 */
	public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;


	/**
	 * Constant that indicates no dependency check at all.
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_NONE = 0;

	/**
	 * Constant that indicates dependency checking for object references.
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;

	/**
	 * Constant that indicates dependency checking for "simple" properties.
	 * @see #setDependencyCheck
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;

	/**
	 * Constant that indicates dependency checking for all properties
	 * (object references as well as "simple" properties).
	 * @see #setDependencyCheck
	 */
	public static final int DEPENDENCY_CHECK_ALL = 3;


	private Object beanClass;

	private boolean abstractFlag = false;

	private boolean singleton = true;

	private boolean lazyInit = false;

	private int autowireMode = AUTOWIRE_NO;

	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	private String[] dependsOn;

	private ConstructorArgumentValues constructorArgumentValues;

	private MutablePropertyValues propertyValues;

	private MethodOverrides methodOverrides = new MethodOverrides();

	private String factoryBeanName;

	private String factoryMethodName;

	private String initMethodName;

	private String destroyMethodName;

	private boolean enforceInitMethod = true;

	private boolean enforceDestroyMethod = true;

	private String resourceDescription;


	/**
	 * Create a new AbstractBeanDefinition with default settings.
	 */
	protected AbstractBeanDefinition() {
		this(null, null);
	}

	/**
	 * Create a new AbstractBeanDefinition with the given
	 * constructor argument values and property values.
	 */
	protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		setConstructorArgumentValues(cargs);
		setPropertyValues(pvs);
	}

	/**
	 * Create a new AbstractBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	protected AbstractBeanDefinition(AbstractBeanDefinition original) {
		this.beanClass = original.beanClass;

		setAbstract(original.isAbstract());
		setSingleton(original.isSingleton());
		setLazyInit(original.isLazyInit());
		setAutowireMode(original.getAutowireMode());
		setDependencyCheck(original.getDependencyCheck());
		setDependsOn(original.getDependsOn());

		setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
		setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
		setMethodOverrides(new MethodOverrides(original.getMethodOverrides()));

		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		setInitMethodName(original.getInitMethodName());
		setEnforceInitMethod(original.isEnforceInitMethod());
		setDestroyMethodName(original.getDestroyMethodName());
		setEnforceDestroyMethod(original.isEnforceDestroyMethod());

		setResourceDescription(original.getResourceDescription());
	}

	/**
	 * Override settings in this bean definition (assumably a copied parent
	 * from a parent-child inheritance relationship) from the given bean
	 * definition (assumably the child).
	 * <p><ul>
	 * <li>Will override beanClass if specified in the given bean definition.
	 * <li>Will always take abstract, singleton, lazyInit, autowireMode,
	 * dependencyCheck, dependsOn from the given bean definition.
	 * <li>Will add constructorArgumentValues, propertyValues, methodOverrides
	 * from the given bean definition to existing ones.
	 * <li>Will override factoryBeanName, factoryMethodName, initMethodName,
	 * destroyMethodName if specified in the given bean definition.
	 * </ul>
	 */
	public void overrideFrom(AbstractBeanDefinition other) {
		if (other.beanClass != null) {
			this.beanClass = other.beanClass;
		}

		setAbstract(other.isAbstract());
		setSingleton(other.isSingleton());
		setLazyInit(other.isLazyInit());
		setAutowireMode(other.getAutowireMode());
		setDependencyCheck(other.getDependencyCheck());
		setDependsOn(other.getDependsOn());

		getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
		getPropertyValues().addPropertyValues(other.getPropertyValues());
		getMethodOverrides().addOverrides(other.getMethodOverrides());

		if (other.getFactoryBeanName() != null) {
			setFactoryBeanName(other.getFactoryBeanName());
		}
		if (other.getFactoryMethodName() != null) {
			setFactoryMethodName(other.getFactoryMethodName());
		}
		if (other.getInitMethodName() != null) {
			setInitMethodName(other.getInitMethodName());
			setEnforceInitMethod(other.isEnforceInitMethod());
		}
		if (other.getDestroyMethodName() != null) {
			setDestroyMethodName(other.getDestroyMethodName());
			setEnforceDestroyMethod(other.isEnforceDestroyMethod());
		}

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
	 * <p>Default is "false". Specify true to tell the bean factory to not try to
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
	 * Set the autowire mode. This determines whether any automagical detection
	 * and setting of bean references will happen. Default is AUTOWIRE_NO,
	 * which means there's no autowire.
	 * @param autowireMode the autowire mode to set.
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
	 * Specify constructor argument values for this bean.
	 */
	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues =
				(constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
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
		this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
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
	 * Specify a factory method, if any. This method will be invoked with
	 * constructor arguments, or with no arguments if none are specified.
	 * The static method will be invoked on the specifed factory bean,
	 * if any, or on the local bean class else.
	 * @param factoryMethodName static factory method name, or <code>null</code> if
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
	 * Set the name of the initializer method. The default is <code>null</code>
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
	 * Specifies whether or not the configured init method is the default.
	 * Default value is <code>false</code>.
	 * @see #setInitMethodName
	 */
	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}

	/**
	 * Indicates whether the configured init method is the default.
	 * @see #getInitMethodName()
	 */
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	/**
	 * Set the name of the destroy method. The default is <code>null</code>
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
	 * Specifies whether or not the configured destroy method is the default.
	 * Default value is <code>false</code>.
	 * @see #setDestroyMethodName
	 */
	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}

	/**
	 * Indicates whether the configured destroy method is the default.
	 * @see #getDestroyMethodName
	 */
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
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
			throw new BeanDefinitionValidationException(
					"Lazy initialization is only applicable to singleton beans");
		}

		if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException(
			    "Cannot combine static factory method with method overrides: " +
			    "the static factory method must create the instance");
		}
		
		if (hasBeanClass()) {
			// Check that lookup methods exists
			for (Iterator it = getMethodOverrides().getOverrides().iterator(); it.hasNext(); ) {
				MethodOverride mo = (MethodOverride) it.next();
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
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException(
			    "Invalid method override: no method with name '" + mo.getMethodName() +
			    "' on class [" + getBeanClassName() + "]");
		}
		else if (count == 1) {
			// Mark override as not overloaded, to avoid the overhead of arg type checking.
			mo.setOverloaded(false);
		}
	}


	public String toString() {
		StringBuffer sb = new StringBuffer("class [");
		sb.append(getBeanClassName()).append("]");
		sb.append("; abstract=").append(this.abstractFlag);
		sb.append("; singleton=").append(this.singleton);
		sb.append("; lazyInit=").append(this.lazyInit);
		sb.append("; autowire=").append(this.autowireMode);
		sb.append("; dependencyCheck=").append(this.dependencyCheck);
		sb.append("; factoryBeanName=").append(this.factoryBeanName);
		sb.append("; factoryMethodName=").append(this.factoryMethodName);
		sb.append("; initMethodName=").append(this.initMethodName);
		sb.append("; destroyMethodName=").append(this.destroyMethodName);
		if (this.resourceDescription != null) {
			sb.append("; defined in ").append(this.resourceDescription);
		}
		return sb.toString();
	}
}
