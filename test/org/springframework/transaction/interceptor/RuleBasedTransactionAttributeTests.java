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

package org.springframework.transaction.interceptor;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJBException;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.transaction.TransactionDefinition;

/**
 * 
 * @author Rod Johnson
 * @since 09-Apr-2003
 */
public class RuleBasedTransactionAttributeTests extends TestCase {

	public void testDefaultRule() {
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute();
		assertTrue(rta.rollbackOn(new RuntimeException()));
		assertTrue(rta.rollbackOn(new EJBException()));
		assertTrue(!rta.rollbackOn(new Exception()));
		assertTrue(!rta.rollbackOn(new ServletException()));
	}
	
	/**
	 * Test one checked exception that should roll back
	 *
	 */
	public void testRuleForRollbackOnChecked() {
		List l = new LinkedList();
		l.add(new RollbackRuleAttribute("javax.servlet.ServletException"));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);
		
		assertTrue(rta.rollbackOn(new RuntimeException()));
		assertTrue(rta.rollbackOn(new EJBException()));
		assertTrue(!rta.rollbackOn(new Exception()));
		// Check that default behaviour is overridden
		assertTrue(rta.rollbackOn(new ServletException()));
	}
	
	public void testRuleForCommitOnUnchecked() {
		List l = new LinkedList();
		l.add(new NoRollbackRuleAttribute("javax.ejb.EJBException"));
		l.add(new RollbackRuleAttribute("javax.servlet.ServletException"));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);
		
		assertTrue(rta.rollbackOn(new RuntimeException()));
		// Check default behaviour is overridden
		assertTrue(!rta.rollbackOn(new EJBException()));
		assertTrue(!rta.rollbackOn(new Exception()));
		// Check that default behaviour is overridden
		assertTrue(rta.rollbackOn(new ServletException()));
	}
	
	public void testRuleForSelectiveRollbackOnCheckedWithString() {
		List l = new LinkedList();
		l.add(new RollbackRuleAttribute("java.rmi.RemoteException"));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);
		testRuleForSelectiveRollbackOnChecked(rta);
	}
	
	public void testRuleForSelectiveRollbackOnCheckedWithClass() {
		List l = Collections.singletonList(new RollbackRuleAttribute(RemoteException.class));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);
		testRuleForSelectiveRollbackOnChecked(rta);
	}
	
	private void testRuleForSelectiveRollbackOnChecked(RuleBasedTransactionAttribute rta) {
		assertTrue(rta.rollbackOn(new RuntimeException()));
		// Check default behaviour is overridden
		assertTrue(!rta.rollbackOn(new Exception()));
		// Check that default behaviour is overridden
		assertTrue(rta.rollbackOn(new RemoteException()));
	}
	
	/**
	 * Check that a rule can cause commit on a ServletException
	 * when Exception prompts a rollback.
	 */
	public void testRuleForCommitOnSubclassOfChecked() {
		List l = new LinkedList();
		// Note that it's important to ensure that we have this as
		// a FQN: otherwise it will match everything!
		l.add(new RollbackRuleAttribute("java.lang.Exception"));
		l.add(new NoRollbackRuleAttribute("ServletException"));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);

		assertTrue(rta.rollbackOn(new RuntimeException()));
		assertTrue(rta.rollbackOn(new Exception()));
		// Check that default behaviour is overridden
		assertFalse(rta.rollbackOn(new ServletException()));
	}
	
	public void testRollbackNever() {
		List l = new LinkedList();
		l.add(new NoRollbackRuleAttribute("Throwable"));
		RuleBasedTransactionAttribute rta = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, l);
	
		assertTrue(!rta.rollbackOn(new Throwable()));
		assertTrue(!rta.rollbackOn(new RuntimeException()));
		assertTrue(!rta.rollbackOn(new EJBException()));
		assertTrue(!rta.rollbackOn(new Exception()));
		assertTrue(!rta.rollbackOn(new ServletException()));
	}

}
