/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.FactoryBean;

/**
 * Convenient superclass for FactoryBean implementations.
 * Exposes properties for singleton and PropertyValues.
 * <br>
 * There's no need for FactoryBean implementation to extend this class:
 * it's just easier in some cases.
 * <br>Although this class does not implement the PropertyValuesProviderFactoryBean
 * interface, it does provide  setPropertyValues() and getPropertyValues().
 * A subclass can merely choose to implement PropertyValuesProviderFactoryBean without
 * needing any code code, treating it as a tag interface.
 * Note that this implementation returns the same PropertyValues
 * for all beans.
 * @author Rod Johnson
 * @since 10-Mar-2003
 * @version $Revision: 1.2 $
 */
public abstract class AbstractFactoryBean implements FactoryBean {

	/**
	 * PropertyValues, if any, to be passed through and applied
	 * to new instances created by the factory. If this is null 
	 * (the default) no properties are set on the new instance.
	 * This is only meaningful if the subclass implements
	 * PassthroughFactoryBean
	 */
	private PropertyValues pvs;
	
	/**
	 * Default is for factories to return a singleton instance.
	 */
	private boolean singleton = true;


	/**
	 * @see org.springframework.beans.factory.PassthroughFactoryBean#getPropertyValues()
	 * Implementation of PassthroughFactoryBean interface. Not
	 * declared on this class, but subclass can choose to treat this
	 * interface as a tag interface. Only used if
	 * the subclass implements PassthroughFactoryBean (which 
	 * it can do without any additional code).
	 * This implementation will always return the same properties for
	 * all objects, ignoring the bean name parameter
	 * @param name name of the bean we're creating
	 */
	public PropertyValues getPropertyValues(String name) {
		return this.pvs;
	}

	/**
	 * Sets the PropertyValues, if any, to pass through to
	 * bean instances created by this factory. Only used if
	 * the subclass implements PassthroughFactoryBean (which 
	 * it can do without any additional code).
	 * @param pvs The pvs to set
	 */
	public void setPropertyValues(PropertyValues pvs) {
		this.pvs = pvs;
	}
	
	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return this.singleton;
	}

	/**
	 * Sets the singleton.
	 * @param singleton The singleton to set
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

}
