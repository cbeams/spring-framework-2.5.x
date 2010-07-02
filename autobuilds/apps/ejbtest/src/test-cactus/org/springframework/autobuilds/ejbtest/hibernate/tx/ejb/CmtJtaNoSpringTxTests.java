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

package org.springframework.autobuilds.ejbtest.hibernate.tx.ejb;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.ServletTestSuite;
import org.springframework.autobuilds.ejbtest.Constants;
import org.springframework.autobuilds.ejbtest.hibernate.tx.ejb.CmtJtaNoSpringTx;
import org.springframework.autobuilds.ejbtest.hibernate.tx.ejb.TestFailureException;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Cactus test for CmtJtaNoSpringTx
 * 
 * @author colin sampaleanu
 */
public class CmtJtaNoSpringTxTests extends TestCase {

	// --- statics
	public static final String SERVICE_ID = "cmtJtaNoSpringTx";

	// --- attributes

	BeanFactoryReference bfr;
	CmtJtaNoSpringTx ejb;

	// --- methods

	public static Test suite() {
		ServletTestSuite suite = new ServletTestSuite();
		suite.addTestSuite(CmtJtaNoSpringTxTests.class);
		return suite;
	}

	protected void setUp() throws Exception {
		bfr = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(
				Constants.SERVICE_LAYER_CONTEXT_ID);

		ejb = (CmtJtaNoSpringTx) bfr.getFactory().getBean(SERVICE_ID);
	}

	protected void tearDown() throws Exception {
		bfr.release();
	}

	public void testMethodInvocation() {
		ejb.echo("hello");
	}

	public void testSameSessionReceivedInTwoHibernateCallbacks()
			throws TestFailureException {
		ejb.testSameSessionReceivedInTwoRequests();
	}

	public void testThrowExceptionSoSessionUnbindCanBeVerified() {

		try {
			ejb.throwExceptionSoSessionUnbindCanBeVerified();
		} catch (Exception e) {
			// DataAccessException expected here, inside the container's Remote exception for
			// remote EJB
		}
	}

}