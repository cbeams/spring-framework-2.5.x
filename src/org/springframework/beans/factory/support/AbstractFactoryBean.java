/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.PropertyValuesProviderFactoryBean;

/**
 * Convenient superclass for FactoryBean implementations.
 * Exposes properties for singleton and propertyValues.
 *
 * <p>There's no need for FactoryBean implementationa to extend this class:
 * It's just easier in some cases.
 *
 * <p>Although this class does not implement the PropertyValuesProviderFactoryBean
 * interface, it does provide  setPropertyValues() and getPropertyValues().
 * A subclass can merely choose to implement PropertyValuesProviderFactoryBean without
 * needing any code code, treating it as a tag interface.
 * Note that this implementation returns the same PropertyValues for all beans.
 *
 * @author Rod Johnson
 * @since 10-Mar-2003
 * @version $Revision: 1.3 $
 */
public abstract class AbstractFactoryBean implements PropertyValuesProviderFactoryBean {

	/**
	 * PropertyValues, if any, to be passed through and applied
	 * to new instances created by the factory. If this is null 
	 * (the default) no properties are set on the new instance.
	 * This is only meaningful if the subclass implements
	 * PropertyValuesProviderFactoryBean.
	 */
	private PropertyValues pvs;
	
	/**
	 * Default is for factories to return a singleton instance.
	 */
	private boolean singleton = true;


	/**
	 * Implementation of PropertyValuesProviderFactoryBean interface. Not declared on
	 * this class, but subclass can choose to treat this interface as a tag interface.
	 * Only used if the subclass implements PropertyValuesProviderFactoryBean (which
	 * it can do without any additional code). This implementation will always return
	 * the same properties for all objects, ignoring the bean name parameter.
	 * @param name name of the bean we're creating
	 * @see org.springframework.beans.factory.PropertyValuesProviderFactoryBean#getPropertyValues(String)
	 */
	public PropertyValues getPropertyValues(String name) {
		return this.pvs;
	}

	/**
	 * Set the PropertyValues, if any, to pass through to bean instances created by
	 * this factory. Only used if the subclass implements PassthroughFactoryBean
	 * (which it can do without any additional code).
	 * @param pvs the PropertyValues to set
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
	 * Set if the bean managed by this factory is a singleton.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

}
