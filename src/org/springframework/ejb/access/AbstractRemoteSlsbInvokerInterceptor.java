package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBObject;

/**
 * <p>Superclass for interceptors proxying remote Stateless Session Beans.</p>
 * 
 * @author Rod Johnson
 * @version $Id: AbstractRemoteSlsbInvokerInterceptor.java,v 1.5 2004-02-16 02:03:56 colins Exp $
 */
public abstract class AbstractRemoteSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {
	
	/**
	 * Return a new instance of the stateless session bean.
	 * Can be overridden to change the algorithm.
	 * @return EJBObject
	 */
	protected EJBObject newSessionBeanInstance() throws InvocationTargetException {
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
