/**
 * The Spring framework is distributed under the Apache
 * Software License.
 */

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by objects used within a BeanFactory
 * that are themselves factories. If a bean implements this interface,
 * it is used as a factory, not directly as a bean.
 *
 * <p><b>NB: A bean that implements this interface cannot be used
 * as a normal bean.</b>
 *
 * <p>FactoryBeans can support singletons and prototypes.
 *
 * @author Rod Johnson
 * @since March 08, 2003
 * @see org.springframework.beans.factory.BeanFactory
 * @version $Id: FactoryBean.java,v 1.4 2003-10-28 11:54:07 jhoeller Exp $
 */
public interface FactoryBean {

	/**
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory. As with a BeanFactory, this allows
	 * support for both the Singleton and Prototype design pattern.
	 * @return an instance of the bean
	 */
	Object getObject() throws BeansException;

	/**
	 * Is the bean managed by this factory a singleton or a prototype?
	 * That is, will getObject() always return the same object?
	 * <p>The singleton status of the FactoryBean itself will
	 * generally be provided by the owning BeanFactory.
	 * @return if this bean is a singleton
	 */
	boolean isSingleton();

}
