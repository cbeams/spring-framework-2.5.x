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
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.CountingTxManager;

/**
 * Abstract tests for EnterpriseServices. Subclasses must
 * load the appropriate bean factory defining the necessary beans
 * and their transaction attributes.
 * See the enterpriseServics.xml file for definitions of beans;
 * define the EnterpriseServices bean in a separate file to
 * change how attributes are source. 
 * @author Rod Johnson
 * @version $Id: AbstractAdvisorAutoProxyCreatorTests.java,v 1.1 2003-12-12 16:50:43 johnsonr Exp $
 */
public abstract class AbstractAdvisorAutoProxyCreatorTests extends TestCase {
	
	private static final String TXMANAGER_BEAN_NAME = "auto_txManager";
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	protected AbstractAdvisorAutoProxyCreatorTests(String arg0) {
		super(arg0);
	}
	
	/**
	 * Return a bean factory with attributes and EnterpriseServices configured.
	 * @return
	 * @throws IOException
	 */
	protected abstract BeanFactory getBeanFactory() throws IOException;
	
	
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
		BeanFactory bf = new ClassPathXmlApplicationContext("/org/springframework/aop/framework/support/customTargetSource.xml");
		ITestBean test = (ITestBean) bf.getBean("test");
		assertTrue(AopUtils.isAopProxy(test));
		Advised advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof CommonsPoolTargetSource);
		assertEquals("Rod", test.getName());
		// Check that references survived pooling
		assertEquals("Kerry", test.getSpouse().getName());
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
