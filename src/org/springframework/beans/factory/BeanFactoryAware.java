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
 * @author Rod Johnson
 * @since 11-Mar-2003
 * @version $Revision: 1.2 $
 */
public interface BeanFactoryAware {
	
	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>If the bean also implements InitializingBean, this method will
	 * be invoked after InitializingBean's <code>afterPropertiesSet</code>.
	 * @param beanFactory owning BeanFactory (may not be null).
	 * The bean can immediately call methods on the factory.
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
