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
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBObject;
import javax.naming.NamingException;

import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.remoting.RemoteLookupFailureException;
import org.springframework.remoting.rmi.RmiClientInterceptorUtils;

/**
 * <p>Basic invoker for a remote Stateless Session Bean.
 * "Creates" a new EJB instance for each invocation.
 * 
 * <p>See {@link org.springframework.jndi.JndiObjectLocator} for info on
 * how to specify the JNDI location of the target EJB.
 *
 * <p>In a bean container, this class is normally best used as a singleton. However,
 * if that bean container pre-instantiates singletons (as do the XML ApplicationContext
 * variants) you may have a problem if the bean container is loaded before the EJB
 * container loads the target EJB. That is because the JNDI lookup will be performed in
 * the init method of this class and cached, but the EJB will not have been bound at the
 * target location yet. The solution is to not pre-instantiate this factory object, but
 * allow it to be created on first use. In the XML containers, this is controlled via
 * the "lazy-init" attribute.
 *
 * <p>This invoker is typically used with an RMI business interface, which serves
 * as super-interface of the EJB component interface. Alternatively, this invoker
 * can also proxy a remote SLSB with a matching non-RMI business interface, i.e. an
 * interface that mirrors the EJB business methods but does not declare RemoteExceptions.
 * In the latter case, RemoteExceptions thrown by the EJB stub will automatically get
 * converted to Spring's unchecked RemoteAccessException.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 09-May-2003
 * @see org.springframework.remoting.RemoteAccessException
 */
public class SimpleRemoteSlsbInvokerInterceptor extends AbstractRemoteSlsbInvokerInterceptor {
	
	/**
	 * This implementation "creates" a new EJB instance for each invocation.
	 * Can be overridden for custom invocation strategies.
	 * <p>Alternatively, override getSessionBeanInstance and
	 * releaseSessionBeanInstance to change EJB instance creation,
	 * for example to hold a single shared EJB instance.
	 */
	protected Object doInvoke(MethodInvocation invocation) throws Throwable {
		EJBObject ejb = null;
		try {
			ejb = getSessionBeanInstance();
			return RmiClientInterceptorUtils.doInvoke(invocation, ejb);
		}
		catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			if (targetEx instanceof RemoteException) {
				throw RmiClientInterceptorUtils.convertRmiAccessException(
				    invocation.getMethod(), (RemoteException) targetEx, getJndiName());
			}
			else if (targetEx instanceof CreateException) {
				throw RmiClientInterceptorUtils.convertRmiAccessException(
				    invocation.getMethod(), targetEx, "Could not create remote EJB [" + getJndiName() + "]");
			}
			throw targetEx;
		}
		catch (NamingException ex) {
			throw new RemoteLookupFailureException("Failed to locate remote EJB [" + getJndiName() + "]", ex);
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to invoke remote EJB [" + getJndiName() + "]", ex);
		}
		finally {
			if (ejb != null) {
				releaseSessionBeanInstance(ejb);
			}
		}
	}

	/**
	 * Return an EJB instance to delegate the call to.
	 * Default implementation delegates to newSessionBeanInstance.
	 * @throws NamingException if thrown by JNDI
	 * @throws InvocationTargetException if thrown by the create method
	 * @see #newSessionBeanInstance
	 */
	protected EJBObject getSessionBeanInstance() throws NamingException, InvocationTargetException {
		return newSessionBeanInstance();
	}

	/**
	 * Release the given EJB instance.
	 * Default implementation delegates to removeSessionBeanInstance.
	 * @param ejb the EJB instance to release
	 * @see #removeSessionBeanInstance
	 */
	protected void releaseSessionBeanInstance(EJBObject ejb) {
		removeSessionBeanInstance(ejb);
	}

}
