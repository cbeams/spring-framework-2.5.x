/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;

/**
 * Common base class for bean definitions. Use a FactoryBean to
 * customize behaviour when returning application beans.
 *
 * <p>A BeanDefinition describes a bean instance, which has property values
 * and further information supplied by concrete classes or subinterfaces.
 *
 * <p>Once configuration is complete, a BeanFactory will be able
 * to return direct references to objects defined by BeanDefinitions.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AbstractBeanDefinition.java,v 1.10 2004-02-04 17:23:45 jhoeller Exp $
 */
public abstract class AbstractBeanDefinition {

	private MutablePropertyValues propertyValues;

	private boolean singleton = true;

	private boolean lazyInit = false;

	private String resourceDescription;


	/**
	 * Set the PropertyValues to be applied to a new instance of this bean.
	 */
	protected AbstractBeanDefinition(MutablePropertyValues pvs) {
		this.propertyValues = (pvs != null) ? pvs : new MutablePropertyValues();
	}

	/**
	 * Return the PropertyValues to be applied to a new instance of this bean.
	 */
	public MutablePropertyValues getPropertyValues() {
		return propertyValues;
	}
	
	/**
	 * Set if this a <b>Singleton</b>, with a single, shared instance returned
	 * on all calls. If false, the BeanFactory will apply the <b>Prototype</b>
	 * design pattern, with each caller requesting an instance getting an
	 * independent instance. How this is defined will depend on the BeanFactory.
	 * "Singletons" are the commoner type.
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls,
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
	 * Return whether this bean should be lazily initialized.
	 */
	public boolean isLazyInit() {
		return lazyInit;
	}

	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription = resourceDescription;
	}

	public String getResourceDescription() {
		return resourceDescription;
	}

	/**
	 * Validate this bean definition.
	 * @throws BeanDefinitionValidationException in case of validation failure
	 */
	public void validate() throws BeanDefinitionValidationException {
		if (this.lazyInit && !this.singleton) {
			throw new BeanDefinitionValidationException("Lazy initialization is just applicable to singleton beans");
		}
	}

}
