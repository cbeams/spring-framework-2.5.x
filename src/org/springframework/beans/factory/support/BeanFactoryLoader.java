/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.support;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Interface to be implemented by objects that can load BeanFactories
 * (usually on behalf of application components such as EJBs).
 * @author Rod Johnson
 * @since 20-Jul-2003
 * @version $Id: BeanFactoryLoader.java,v 1.2 2003-12-07 23:23:07 colins Exp $
 */
public interface BeanFactoryLoader {
	
	/**
	 * Load the BeanFactory.
	 * <br/>In an EJB usage scenario this would normally be called from ejbCreate and
	 * ejbActivate. 
	 * @return BeanFactory loaded BeanFactory. Never returns null. 
	 * @throws BootstrapException if a BeanFactory cannot be loaded
	 */
	BeanFactory loadBeanFactory() throws BootstrapException;
	
	/**
	 * Unload the BeanFactory. This may possibly not actually do anything, or 
	 * alternately in the case of a 'closeable' BeanFactory or derived class
	 * (such as ApplicationContext) may 'close' it.
	 * <br/>In an EJB usage scenario this would normally be called from ejbRemove and
	 * ejbPassivate. 
	 * @throws FatalBeanException
	 */
	void unloadBeanFactory(BeanFactory bf) throws FatalBeanException;

}
