/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.support.BootstrapException;

/**
 * Convenient superclass for stateful session beans.
 * SFSBs should extend this class, leaving them to implement
 * the ejbActivate() and ejbPassivate() lifecycle methods
 * to comply with the requirements of the EJB specification.
 *
 * <p><b>Note: Subclasses should invoke the loadBeanFactory()
 * method in their custom ejbCreate() and ejbActivate methods,
 * and should invoke the unloadBeanFactory() method in their
 * ejbPassive method.</b>
 * 
 * <p><b>Note: Subclasses need to remove and restore the logger
 * instance from the superclass ('logger') in ejbPassivate/ejbActivate.</b>
 * 
 * <p><b>Note: The default BeanFactoryLoader used by this class's
 * superclass is <b>not</b> serializable. When using the default
 * BeanFactoryLoader, or another variant which is not serializable,
 * subclasses must call setBeanFactoryLoader(null) in ejbPassivate,
 * with a corresponding call to setBeanFactoryLoader(xxx) in 
 * ejbActivate unless relying on the default loader.
 * 
 * @version $Id: AbstractStatefulSessionBean.java,v 1.5 2003-12-12 19:23:39 colins Exp $
 * @author Rod Johnson
 * @author Colin Sampaleanu
 */
public abstract class AbstractStatefulSessionBean extends AbstractSessionBean {

	/**
	 * Load a Spring BeanFactory namespace. Exposed for subclasses
	 * to load a BeanFactory in their ejbCreate() methods. Those 
	 * callers would normally want to catch BootstrapException and
	 * rethrow it as {@link javax.ejb.CreateException}. Unless
	 * the BeanFactory is known to be serializable, this method
	 * must also be called from ejbActivate(), to reload a context
	 * removed via a call to unloadBeanFactory from ejbPassivate.
	 */
	protected void loadBeanFactory() throws BootstrapException {
		super.loadBeanFactory();
	}
	
	/**
	 * Unload the Spring BeanFactory instance.
	 * The default ejbRemove method invokes this method, but subclasses which
	 * override ejbRemove must invoke this method themselves. Unless
	 * the BeanFactory is known to be serializable, this method
	 * must also be called from ejbPassivate, with a corresponding call to 
	 * loadBeanFactory from ejbActivate.
	 */
	protected void unloadBeanFactory() throws FatalBeanException {
		super.unloadBeanFactory();
	}

}
