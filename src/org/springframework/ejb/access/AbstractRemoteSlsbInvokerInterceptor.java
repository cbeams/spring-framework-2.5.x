package org.springframework.ejb.access;

import javax.ejb.EJBObject;

/**
 * Superclass for interceptors proxying remote Stateless Session Beans.
 * @author Rod Johnson
 * @version $Id: AbstractRemoteSlsbInvokerInterceptor.java,v 1.3 2003-12-20 18:20:06 johnsonr Exp $
 */
public abstract class AbstractRemoteSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {
	
	/**
	 * Return a new instance of the stateless session bean.
	 * Can be overridden to change the algorithm.
	 * @return EJBObject
	 */
	protected EJBObject newSessionBeanInstance() {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to remote EJB");
		}

		// Invoke the superclass's generic create method
		EJBObject session = (EJBObject) create();
		// if it throws remote exception (wrapped in bean exception), retry?
	
		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to remote EJB: " + session);
		}
		return session;
	}

}
