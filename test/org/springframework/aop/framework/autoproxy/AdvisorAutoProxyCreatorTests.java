/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.support.AopUtils;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.CountingTxManager;

/**
 * Tests for auto proxy creation by advisor recognition.
 * @author Rod Johnson
 * @version $Id: AdvisorAutoProxyCreatorTests.java,v 1.5 2003-12-14 16:11:03 johnsonr Exp $
 */
public class AdvisorAutoProxyCreatorTests extends TestCase {
	
	private static final String ADVISOR_APC_BEAN_NAME = "aapc";
	
	private static final String TXMANAGER_BEAN_NAME = ADVISOR_APC_BEAN_NAME + ".txManager";
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	public AdvisorAutoProxyCreatorTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Return a bean factory with attributes and EnterpriseServices configured.
	 * @return
	 * @throws IOException
	 */
	protected BeanFactory getBeanFactory() throws IOException {
		return new ClassPathXmlApplicationContext("/org/springframework/aop/framework/autoproxy/advisorAutoProxyCreator.xml");
	}
	
	public void testDefaultExclusionPrefix() throws Exception {
		AdvisorAutoProxyCreator aapc = (AdvisorAutoProxyCreator) getBeanFactory().getBean(ADVISOR_APC_BEAN_NAME);
		assertEquals(ADVISOR_APC_BEAN_NAME + AdvisorAutoProxyCreator.SEPARATOR, aapc.getInfrastructureBeanNamePrefix() );
	}
	
	/**
	 * If no pointcuts match (no atts) there should be proxying
	 * @throws Exception
	 */
	public void testNoProxy() throws Exception {
		BeanFactory bf = getBeanFactory();
		Object o = bf.getBean("noSetters");
		assertFalse(AopUtils.isAopProxy(o));
	}
	
	public void testTxIsProxied() throws Exception {
		BeanFactory bf = getBeanFactory();
		ITestBean test = (ITestBean) bf.getBean("test");
		assertTrue(AopUtils.isAopProxy(test));
	}
	
