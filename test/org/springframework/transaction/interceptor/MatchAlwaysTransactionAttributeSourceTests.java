/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
package org.springframework.transaction.interceptor;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.transaction.TransactionDefinition;

/**
 * Test for MatchAlwaysTransactionAttributeSource
 *
 * @author Colin Sampaleanu
 * @since 15.10.2003
 * @version $Id: MatchAlwaysTransactionAttributeSourceTests.java,v 1.1 2003-10-16 19:37:13 johnsonr Exp $
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 * @see org.springframework.aop.framework.support.BeanNameAutoProxyCreator
 */
public class MatchAlwaysTransactionAttributeSourceTests extends TestCase {
  
	public void testGetTransactionAttribute() {
		MockControl control = MockControl.createControl(MethodInvocation.class);

		MethodInvocation mock = (MethodInvocation) control.getMock();

		// we know it doesn't need an actual method, so no more setup is needed
		control.replay();

		MatchAlwaysTransactionAttributeSource tas = new MatchAlwaysTransactionAttributeSource();
		TransactionAttribute ta = tas.getTransactionAttribute(mock);
		control.verify();
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_REQUIRED == ta.getPropagationBehavior());

		control.reset();
		tas.setTransactionAttribute("PROPAGATION_SUPPORTS");
		control.replay();
		ta = tas.getTransactionAttribute(mock);
		control.verify();
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_SUPPORTS == ta.getPropagationBehavior());
	}
}
