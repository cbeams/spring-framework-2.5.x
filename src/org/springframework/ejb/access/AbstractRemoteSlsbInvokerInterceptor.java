/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ejb.access;

import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBObject;
import javax.naming.NamingException;

import org.aopalliance.aop.AspectException;

/**
 * Superclass for interceptors proxying remote Stateless Session Beans.
 *
 * <p>Such an interceptor must be the last interceptor in the advice chain.
 * In this case, there is no target object.
 *
 * @author Rod Johnson
 */
public abstract class AbstractRemoteSlsbInvokerInterceptor extends AbstractSlsbInvokerInterceptor {
	
	/**
	 * Return a new instance of the stateless session bean.
	 * Can be overridden to change the algorithm.
	 */
	protected EJBObject newSessionBeanInstance() throws NamingException, InvocationTargetException {
		if (logger.isDebugEnabled()) {
			logger.debug("Trying to create reference to remote EJB");
		}

		// invoke the superclass's generic create method
		Object ejbInstance = create();
		if (!(ejbInstance instanceof EJBObject)) {
			throw new AspectException("EJB instance [" + ejbInstance + "] is not a remote SLSB");
		}
		// if it throws remote exception (wrapped in bean exception), retry?

		if (logger.isDebugEnabled()) {
			logger.debug("Obtained reference to remote EJB: " + ejbInstance);
		}
		return (EJBObject) ejbInstance;
	}

}
