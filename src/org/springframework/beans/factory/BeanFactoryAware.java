/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by beans that wish to be aware of their owning
 * BeanFactory. Beans can e.g. look up collaborating beans via the factory.
 *
 * <p>Note that most beans will choose to receive references to collaborating
 * beans via respective bean properties.
 *
 * <p>For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Rod Johnson
 * @since 11-Mar-2003
 * @version $Revision: 1.4 $
 * @see BeanNameAware
 * @see InitializingBean
 * @see BeanFactory
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware {
	
	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>Invoked after population of normal bean properties but before an init
	 * callback like InitializingBean's afterPropertiesSet or a custom init-method.
	 * @param beanFactory owning BeanFactory (may not be null).
	 * The bean can immediately call methods on the factory.
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
