/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.orm.jpa;

import javax.persistence.EntityManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This interceptor binds a new JPA EntityManager to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already is a pre-bound EntityManager (e.g. from JpaTransactionManager,
 * or from a surrounding JPA-intercepted method), the interceptor simply participates in it.
 *
 * <p>This interceptor could convert unchecked PersistenceExceptions to unchecked dao ones
 * on-the-fly. The intercepted method wouldn't have to throw any special checked
 * exceptions to be able to achieve this. Nevertheless, such a mechanism would
 * effectively break the contract of the intercepted method (runtime exceptions
 * can be considered part of the contract too), therefore it isn't supported.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see JpaTransactionManager
 * @see JpaTemplate
 */
public class JpaInterceptor extends JpaAccessor implements MethodInterceptor {

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		// Determine current EntityManager: either the transactional one
		// managed by the factory or a temporary one for the given invocation.
		EntityManager em = EntityManagerFactoryUtils.getEntityManager(getEntityManagerFactory());
		boolean isNewEm = false;
		if (em == null) {
			logger.debug("Creating new EntityManager for JpaInterceptor invocation");
			em = getEntityManagerFactory().createEntityManager();
			isNewEm = true;
		}

		try {
			Object retVal = methodInvocation.proceed();
			flushIfNecessary(em, !isNewEm);
			return retVal;
		}
		finally {
			if (isNewEm) {
				TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
				logger.debug("Closing new EntityManager after JpaInterceptor invocation");
				em.close();
			}
		}
	}

}
