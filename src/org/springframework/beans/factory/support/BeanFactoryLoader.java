/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanFactory;

/**
 * Interface to be implemented by objects that can load BeanFactories
 * (usually on behalf of application components such as EJBs).
 * @author Rod Johnson
 * @since 20-Jul-2003
 * @version $Id: BeanFactoryLoader.java,v 1.1.1.1 2003-08-14 16:20:19 trisberg Exp $
 */
public interface BeanFactoryLoader {
	
	/**
	 * Load the BeanFactory.
	 * @return BeanFactory loaded BeanFactory. Never returns null. 
	 * @throws BootstrapException if a BeanFactory cannot be loaded
	 */
	BeanFactory loadBeanFactory() throws BootstrapException;

}
