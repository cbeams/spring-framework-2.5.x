/**
 * The Spring framework is distributed under the Apache
 * Software License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.PropertyValues;

/**
 * Subinterface of FactoryBean that can pass property
 * values through to bean instances created by the factory. The owning
 * BeanFactory will do this automatically, based on the getPropertyValues()
 * interface from this method.
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b>
 *
 * @author Rod Johnson
 * @since September 9, 2003
 * @see org.springframework.beans.factory.BeanFactory
 * @version $Id: PropertyValuesProviderFactoryBean.java,v 1.1 2003-09-06 17:06:21 johnsonr Exp $
 */
public interface PropertyValuesProviderFactoryBean extends FactoryBean {

	/**
	 * Property values to pass to new bean instances created
	 * by this factory. Mapped directly onto the bean instance using
	 * reflection. This occurs <i>after</i> any configuration of the
	 * instance performed by the factory itself, and is an optional
	 * step within the control of the owning BeanFactory.
	 * @param name the name of the bean these properties should apply to.
	 * An implementation can always have one set of properties, 
	 * or a different one per bean.
	 * @return PropertyValues to pass to each new instance,
	 * or null (the default) if there are no properties to
	 * pass to the instance
	 */
	PropertyValues getPropertyValues(String name);

}
