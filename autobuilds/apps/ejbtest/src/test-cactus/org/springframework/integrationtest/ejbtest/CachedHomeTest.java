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

package org.springframework.integrationtest.ejbtest;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.ServletTestSuite;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.integrationtest.ejbtest.simple.ejb.SimpleService;

/**
 * Test usage of EJB Proxy with and without a cached home. We actually just reuse the
 * EJB used for the test of Hibernate session binding...
 * 
 * @author colin sampaleanu
 * @version $Id: CachedHomeTest.java,v 1.2 2004-05-19 15:39:25 colins Exp $
 */
public class CachedHomeTest extends TestCase {

	// --- statics
	public static final String SERVICE_ID_CACHE_ON = "cachedHomeProxy";
	public static final String SERVICE_ID_CACHE_OFF = "noCachedHomeProxy";

	// --- attributes

	BeanFactoryReference bfr;
	
	// --- methods

	public static Test suite() {
		ServletTestSuite suite = new ServletTestSuite();
		suite.addTestSuite(CachedHomeTest.class);
		return suite;
	}

	protected void setUp() throws Exception {
		bfr = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(
				Constants.SERVICE_LAYER_CONTEXT_ID);
	}

	protected void tearDown() throws Exception {
		bfr.release();
	}

	public void testInvocationsWithCache() {
		SimpleService ejb = (SimpleService) bfr.getFactory().getBean(SERVICE_ID_CACHE_ON);
		ejb.echo("hello");
		ejb.echo("hello");
		ejb.echo("hello");
	}

	
	public void testInvocationsWithNoCache() {
		SimpleService ejb = (SimpleService) bfr.getFactory().getBean(SERVICE_ID_CACHE_OFF);
		ejb.echo("hello");
		ejb.echo("hello");
		ejb.echo("hello");
	}
}
