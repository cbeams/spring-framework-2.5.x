/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

/** 
* Root bean definitions have a class and properties.
* @author Rod Johnson
* @version $Id: RootBeanDefinition.java,v 1.4 2003-10-21 13:36:51 jhoeller Exp $
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

		// We'll just use this to look at descriptors, not make changes 
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
	
	
	/**
	 * Return a set of object-type property names that are unsatisfied
	 * @return  a set of object-type property names that are unsatisfied.
	 * These are probably unsatisfied references to other beans in the
	 * factory. Does not include properties of type String.
	 */
	public Set unsatisfiedObjectProperties() {
		Set s = new TreeSet();
		PropertyDescriptor[] pds = this.bw.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++) {
			String name = pds[i].getName();
			if (!pds[i].getPropertyType().isPrimitive()
				&& !pds[i].getName().equals("class")
				&& !pds[i].getPropertyType().equals(String.class)
				&& getPropertyValues().getPropertyValue(name) == null) {
				s.add(name);
			}
		}
		return s;
	}
	
	
	/**
	 * Fill in any missing property values with references to
	 * other beans in this factory if autowire is set to "byName".
	 * @param beanName name of the bean we're wiring up.
	 * Useful for debugging. Not used functionally.
	 */
	public void autowireByName(String beanName) {
		if (getAutowire() == AUTOWIRE_BY_NAME) {
			Set s = unsatisfiedObjectProperties();
			for (Iterator itr = s.iterator(); itr.hasNext(); ) {
				String propertyName = (String) itr.next();
				addPropertyValue(new PropertyValue(propertyName, new RuntimeBeanReference(propertyName)));
				logger.info("Added autowiring by name from bean name '" + beanName + 
					"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
			} // for each unsatisfied property
		} // if we should autowire by name
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
