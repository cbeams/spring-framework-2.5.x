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

import java.lang.reflect.Method;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.metadata.support.MapAttributes;


/**
 * 
 * @author Rod Johnson
 */
public class AttributesTransactionAttributeSourceTests extends TestCase {

	/**
	 * Constructor for AttributeRegistryTransactionAttributeSourceTests.
	 * @param arg0
	 */
	public AttributesTransactionAttributeSourceTests(String arg0) {
		super(arg0);
	}
	
	protected void setUp() {
		//Logger.getLogger(RuleBasedTransactionAttribute.class.getName()).setLevel(Level.DEBUG);
	}
	
	public void testNullOrEmpty() throws Exception {
		Method method = ITestBean.class.getMethod("getAge", null);
		
		MapAttributes mar = new MapAttributes();
		mar.register(method, null);
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(mar);
		assertNull(atas.getTransactionAttribute(method, null));
		
		mar.register(method, new Object[0]);
		assertNull(atas.getTransactionAttribute(method, null));
		// Try again in case of caching
		assertNull(atas.getTransactionAttribute(method, null));
	}
	
	public void testSingleTransactionAttribute() throws Exception {
		Method method = ITestBean.class.getMethod("getAge", null);
	
		TransactionAttribute txAtt = new DefaultTransactionAttribute();
		
		MapAttributes ma = new MapAttributes();
		ma.register(method, new Object[] { txAtt });
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		TransactionAttribute actual = atas.getTransactionAttribute(method, method.getDeclaringClass());
		assertEquals(txAtt, actual);
		// Check that the same attribute comes back if we ask twice
		assertSame(txAtt, atas.getTransactionAttribute(method, method.getDeclaringClass()));
	}
	
	
	public void testTransactionAttributeAmongOthers() throws Exception {
		Method method = TestBean.class.getMethod("getAge", null);

		TransactionAttribute txAtt = new DefaultTransactionAttribute();
	
		MapAttributes ma = new MapAttributes();
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		ma.register(method, new Object[] { new Object(), "", txAtt, "er" });
		TransactionAttribute actual = atas.getTransactionAttribute(method, method.getDeclaringClass());
		assertEquals(txAtt, actual);
		assertSame(txAtt, atas.getTransactionAttribute(method, method.getDeclaringClass()));
	}
	
	/**
	 * Test the important case where the invocation is on a proxied interface method, but
	 * the attribute is defined on the target class
	 * @throws Exception
	 */
	public void testTransactionAttributeDeclaredOnClassMethod() throws Exception {
		Method classMethod = TestBean.class.getMethod("getAge", null);
		Method interfaceMethod = ITestBean.class.getMethod("getAge", null);

		TransactionAttribute txAtt = new DefaultTransactionAttribute();

		MapAttributes ma = new MapAttributes();
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		ma.register(classMethod, new Object[] { new Object(), "", txAtt, "er" });
		// Target class implements ITestBean
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean.class);
		assertEquals(txAtt, actual);
	}
	
	public void testTransactionAttributeDeclaredOnInterfaceMethodOnly() throws Exception {
		Method interfaceMethod = ITestBean.class.getMethod("getAge", null);

		TransactionAttribute txAtt = new DefaultTransactionAttribute();

		MapAttributes ma = new MapAttributes();
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		ma.register(interfaceMethod, new Object[] { new Object(), "", txAtt, "er" });
		// Target class implements ITestBean
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean.class);
		assertEquals(txAtt, actual);
	}
	
	public void testTransactionAttributeDeclaredOnTargetClassMethodTakesPrecedenceOverAttributeDeclaredOnInterfaceMethod() throws Exception {
		Method classMethod = TestBean.class.getMethod("getAge", null);
		Method interfaceMethod = ITestBean.class.getMethod("getAge", null);

		TransactionAttribute interfaceAtt = new DefaultTransactionAttribute();
		TransactionAttribute classAtt = new DefaultTransactionAttribute();

		MapAttributes ma = new MapAttributes();
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		ma.register(interfaceMethod, new Object[] { new Object(), "", interfaceAtt, "er" });
		ma.register(classMethod, new Object[] { new Object(), "", classAtt, "er" });
		// Target class implements ITestBean
		TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean.class);
		assertEquals(classAtt, actual);
	}
	
	public void testRollbackRulesAreApplied() throws Exception {
		Method method = TestBean.class.getMethod("getAge", null);

		MapAttributes ma = new MapAttributes();
		TransactionAttribute txAtt = new RuleBasedTransactionAttribute();
		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
		RollbackRuleAttribute nrr = new NoRollbackRuleAttribute("ServletException");

		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		
		ma.register(method, new Object[] { new Object(), "", txAtt, rr, nrr, "er" });
		TransactionAttribute actual = atas.getTransactionAttribute(method, method.getDeclaringClass());
		assertEquals(txAtt, actual);
		assertTrue(txAtt.rollbackOn(new Exception()));
		assertFalse(txAtt.rollbackOn(new ServletException()));
		
		assertSame(txAtt, atas.getTransactionAttribute(method, method.getDeclaringClass()));
	}
	
	/**
	 * Test that transaction attribute is inherited from class
	 * if not specified on method
	 * @throws Exception
	 */
	public void testDefaultsToClassTransactionAttribute() throws Exception {
		Method method = TestBean.class.getMethod("getAge", null);

		TransactionAttribute txAtt = new DefaultTransactionAttribute();
		MapAttributes ma = new MapAttributes();
		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
		ma.register(TestBean.class, new Object[] { new Object(), "", txAtt, "er" });
		TransactionAttribute actual = atas.getTransactionAttribute(method, null);
		assertEquals(txAtt, actual);
	}

}
