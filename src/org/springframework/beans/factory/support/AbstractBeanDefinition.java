/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.PropertyValues;

/**
 * Internal BeanFactory implementation class. This abstract base
 * class defines the BeanFactory type.Use a FactoryBean to
 * customize behaviour when returning application beans.
 *
 * <p> A BeanDefinition describes a bean instance,
 * which has property values and further information supplied
 * by concrete classes or subinterfaces.
 *
 * <p>Once configuration is complete, a BeanFactory will be able
 * to return direct references to objects defined by
 * BeanDefinitions.
 *
 * @author Rod Johnson
 * @version $Id: AbstractBeanDefinition.java,v 1.2 2003-09-03 23:41:39 johnsonr Exp $
 */
public abstract class AbstractBeanDefinition {
	
	public static final int DEPENDENCY_CHECK_NONE = 0;
	
	public static final int DEPENDENCY_CHECK_OBJECTS = 1;
	
	public static final int DEPENDENCY_CHECK_SIMPLE = 2;
	
	public static final int DEPENDENCY_CHECK_ALL = 3;

	/** Is this a singleton bean? */
	private boolean singleton;
	
	/** Property map */
	private PropertyValues pvs;
	
	/** A constant */
	private int dependencyCheck;
	
	/** 
	 * Creates new BeanDefinition
	 * @param pvs properties of the bean
	 */
	protected AbstractBeanDefinition(PropertyValues pvs, boolean singleton) {
		this.pvs = pvs;
		this.singleton = singleton;
	}
	
	/**
	 * Is this a <b>Singleton</b>, with a single, shared
	 * instance returned on all calls,
	 * or should the BeanFactory apply the <b>Prototype</b> design pattern,
	 * with each caller requesting an instance getting an independent
	 * instance? How this is defined will depend on the BeanFactory.
	 * "Singletons" are the commoner type.
	 * @return whether this is a Singleton
	 */
	public final boolean isSingleton() {
		return singleton;
	}
	

	/**
	 * Return the PropertyValues to be applied to a new instance
	 * of this bean.
	 * @return the PropertyValues to be applied to a new instance
	 * of this bean
	 */
	public PropertyValues getPropertyValues() {
		return pvs;
	}
	
	public void setPropertyValues(PropertyValues pvs) {
		this.pvs = pvs;
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof AbstractBeanDefinition))
			return false;
		AbstractBeanDefinition obd = (AbstractBeanDefinition) other;
		return this.singleton = obd.singleton &&
			this.pvs.changesSince(obd.pvs).getPropertyValues().length == 0;
	}

	/**
	 * @return int
	 */
	protected int getDependencyCheck() {
		return dependencyCheck;
	}

	/**
	 * Sets the dependencyCheck.
	 * @param dependencyCheck The dependencyCheck to set
	 */
	public void setDependencyCheck(int dependencyCheck) {
		this.dependencyCheck = dependencyCheck;
	}

}
