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
package org.springframework.integrationtest.ejbtest.hibernate.tx.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

/**
 * @author colin sampaleanu
 * @version $Id: CmtJtaNoSpringTxEJB.java,v 1.1 2004-04-16 23:13:37 colins Exp $
 */
public class CmtJtaNoSpringTxEJB extends AbstractStatelessSessionBean implements
		SessionBean, CmtJtaNoSpringTxService {

	protected static final Log logger = LogFactory.getLog(CmtJtaNoSpringTxEJB.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext) {
		super.setSessionContext(sessionContext);
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("primary-context");
	}

	/*
	 * arghhh! stupid method needed just to make XDoclet happy, otherwise it
	 * will create one in the subclass it generates, killing the one in the superclass
	 * @ejb.create-method
	 */
	public void ejbCreate() throws CreateException {
		super.ejbCreate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ejb.support.AbstractStatelessSessionBean#onEjbCreate()
	 */
	protected void onEjbCreate() throws CreateException {
		//    _appController = (ApplicationControllerService) getBeanFactory().getBean(
		//        CORE_SERVICES_APPLICATION_CONTROLLER_SERVICE);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integrationtest.ejbtest.hibernate.tx.CmtJtaNoSpringTxService#testMethod(java.lang.String)
	 */
	public String testMethod(String input) {
		return "hello " + input;
	}

}