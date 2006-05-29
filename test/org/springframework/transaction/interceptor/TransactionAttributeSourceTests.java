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

package org.springframework.transaction.interceptor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.transaction.TransactionDefinition;

/**
 * Tests for various TransactionAttributeSource implementations.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @since 15.10.2003
 * @see org.springframework.transaction.interceptor.TransactionProxyFactoryBean
 */
public class TransactionAttributeSourceTests extends TestCase {
  
	public void testMatchAlwaysTransactionAttributeSource() throws Exception {
		MatchAlwaysTransactionAttributeSource tas = new MatchAlwaysTransactionAttributeSource();
		TransactionAttribute ta = tas.getTransactionAttribute(
				Object.class.getMethod("hashCode", (Class[]) null), null);
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_REQUIRED == ta.getPropagationBehavior());

		tas.setTransactionAttribute(new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_SUPPORTS));
		ta = tas.getTransactionAttribute(
				ServletException.class.getMethod("getMessage", (Class[]) null), ServletException.class);
		assertNotNull(ta);
		assertTrue(TransactionDefinition.PROPAGATION_SUPPORTS == ta.getPropagationBehavior());
	}

	public void testMethodMapTransactionAttributeSource() throws NoSuchMethodException {
		MethodMapTransactionAttributeSource tas = new MethodMapTransactionAttributeSource();
		Map methodMap = new HashMap();
		methodMap.put(Object.class.getName() + ".hashCode", "PROPAGATION_REQUIRED");
		methodMap.put(Object.class.getName() + ".toString",
				new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_SUPPORTS));
		tas.setMethodMap(methodMap);
		tas.afterPropertiesSet();
		TransactionAttribute ta = tas.getTransactionAttribute(
				Object.class.getMethod("hashCode", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, ta.getPropagationBehavior());
		ta = tas.getTransactionAttribute(Object.class.getMethod("toString", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_SUPPORTS, ta.getPropagationBehavior());
	}

	public void testMethodMapTransactionAttributeSourceWithLazyInit() throws NoSuchMethodException {
		MethodMapTransactionAttributeSource tas = new MethodMapTransactionAttributeSource();
		Map methodMap = new HashMap();
		methodMap.put(Object.class.getName() + ".hashCode", "PROPAGATION_REQUIRED");
		methodMap.put(Object.class.getName() + ".toString",
				new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_SUPPORTS));
		tas.setMethodMap(methodMap);
		TransactionAttribute ta = tas.getTransactionAttribute(
				Object.class.getMethod("hashCode", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, ta.getPropagationBehavior());
		ta = tas.getTransactionAttribute(Object.class.getMethod("toString", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_SUPPORTS, ta.getPropagationBehavior());
	}

	public void testNameMatchTransactionAttributeSource() throws NoSuchMethodException {
		NameMatchTransactionAttributeSource tas = new NameMatchTransactionAttributeSource();
		Map methodMap = new HashMap();
		methodMap.put("hashCode", "PROPAGATION_REQUIRED");
		methodMap.put("toString", new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_SUPPORTS));
		tas.setNameMap(methodMap);
		TransactionAttribute ta = tas.getTransactionAttribute(
				Object.class.getMethod("hashCode", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, ta.getPropagationBehavior());
		ta = tas.getTransactionAttribute(Object.class.getMethod("toString", (Class[]) null), null);
		assertNotNull(ta);
		assertEquals(TransactionDefinition.PROPAGATION_SUPPORTS, ta.getPropagationBehavior());
	}

}
