/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

/** 
* Root bean definitions have a class and properties.
* @author Rod Johnson
* @version $Id: RootBeanDefinition.java,v 1.2 2003-09-03 23:41:39 johnsonr Exp $
*/
public class RootBeanDefinition extends AbstractBeanDefinition {

	/** Class of the wrapped object */
	private Class clazz;
	
	private String initMethodName;

	private String destroyMethodName;

	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton,
	                          String initMethodName, String destroyMethodName) {
		super(pvs, singleton);
		this.clazz = clazz;
		this.initMethodName = initMethodName;
		this.destroyMethodName = destroyMethodName;
	}
	
	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton) {
		this(clazz, pvs, singleton, null, null);
	}
	
	/**
	 * Deep copy constructor.
	 */
	public RootBeanDefinition(RootBeanDefinition other) {
		super(new MutablePropertyValues(other.getPropertyValues()), other.isSingleton());
		this.clazz = other.clazz;
		this.initMethodName = other.initMethodName;
		this.destroyMethodName = other.destroyMethodName;
		this.setDependencyCheck(other.getDependencyCheck());
	}
	
	/**
	 * Returns the name of the initializer method. The default is null
	 * in which case there is no initializer method.
	 */
	public String getInitMethodName() {
		return this.initMethodName;
	}

	/**
	 * Returns the name of the destroy method. The default is null
	 * in which case there is no initializer method.
	 */
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

	/**
	 * Returns the class of the wrapped bean.
	 */
	public final Class getBeanClass() {
		return this.clazz;
	}

	/**
	 * Perform a dependency check that all properties exposed have
	 * been set, if desired.
	 * Dependency checks can be simple (primitives and String),
	 * object (collaborating beans)
	 * or all (both)
	 * @throws UnsatisfiedDependencyException
	 */
	public void dependencyCheck(String beanName) throws UnsatisfiedDependencyException {
		if (getDependencyCheck() == DEPENDENCY_CHECK_NONE) {
			return;
		}
		
		BeanWrapper bw = new BeanWrapperImpl(this.clazz);
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			String name = pds[i].getName();
			if (getPropertyValues().getPropertyValue(name) == null &&
					!pds[i].getName().equals("class")) {
				boolean isSimple = pds[i].getPropertyType().isPrimitive() || pds[i].getPropertyType().equals(String.class);
				boolean unsatisfied = getDependencyCheck() == DEPENDENCY_CHECK_ALL ||
					(isSimple && getDependencyCheck() == DEPENDENCY_CHECK_SIMPLE) ||
					(!isSimple && getDependencyCheck() == DEPENDENCY_CHECK_OBJECTS); 
				// The property isn't set
				if (unsatisfied) 
					throw new UnsatisfiedDependencyException(beanName, name);
			}
		}
	}
		
	public boolean equals(Object obj) {
		if (!(obj instanceof RootBeanDefinition))
			return false;
		return super.equals(obj) && ((RootBeanDefinition) obj).getBeanClass().equals(this.getBeanClass());
	}

	public String toString() {
		return "RootBeanDefinition for class '" + getBeanClass().getName() + "'; " + super.toString();
	}

}
