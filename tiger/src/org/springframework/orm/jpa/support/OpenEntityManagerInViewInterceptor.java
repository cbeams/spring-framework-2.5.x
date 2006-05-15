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

package org.springframework.orm.jpa.support;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring web HandlerInterceptor that binds a JPA EntityManager to the
 * thread for the entire processing of the request. Intended for the "Open
 * EntityManager in View" pattern, i.e. to allow for lazy loading in
 * web views despite the original transactions already being completed.
 *
 * <p>This filter works similar to the AOP JpaInterceptor: It just makes JPA
 * EntityManagers available via the thread. It is suitable for
 * non-transactional execution but also for middle tier transactions via
 * JpaTransactionManager or JtaTransactionManager. In the latter case,
 * EntityManagers pre-bound by this filter will automatically be used
 * for the transactions.
 *
 * <p>In contrast to OpenEntityManagerInViewFilter, this interceptor is set
 * up in a Spring application context and can thus take advantage of bean wiring.
 * It derives from JpaAccessor to inherit common JPA configuration properties.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see OpenEntityManagerInViewFilter
 * @see org.springframework.orm.jpa.JpaInterceptor
 * @see org.springframework.orm.jpa.JpaTransactionManager
 * @see org.springframework.orm.jpa.JpaTemplate#execute
 * @see SharedEntityManagerFactory
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class OpenEntityManagerInViewInterceptor extends HandlerInterceptorAdapter {

	/**
	 * Suffix that gets appended to the EntityManagerFactory toString
	 * representation for the "participate in existing persistence manager
	 * handling" request attribute.
	 * @see #getParticipateAttributeName
	 */
	public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";


	protected final Log logger = LogFactory.getLog(getClass());

	private EntityManagerFactory entityManagerFactory;


	/**
	 * Set the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.entityManagerFactory = emf;
	}

	/**
	 * Return the JPA EntityManagerFactory that should be used to create
	 * EntityManagers.
	 */
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}


	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws DataAccessException {

		if (TransactionSynchronizationManager.hasResource(getEntityManagerFactory())) {
			// do not modify the EntityManager: just mark the request accordingly
			String participateAttributeName = getParticipateAttributeName();
			Integer count = (Integer) request.getAttribute(participateAttributeName);
			int newCount = (count != null) ? count.intValue() + 1 : 1;
			request.setAttribute(getParticipateAttributeName(), new Integer(newCount));
		}

		else {
			logger.debug("Opening JPA persistence manager in OpenEntityManagerInViewInterceptor");
			try {
				EntityManager em = getEntityManagerFactory().createEntityManager();
				TransactionSynchronizationManager.bindResource(getEntityManagerFactory(), new EntityManagerHolder(em));
			}
			catch (PersistenceException ex) {
				throw new DataAccessResourceFailureException("Could not open JPA EntityManager", ex);
			}
		}

		return true;
	}

	public void afterCompletion(
			HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws DataAccessException {

		String participateAttributeName = getParticipateAttributeName();
		Integer count = (Integer) request.getAttribute(participateAttributeName);
		if (count != null) {
			// do not modify the EntityManager: just clear the marker
			if (count.intValue() > 1) {
				request.setAttribute(participateAttributeName, new Integer(count.intValue() - 1));
			}
			else {
				request.removeAttribute(participateAttributeName);
			}
		}

		else {
			EntityManagerHolder emHolder = (EntityManagerHolder)
					TransactionSynchronizationManager.unbindResource(getEntityManagerFactory());
			logger.debug("Closing JPA persistence manager in OpenEntityManagerInViewInterceptor");
			emHolder.getEntityManager().close();
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request is
	 * already filtered. Default implementation takes the toString representation
	 * of the EntityManagerFactory instance and appends ".FILTERED".
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getEntityManagerFactory().toString() + PARTICIPATE_SUFFIX;
	}

}
