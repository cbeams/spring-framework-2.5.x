/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.enterpriseservices;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.enterpriseservices.mod.Modifiable;
import org.springframework.enterpriseservices.mod.ModifiableTestBean;

/**
 * Abstract tests for EnterpriseServices. Subclasses must
 * load the appropriate bean factory defining the necessary beans
 * and their transaction attributes.
 * See the enterpriseServics.xml file for definitions of beans;
 * define the EnterpriseServices bean in a separate file to
 * change how attributes are source. 
 * @author Rod Johnson
 * @version $Id: AbstractEnterpriseServicesTests.java,v 1.1 2003-11-22 09:05:41 johnsonr Exp $
 */
public abstract class AbstractEnterpriseServicesTests extends TestCase {
	
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	protected AbstractEnterpriseServicesTests(String arg0) {
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
		TestBean tb = (TestBean) bf.getBean("noProxy");
		// We can tell it's not a CGLIB proxy by looking at the class name
		assertTrue(tb.getClass().getName().equals(TestBean.class.getName()));
	}
	
	public void testTxIsProxied() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		// We can tell it's a CGLIB proxy by looking at the class name
		System.out.println(txClass.getClass().getName());
		assertFalse(txClass.getClass().getName().equals(TxClass.class.getName()));
	}
	
	public void testIntroductionIsProxied() throws Exception {
		BeanFactory bf = getBeanFactory();
		Object modifiable = bf.getBean("modifiable1");
		// We can tell it's a CGLIB proxy by looking at the class name
		System.out.println(modifiable.getClass().getName());
		assertFalse(modifiable.getClass().getName().equals(ModifiableTestBean.class.getName()));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void testDefaultTransactionAttributeOnMethod() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		System.out.println(txClass.getClass());
		
		TestTxManager txMan = (TestTxManager) bf.getBean("txManager");
		
		assertEquals(0, txMan.committed);
		int count = txClass.defaultTxAttribute();
		assertEquals("Return value was correct", 1, count);
		assertEquals("Transaction counts match", 1, txMan.committed);
	}
	
	
	/**
	 * Should not roll back on servlet exception
	 * @throws Exception
	 */
	public void testRollbackRulesOnMethodCauseRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		System.out.println(txClass.getClass());
	
		TestTxManager txMan = (TestTxManager) bf.getBean("txManager");
	
		assertEquals(0, txMan.committed);
		txClass.echoException(null);
		assertEquals("Transaction counts match", 1, txMan.committed);
		
		assertEquals(0, txMan.rolledback);
		Exception ex = new Exception();
		try {
			txClass.echoException(ex);
		}
		catch (Exception actual) {
			assertEquals(ex, actual);
		}
		assertEquals("Transaction counts match", 1, txMan.rolledback);
	}
	
	public void testRollbackRulesOnMethodPreventRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		System.out.println(txClass.getClass());

		TestTxManager txMan = (TestTxManager) bf.getBean("txManager");

		assertEquals(0, txMan.committed);
		// Should NOT roll back on ServletException 
		try {
			txClass.echoException(new ServletException());
		}
		catch (ServletException ex) {
			
		}
		assertEquals("Transaction counts match", 1, txMan.committed);
	}
	
	public void testProgrammaticRollback() throws Exception {
		BeanFactory bf = getBeanFactory();

		TestTxManager txMan = (TestTxManager) bf.getBean("txManager");
		
		TxClassWithClassAttribute txClass = (TxClassWithClassAttribute) bf.getBean("txClassWithClassAttribute");
		assertEquals(0, txMan.committed);
		txClass.rollbackOnly(false);
		assertEquals("Transaction counts match", 1, txMan.committed);
		assertEquals(0, txMan.rolledback);
		txClass.rollbackOnly(true);
		assertEquals(1, txMan.rolledback);
	}


	public void testAttributeInheritedFromClass() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClassWithClassAttribute txClass = (TxClassWithClassAttribute) bf.getBean("txClassWithClassAttribute");
	
		TestTxManager txMan = (TestTxManager) bf.getBean("txManager");
	
		assertEquals(0, txMan.committed);
		int ret = txClass.inheritClassTxAttribute(25);
		assertEquals("Return value was correct", 25, ret);
		assertEquals("Transaction counts match when inherited from class", 1, txMan.committed);

		// Check method attribute override class att
		 try {
			 txClass.echoException(new Exception());
		 }
		 catch (Exception ex) {
		
		 }
		 assertEquals("Transaction rollbacks correct", 1, txMan.rolledback);
	}

	
	/**
	 * Tests an introduction pointcut. This is a prototype, so that it can add
	 * a Modifiable mixin. Tests that the autoproxy infrastructure can create
	 * advised objects with independent interceptor instances.
	 * The Modifiable behaviour of each instance of TestBean should be distinct.
	 * @throws Exception
	 */
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
	
}
