/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Interface to be implemented by objects that can load a BeanFactory
 * (usually on behalf of application components such as EJBs).
 * @author Rod Johnson
 * @since 20-Jul-2003
 * @version $Id: BeanFactoryLoader.java,v 1.3 2004-01-01 23:48:32 jhoeller Exp $
 */
public interface BeanFactoryLoader {
	
	/**
	 * Load the BeanFactory.
	 * <p>In an EJB usage scenario this would normally be called from
	 * ejbCreate and ejbActivate.
	 * @return BeanFactory the loaded BeanFactory (must not be null)
	 * @throws BootstrapException if the BeanFactory cannot be loaded
	 */
	BeanFactory loadBeanFactory() throws BootstrapException;
	
	/**
	 * Unload the BeanFactory. This may possibly not actually do anything, or 
	 * alternately in the case of a 'closeable' BeanFactory or derived class
	 * (such as ApplicationContext) may 'close' it.
	 * <p>In an EJB usage scenario this would normally be called from
	 * ejbRemove and ejbPassivate.
	 * @throws FatalBeanException if the BeanFactory cannot be unloaded
	 */
	void unloadBeanFactory(BeanFactory beanFactory) throws FatalBeanException;

}
