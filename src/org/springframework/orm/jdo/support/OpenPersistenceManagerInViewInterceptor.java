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

package org.springframework.orm.jdo.support;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.jdo.PersistenceManagerFactoryUtils;
import org.springframework.orm.jdo.PersistenceManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Spring web HandlerInterceptor that binds a JDO PersistenceManager to the
 * thread for the entire processing of the request. Intended for the "Open
 * PersistenceManager in View" pattern, i.e. to allow for lazy loading in
 * web views despite the original transactions already being completed.
 *
 * <p>This filter works similar to the AOP JdoInterceptor: It just makes JDO
 * PersistenceManagers available via the thread. It is suitable for
 * non-transactional execution but also for middle tier transactions via
 * JdoTransactionManager or JtaTransactionManager. In the latter case,
 * PersistenceManagers pre-bound by this filter will automatically be used
 * for the transactions.
 *
 * <p>In contrast to OpenPersistenceManagerInViewFilter, this interceptor is set
 * up in a Spring application context and can thus take advantage of bean wiring.
 * It derives from JdoAccessor to inherit common JDO configuration properties.
 *
 * @author Juergen Hoeller
 * @since 12.06.2004
 * @see OpenPersistenceManagerInViewFilter
 * @see org.springframework.orm.jdo.JdoInterceptor
 * @see org.springframework.orm.jdo.JdoTransactionManager
 * @see org.springframework.orm.jdo.PersistenceManagerFactoryUtils#getPersistenceManager
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class OpenPersistenceManagerInViewInterceptor extends HandlerInterceptorAdapter {

	/**
	 * Suffix that gets appended to the PersistenceManagerFactory toString
	 * representation for the "participate in existing persistence manager
	 * handling" request attribute.
	 * @see #getParticipateAttributeName
	 */
	public static final String PARTICIPATE_SUFFIX = ".PARTICIPATE";


	protected final Log logger = LogFactory.getLog(getClass());

	private PersistenceManagerFactory persistenceManagerFactory;


	/**
	 * Set the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}


	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
	    throws DataAccessException {

		if (TransactionSynchronizationManager.hasResource(getPersistenceManagerFactory())) {
			// do not modify the PersistenceManager: just mark the request accordingly
			String participateAttributeName = getParticipateAttributeName();
			Integer count = (Integer) request.getAttribute(participateAttributeName);
			int newCount = (count != null) ? count.intValue() + 1 : 1;
			request.setAttribute(getParticipateAttributeName(), new Integer(newCount));
		}

		else {
			logger.debug("Opening JDO persistence manager in OpenPersistenceManagerInViewInterceptor");
			PersistenceManager pm =
					PersistenceManagerFactoryUtils.getPersistenceManager(getPersistenceManagerFactory(), true);
			TransactionSynchronizationManager.bindResource(
					getPersistenceManagerFactory(), new PersistenceManagerHolder(pm));
		}

		return true;
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
	    Exception ex) throws DataAccessException {

		String participateAttributeName = getParticipateAttributeName();
		Integer count = (Integer) request.getAttribute(participateAttributeName);
		if (count != null) {
			// do not modify the PersistenceManager: just clear the marker
			if (count.intValue() > 1) {
				request.setAttribute(participateAttributeName, new Integer(count.intValue() - 1));
			}
			else {
				request.removeAttribute(participateAttributeName);
			}
		}

		else {
			PersistenceManagerHolder pmHolder = (PersistenceManagerHolder)
					TransactionSynchronizationManager.unbindResource(getPersistenceManagerFactory());
			logger.debug("Closing JDO persistence manager in OpenPersistenceManagerInViewInterceptor");
			PersistenceManagerFactoryUtils.closePersistenceManagerIfNecessary(
					pmHolder.getPersistenceManager(), getPersistenceManagerFactory());
		}
	}

	/**
	 * Return the name of the request attribute that identifies that a request is
	 * already filtered. Default implementation takes the toString representation
	 * of the PersistenceManagerFactory instance and appends ".FILTERED".
	 * @see #PARTICIPATE_SUFFIX
	 */
	protected String getParticipateAttributeName() {
		return getPersistenceManagerFactory().toString() + PARTICIPATE_SUFFIX;
	}

}
