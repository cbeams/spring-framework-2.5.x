/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;

/** 
* Root bean definitions have a class and properties.
* @author Rod Johnson
* @version $Id: RootBeanDefinition.java,v 1.7 2003-11-05 11:28:15 jhoeller Exp $
*/
public class RootBeanDefinition extends AbstractBeanDefinition {

	public static final int DEPENDENCY_CHECK_NONE = 0;

	public static final int DEPENDENCY_CHECK_OBJECTS = 1;

	public static final int DEPENDENCY_CHECK_SIMPLE = 2;

	public static final int DEPENDENCY_CHECK_ALL = 3;

	public static final int AUTOWIRE_NO = 10;

	public static final int AUTOWIRE_BY_NAME = 11;

	public static final int AUTOWIRE_BY_TYPE = 12;


	private Class beanClass;
	
	private String initMethodName;

	private String destroyMethodName;
	
	private int dependencyCheck = DEPENDENCY_CHECK_NONE;

	private int autowire = AUTOWIRE_NO;

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 */
	public RootBeanDefinition(Class beanClass, PropertyValues pvs) {
		super(pvs);
		this.beanClass = beanClass;
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status.
	 */
	public RootBeanDefinition(Class beanClass, PropertyValues pvs, boolean singleton) {
		super(pvs);
		this.beanClass = beanClass;
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for the given parameters.
	 */
	public RootBeanDefinition(Class beanClass, PropertyValues pvs, boolean singleton, int dependencyCheck, int autowire) {
		super(pvs);
		this.beanClass = beanClass;
		setSingleton(singleton);
		setDependencyCheck(dependencyCheck);
		setAutowire(autowire);
	}
	
	/**
	 * Deep copy constructor.
	 */
	public RootBeanDefinition(RootBeanDefinition other) {
		super(new MutablePropertyValues(other.getPropertyValues()));
		this.beanClass = other.beanClass;
		setSingleton(other.isSingleton());
		setLazyInit(other.isLazyInit());
		setInitMethodName(other.getInitMethodName());
		setDestroyMethodName(other.getDestroyMethodName());
		setDependencyCheck(other.getDependencyCheck());
		setAutowire(other.getAutowire());
	}
	
	/**
	 * Returns the class of the wrapped bean.
	 */
	public final Class getBeanClass() {
		return this.beanClass;
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
	 * in which case there is no initializer method.
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
	 * Set the autowire code. This determines whether any automagical
	 * detection and setting of bean references will happen. Default
	 * is AUTOWIRE_NO constant, which means there's no autowire.
	 * @param autowire the autowire to set.
	 * Must be one of the three constants defined in this class.
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 */
	public void setAutowire(int autowire) {
		this.autowire = autowire;
	}

	/**
	 * Return the autowire code.
	 */
	public int getAutowire() {
		return this.autowire;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof RootBeanDefinition))
			return false;
		return super.equals(obj) && ((RootBeanDefinition) obj).getBeanClass().equals(this.getBeanClass());
	}

	public String toString() {
		return "Root bean definition with class [" + getBeanClass().getName() + "]";
	}

}
