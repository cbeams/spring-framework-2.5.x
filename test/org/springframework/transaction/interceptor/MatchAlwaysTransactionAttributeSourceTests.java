/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.transaction.interceptor;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.transaction.TransactionDefinition;

/**
 * Test for MatchAlwaysTransactionAttributeSource
 *
 * @author Colin Sampaleanu
 * @since 15.10.2003
 * @version $Id: MatchAlwaysTransactionAttributeSourceTests.java,v 1.2 2003-11-28 11:57:10 johnsonr Exp $
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.support.BeanNameAutoProxyCreator
 */
public class MatchAlwaysTransactionAttributeSourceTests extends TestCase {
  
	public void testGetTransactionAttribute() throws Exception {
		MatchAlwaysTransactionAttributeSource tas = new MatchAlwaysTransactionAttributeSource();
		TransactionAttribute ta = tas.getTransactionAttribute(Object.class.getMethod("hashCode", null), null);
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_REQUIRED == ta.getPropagationBehavior());

		tas.setTransactionAttribute("PROPAGATION_SUPPORTS");
		ta = tas.getTransactionAttribute(ServletException.class.getMethod("getMessage", null), ServletException.class);
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_SUPPORTS == ta.getPropagationBehavior());
	}
}
