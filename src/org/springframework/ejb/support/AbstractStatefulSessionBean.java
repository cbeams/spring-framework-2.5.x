/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ejb.support;

import org.springframework.beans.factory.support.BootstrapException;

/**
 * Convenient superclass for stateful session beans.
 * SFSBs should extend this class, leaving them to implement
 * the ejbActivate() and ejbPassivate() lifecycle methods
 * to comply with the requirements of the EJB specification.
 *
 * <p><b>NB: Subclasses should invoke the loadBeanFactory()
 * method in their custom ejbCreate() methods.</b>
 *
 * @version $Id: AbstractStatefulSessionBean.java,v 1.2 2003-11-14 20:19:33 colins Exp $
 * @author Rod Johnson
 */
public abstract class AbstractStatefulSessionBean extends AbstractSessionBean {

	/**
	 * Load a Spring BeanFactory namespace. Exposed for subclasses
	 * to load a BeanFactory in their ejbCreate() methods. Those 
	 * callers would normally want to catch BootstrapException and
	 * rethrow it as {@link javax.ejb.CreateException}.
	 */
	protected void loadBeanFactory() throws BootstrapException {
		super.loadBeanFactory();
	}

}
