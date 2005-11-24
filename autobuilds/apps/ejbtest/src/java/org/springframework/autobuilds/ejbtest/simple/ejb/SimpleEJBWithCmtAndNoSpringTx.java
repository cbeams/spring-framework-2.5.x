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

package org.springframework.autobuilds.ejbtest.simple.ejb;

import javax.ejb.CreateException;
import javax.ejb.SessionContext;

import org.hibernate.SessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.autobuilds.ejbtest.Constants;
import org.springframework.autobuilds.ejbtest.simple.SimpleService;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

/**
 * <p>Simple test EJB. This is used to demonstrat an EJB working without CMT
 * (Container Managed Transactions) for the EJB, but  without Spring Transactions,
 * in a JTA environment. Each EJB method delegates to a POJO service object which
 * has been transactionally wrapped. In this kind of a setup, you muse ensure that
 * one EJB method does not combine multiple calls into the POJO service object,
 * since each of those calls would end up being a separate (unrelated) transaction.
 * So the EJB can be considered just a facade.</p>
 *
 * <p>Note for XDoclet users. XDoclet is not smart enough to see that this class's
 * superclass implements SessionBean. If you are using XDoclet (not the case here,
 * this class would also have to directly implement SessionBean or XDoclet will
 * complain! Additionally, XDoclet is not smart enough to see that the superclass
 * implements ejbCreate, so it will create its own empty ejbCreate method in the
 * final EJB class it generates. This will override the one from the superclass,
 * which means the superclass one will never get called, and the bean factory will
 * never get loaded. Bad XDoclet! The solution is to yourself add an ejbCreate
 * method (as per the example below) which just delegates to the superclass one,
 * and then XDoclet will not generate its own.</p>
 * 
 * @author colin sampaleanu
 */
public class SimpleEJBWithCmtAndNoSpringTx extends AbstractStatelessSessionBean
		implements SimpleService {

	// --- statics
	public static final String POJO_SERVICE_ID = "delegatingSimpleService2";

	protected static final Log logger = LogFactory
			.getLog(SimpleEJBWithCmtAndNoSpringTx.class);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.ejb.support.AbstractStatelessSessionBean#onEjbCreate()
	 */
	protected void onEjbCreate() throws CreateException {
		// if we were delegating to a pojo service object on every ejb method, then we would
		// probably want to get the pojo service object here, as a class field. For the purpose
		// of the test we will only get it when needed
		//simpleService = (SimpleService) getBeanFactory().getBean(POJO_SERVICE_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.autobuilds.ejbtest.hibernate.tx.CmtJtaNoSpringTx#testMethod(java.lang.String)
	 */
	public String echo(String input) {
		return "(SimpleEJBWithCmtAndNoSpringTx: hello " + input + ")";
	}

	/* 
	 * An example of how an EJB method can delegate to a POJO object to provide
	 * the service. If every method were delegating, you would probably want to
	 * get the POJO instance in the onEjbCreateMethod
	 */
	public String echo2(String input) {
		SimpleService simpleService = (SimpleService) getBeanFactory().getBean(POJO_SERVICE_ID);
		return simpleService.echo2(input);
	}
	
	public String echo3(String input) {
		SimpleService simpleService = (SimpleService) getBeanFactory().getBean(POJO_SERVICE_ID);
		return simpleService.echo3(input);
	}
}