	public void testCustomTargetSource() throws Exception {
		BeanFactory bf = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/autoproxy/customTargetSource.xml");
		ITestBean test = (ITestBean) bf.getBean("test");
		assertTrue(AopUtils.isAopProxy(test));
		Advised advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof CommonsPoolTargetSource);
		assertEquals("Rod", test.getName());
		// Check that references survived pooling
		assertEquals("Kerry", test.getSpouse().getName());
	}
	
	public void testQuickTargetSourceCreator() throws Exception {
		ClassPathXmlApplicationContext bf = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/autoproxy/quickTargetSource.xml");
		ITestBean test = (ITestBean) bf.getBean("test");
		assertFalse(AopUtils.isAopProxy(test));
		assertEquals("Rod", test.getName());
		// Check that references survived pooling
		assertEquals("Kerry", test.getSpouse().getName());
	
		// Now test the pooled one
		test = (ITestBean) bf.getBean(":test");
		assertTrue(AopUtils.isAopProxy(test));
		Advised advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof CommonsPoolTargetSource);
		assertEquals("Rod", test.getName());
		// Check that references survived pooling
		assertEquals("Kerry", test.getSpouse().getName());
		
		// Now test the ThreadLocal one
		test = (ITestBean) bf.getBean("%test");
		assertTrue(AopUtils.isAopProxy(test));
		advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof ThreadLocalTargetSource);
		assertEquals("Rod", test.getName());
		// Check that references survived pooling
		assertEquals("Kerry", test.getSpouse().getName());
		
		// Now test the Prototype TargetSource
		 test = (ITestBean) bf.getBean("!test");
		 assertTrue(AopUtils.isAopProxy(test));
		 advised = (Advised) test;
		 assertTrue(advised.getTargetSource() instanceof PrototypeTargetSource);
		 assertEquals("Rod", test.getName());
		 // Check that references survived pooling
		 assertEquals("Kerry", test.getSpouse().getName());
		 
		 
		 ITestBean test2 = (ITestBean) bf.getBean("!test");
		 assertFalse("Prototypes cannot be the same object", test == test2);
		assertEquals("Rod", test2.getName());
		assertEquals("Kerry", test2.getSpouse().getName());
		bf.close();
	}
	
	/*
	public void testIntroductionIsProxied() throws Exception {
		BeanFactory bf = getBeanFactory();
		Object modifiable = bf.getBean("modifiable1");
		// We can tell it's a CGLIB proxy by looking at the class name
		System.out.println(modifiable.getClass().getName());
		assertFalse(modifiable.getClass().getName().equals(ModifiableTestBean.class.getName()));
	}
	*/

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void testTransactionAttributeOnMethod() throws Exception {
		BeanFactory bf = getBeanFactory();
		ITestBean test = (ITestBean) bf.getBean("test");
		
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
		
		assertEquals(0, txMan.commits);
		assertEquals("Initial value was correct", 4, test.getAge());
		int newAge = 5;
		test.setAge(newAge);
		assertEquals("New value set correctly", newAge, test.getAge());
		assertEquals("Transaction counts match", 1, txMan.commits);
	}
	
	
	/**
	 * Should not roll back on servlet exception
	 * @throws Exception
	 */
	public void testRollbackRulesOnMethodCauseRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		Rollback rb = (Rollback) bf.getBean("rollback");
	
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
	
		assertEquals(0, txMan.commits);
		rb.echoException(null);
		assertEquals("Transaction counts match", 1, txMan.commits);
		
		assertEquals(0, txMan.rollbacks);
		Exception ex = new Exception();
		try {
			rb.echoException(ex);
		}
		catch (Exception actual) {
			assertEquals(ex, actual);
		}
		assertEquals("Transaction counts match", 1, txMan.rollbacks);
	}
	
	public void testRollbackRulesOnMethodPreventRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		Rollback rb = (Rollback) bf.getBean("rollback");

		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);

		assertEquals(0, txMan.commits);
		// Should NOT roll back on ServletException 
		try {
			rb.echoException(new ServletException());
		}
		catch (ServletException ex) {
			
		}
		assertEquals("Transaction counts match", 1, txMan.commits);
	}
	
	public void testProgrammaticRollback() throws Exception {
		BeanFactory bf = getBeanFactory();

		assertTrue(bf.getBean(TXMANAGER_BEAN_NAME) instanceof CountingTxManager);
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
		
		Rollback rb = (Rollback) bf.getBean("rollback");
		assertEquals(0, txMan.commits);
		rb.rollbackOnly(false);
		assertEquals("Transaction counts match", 1, txMan.commits);
		assertEquals(0, txMan.rollbacks);
		// Will cause rollback only
		rb.rollbackOnly(true);
		assertEquals(1, txMan.rollbacks);
	}


	
	/**
	 * Tests an introduction pointcut. This is a prototype, so that it can add
	 * a Modifiable mixin. Tests that the autoproxy infrastructure can create
	 * advised objects with independent interceptor instances.
	 * The Modifiable behaviour of each instance of TestBean should be distinct.
	 * @throws Exception
	 */
	/*
	public void testIntroductionViaPrototype() throws Exception {
		BeanFactory bf = getBeanFactory();

		Object o = bf.getBean("modifiable1");
		ITestBean modifiable1 = (ITestBean) bf.getBean("modifiable1");
		ITestBean modifiable2 = (ITestBean) bf.getBean("modifiable2");
		
		Advised pc = (Advised) modifiable1;
		System.err.println(pc.toProxyConfigString());
		
		// For convenience only
		Modifiable mod1 = (Modifiable) modifiable1;  
		Modifiable mod2 = (Modifiable) modifiable2;  
		
		assertFalse(mod1.isModified());
		assertFalse(mod2.isModified());
		
		int newAge = 33;
		modifiable1.setAge(newAge);
		assertTrue(mod1.isModified());
		// Changes to one shouldn't have affected the other
		assertFalse("Instances of prototype introduction pointcut don't seem distinct", mod2.isModified());
		mod1.acceptChanges();
		assertFalse(mod1.isModified());
		assertEquals(modifiable1.getAge(), newAge);
		assertFalse(mod1.isModified());
	}
	*/
	
}
