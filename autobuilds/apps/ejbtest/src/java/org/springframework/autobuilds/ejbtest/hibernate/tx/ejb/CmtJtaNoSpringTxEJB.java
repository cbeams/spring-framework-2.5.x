/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.autobuilds.ejbtest.hibernate.tx.ejb;

import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.autobuilds.ejbtest.Constants;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.ejb.support.AbstractStatelessSessionBean;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

/**
 * <p>EJB used just to test proper Hibernate Session binding and unbinding in 
 * a CMT (Container Managed Transaction) environment with JTA, but no Spring
 * TransactionManager involved. The methods here are not an example of what your
 * own EJB methods should do; they are just meant to test that Spring internals
 * are working ok. However the config of this bean _is_ a general example of how
 * you would set up an EJB to work with CMT without Spring transaction environment</p>
 * 
 * <p>In a CMT without Spring transactions environment, as long as the Hibernate
 * Configuration is set up with a TransactionManagerLookup so Hibernate (and Spring)
 * can find the JTA TransactionManager, Spring is still able to bind the Hibernate
 * Session to the current transaction, and ensure that all Hibernate work in a
 * transaction happens within the same session (when using
 * HibernateTemplate/SessionFactoryUtils.</p>
 * 
 * <p>This is not an example of how you would ideally write an EJB in a Spring 
 * environment. Ideally the EJB just obtains a POJO service object (wrapped with
 * TransactionInterceptor and possibly HibernateInterceptor) from an
 * ApplicationContext, and delegates all method calls to it. The technique used
 * here though is useful when there is a need to keep actual business code in the
 * EJB, and have this code work with Hibernate/Spring Mappers/DAOs.
 * </p>
 * 
 * @author colin sampaleanu
 */
public class CmtJtaNoSpringTxEJB extends AbstractStatelessSessionBean
		implements
			SessionBean,
			CmtJtaNoSpringTx {

	// --- statics
	public static final String SESSION_FACTORY_ID = "hibSessionFactory";

	protected static final Log logger = LogFactory
			.getLog(CmtJtaNoSpringTxEJB.class);

	// --- attributes
	SessionFactory sessionFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey(Constants.SERVICE_LAYER_CONTEXT_ID);
	}

	/*
	 * arghhh! stupid method needed just to make XDoclet happy, otherwise it will
	 * create one in the subclass it generates, killing the one in the superclass
	 * @ejb.create-method
	 */
	public void ejbCreate() throws CreateException {
		super.ejbCreate();
	}

	// javadoc in superclass
	protected void onEjbCreate() throws CreateException {
		sessionFactory = (SessionFactory) getBeanFactory().getBean(
				SESSION_FACTORY_ID);
	}

	// javadoc in superclass
	public String echo(String input) {
		return "hello " + input;
	}

	// javadoc in superclass
	public void testSameSessionReceivedInTwoRequests() throws TestFailureException {

			
		Session sess1 = SessionFactoryUtils.getSession(sessionFactory, true);
		
		Session sess2 = SessionFactoryUtils.getSession(sessionFactory, true);
		
		if (sess1 != sess2)
			throw new TestFailureException(
					"Should have received the same Session in each execute call since we are in one JTA transaction, but they were different.");
		
		sess1.close();
	}

	public void throwExceptionSoSessionUnbindCanBeVerified()
			throws DataAccessException {

		HibernateTemplate h = new HibernateTemplate(sessionFactory, true);

		h.execute(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				return session;
			}
		});

		h.execute(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {

				throw new DataRetrievalFailureException(
						"This Exception is being thrown just to verify proper unbinding of Hibernate Session from JTA transaction");
			}
		});
	}

	

}