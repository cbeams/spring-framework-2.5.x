/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.transaction.interceptor;

import java.lang.reflect.Method;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.MockControl;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.enterpriseservices.MapAttributes;

/**
 * 
 * @author Rod Johnson
 * @version $Id: AttributesTransactionAttributeSourceTests.java,v 1.2 2003-12-05 17:54:24 johnsonr Exp $
 */
public class AttributesTransactionAttributeSourceTests extends TestCase {

//	/**
//	 * Constructor for AttributeRegistryTransactionAttributeSourceTests.
//	 * @param arg0
//	 */
//	public AttributesTransactionAttributeSourceTests(String arg0) {
//		super(arg0);
//	}
//	
//	protected void setUp() {
//		Logger.getLogger(RuleBasedTransactionAttribute.class.getName()).setLevel(Level.DEBUG);
//	}
//	
//	public void testNullOrEmpty() throws Exception {
//		Method method = ITestBean.class.getMethod("getAge", null);
//		
//		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource();
//		MockControl mc = MockControl.createControl(MethodInvocation.class);
//		MethodInvocation mi = (MethodInvocation) mc.getMock();
//		mi.getMethod();
//		mc.setReturnValue(method, 2);
//		MapAttributes mar = new MapAttributes();
//		mar.register(method, null);
//		atas.setAttributes(mar);
//		mc.replay();
//		assertNull(atas.getTransactionAttribute(mi));
//		
//		mar.register(method, new Object[0]);
//		assertNull(atas.getTransactionAttribute(mi));
//		
//		mc.verify();
//	}
//	
//	public void testSingleTransactionAttribute() throws Exception {
//		Method method = ITestBean.class.getMethod("getAge", null);
//	
//		TransactionAttribute txAtt = new DefaultTransactionAttribute();
//		
//		MockControl mc = MockControl.createControl(MethodInvocation.class);
//		MethodInvocation mi = (MethodInvocation) mc.getMock();
//		mi.getMethod();
//		mc.setReturnValue(method, 1);
//		MapAttributes ma = new MapAttributes();
//		ma.register(method, new Object[] { txAtt });
//		mc.replay();
//		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
//		TransactionAttribute actual = atas.getTransactionAttribute(mi);
//		assertEquals(txAtt, actual);
//		mc.verify();
//	}
//	
//	
//	public void testTransactionAttributeAmongOthers() throws Exception {
//		Method method = TestBean.class.getMethod("getAge", null);
//
//		TransactionAttribute txAtt = new DefaultTransactionAttribute();
//	
//		MapAttributes ma = new MapAttributes();
//		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
//		MockControl mc = MockControl.createControl(MethodInvocation.class);
//		MethodInvocation mi = (MethodInvocation) mc.getMock();
//		mi.getMethod();
//		mc.setReturnValue(method, 1);
//		ma.register(method, new Object[] { new Object(), "", txAtt, "er" });
//		mc.replay();
//		TransactionAttribute actual = atas.getTransactionAttribute(mi);
//		assertEquals(txAtt, actual);
//		mc.verify();
//	}
//	
//	public void testRollbackRulesAreApplied() throws Exception {
//		Method method = TestBean.class.getMethod("getAge", null);
//
//		MapAttributes ma = new MapAttributes();
//		TransactionAttribute txAtt = new RuleBasedTransactionAttribute();
//		RollbackRuleAttribute rr = new RollbackRuleAttribute("java.lang.Exception");
//		RollbackRuleAttribute nrr = new NoRollbackRuleAttribute("ServletException");
//
//		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
//		MockControl mc = MockControl.createControl(MethodInvocation.class);
//		MethodInvocation mi = (MethodInvocation) mc.getMock();
//		mi.getMethod();
//		mc.setReturnValue(method, 1);
//		ma.register(method, new Object[] { new Object(), "", txAtt, rr, nrr, "er" });
//		mc.replay();
//		TransactionAttribute actual = atas.getTransactionAttribute(mi);
//		assertEquals(txAtt, actual);
//		assertTrue(txAtt.rollbackOn(new Exception()));
//		assertFalse(txAtt.rollbackOn(new ServletException()));
//		mc.verify();
//	}
//	
//	/**
//	 * Test that transaction attribute is inherited from class
//	 * if not specified on method
//	 * @throws Exception
//	 */
//	public void testDefaultsToClassTransactionAttribute() throws Exception {
//		Method method = TestBean.class.getMethod("getAge", null);
//
//		TransactionAttribute txAtt = new DefaultTransactionAttribute();
//		MapAttributes ma = new MapAttributes();
//		AttributesTransactionAttributeSource atas = new AttributesTransactionAttributeSource(ma);
//		MockControl mc = MockControl.createControl(MethodInvocation.class);
//		MethodInvocation mi = (MethodInvocation) mc.getMock();
//		mi.getMethod();
//		mc.setReturnValue(method, 1);
//		ma.register(TestBean.class, new Object[] { new Object(), "", txAtt, "er" });
//		mc.replay();
//		TransactionAttribute actual = atas.getTransactionAttribute(mi);
//		assertEquals(txAtt, actual);
//		mc.verify();
//	}

}
