/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

/** 
* Root bean definitions have a class and properties.
* @author Rod Johnson
* @version $Id: RootBeanDefinition.java,v 1.5 2003-10-31 17:01:27 jhoeller Exp $
*/
public class RootBeanDefinition extends AbstractBeanDefinition {

	/** Class of the wrapped object */
	private Class clazz;
	
	private String initMethodName;

	private String destroyMethodName;
	
	private BeanWrapper bw;

	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton,
	                          String initMethodName, String destroyMethodName) {
		super(pvs, singleton);
		this.clazz = clazz;
		this.initMethodName = initMethodName;
		this.destroyMethodName = destroyMethodName;
		this.bw = new BeanWrapperImpl(this.clazz);
	}

	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton, int autowire) {
		this(clazz, pvs, singleton, null, null);
		setAutowire(autowire);
	}
	
	public RootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton) {
		this(clazz, pvs, singleton, null, null);
	}

	/**
	 * Deep copy constructor.
	 */
	public RootBeanDefinition(RootBeanDefinition other) {
		super(new MutablePropertyValues(other.getPropertyValues()), other.isSingleton());
		this.setDependencyCheck(other.getDependencyCheck());
		this.setAutowire(other.getAutowire());
		this.clazz = other.clazz;
		this.initMethodName = other.initMethodName;
		this.destroyMethodName = other.destroyMethodName;
		this.bw = other.bw;
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
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName name of the bean
	 * @param ignoreTypes property types to ignore
	 * @throws UnsatisfiedDependencyException
	 */
	public void dependencyCheck(String beanName, Set ignoreTypes) throws UnsatisfiedDependencyException {
		if (getDependencyCheck() == DEPENDENCY_CHECK_NONE)
			return;

		PropertyDescriptor[] pds = this.bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			if (pds[i].getWriteMethod() != null &&
			    !ignoreTypes.contains(pds[i].getPropertyType()) &&
			    getPropertyValues().getPropertyValue(pds[i].getName()) == null) {
				boolean isSimple = BeanUtils.isSimpleProperty(pds[i].getPropertyType());
				boolean unsatisfied = getDependencyCheck() == DEPENDENCY_CHECK_ALL ||
					(isSimple && getDependencyCheck() == DEPENDENCY_CHECK_SIMPLE) ||
					(!isSimple && getDependencyCheck() == DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(beanName, pds[i].getName());
				}
			}
		}
	}

	/**
	 * Return an array of object-type property names that are unsatisfied.
	 * These are probably unsatisfied references to other beans in the
	 * factory. Does not include simple properties like primitives or Strings.
	 * @param ignoreTypes property types to ignore
	 * @return an array of object-type property names that are unsatisfied
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	public String[] unsatisfiedObjectProperties(Set ignoreTypes) {
		Set result = new TreeSet();
		PropertyDescriptor[] pds = this.bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			String name = pds[i].getName();
			if (pds[i].getWriteMethod() != null &&
			    !BeanUtils.isSimpleProperty(pds[i].getPropertyType()) &&
			    !ignoreTypes.contains(pds[i].getPropertyType()) &&
			    getPropertyValues().getPropertyValue(name) == null) {
				result.add(name);
			}
		}
		return (String[]) result.toArray(new String[result.size()]);
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
