/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new TopLink Session to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already is a pre-bound Session (e.g. from TopLinkTransactionManager,
 * or from a surrounding TopLink-intercepted method), the interceptor simply
 * takes part in it.
 *
 * <p>Application code must retrieve a TopLink Session via the
 * <code>SessionFactoryUtils.getSession</code> method, to be able to detect a
 * thread-bound Session. It is preferable to use <code>getSession</code> with
 * allowCreate=false, if the code relies on the interceptor to provide proper
 * Session handling. Typically, the code will look as follows:
 *
 * <pre>
 * public void doTopLinkAction() {
 *   Session session = SessionFactoryUtils.getSession(this.sessionFactory, false);
 *   try {
 *     ...
 *   }
 *   catch (TopLinkException ex) {
 *     throw SessionFactoryUtils.convertTopLinkAccessException(ex);
 *   }
 * }</pre>
 *
 * Note that the application must care about handling TopLinkExceptions itself,
 * preferably via delegating to the <code>SessionFactoryUtils.convertTopLinkAccessException</code>
 * method that converts them to exceptions that are compatible with the
 * <code>org.springframework.dao</code> exception hierarchy (like TopLinkTemplate does).
 *
 * <p>This class can be considered a declarative alternative to TopLinkTemplate's
 * callback approach. The advantages are:
 * <ul>
 * <li>no anonymous classes necessary for callback implementations;
 * <li>the possibility to throw any application exceptions from within data access code.
 * </ul>
 *
 * <p>The drawbacks are:
 * <ul>
 * <li>the dependency on interceptor configuration;
 * <li>the delegating try/catch blocks.
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public class TopLinkInterceptor extends TopLinkAccessor implements MethodInterceptor {

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean existingTransaction = false;
		Session session = SessionFactoryUtils.getSession(getSessionFactory(), true);
		if (TransactionSynchronizationManager.hasResource(getSessionFactory())) {
			logger.debug("Found thread-bound Session for TopLink interceptor");
			existingTransaction = true;
		}
		else {
			logger.debug("Using new Session for TopLink interceptor");
			TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
		}
		try {
			return methodInvocation.proceed();
		}
		catch (TopLinkException ex) {
			throw convertTopLinkAccessException(ex);
		}
		finally {
			if (existingTransaction) {
				logger.debug("Not closing pre-bound TopLink Session after interceptor");
			}
			else {
				TransactionSynchronizationManager.unbindResource(getSessionFactory());
				SessionFactoryUtils.releaseSession(session, getSessionFactory());
			}
		}
	}

}
