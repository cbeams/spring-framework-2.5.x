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

package org.springframework.aop.framework.autoproxy.metadata;

import java.io.IOException;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.target.AbstractPoolingTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.CountingTxManager;

/**
 * Abstract tests for EnterpriseServices. Subclasses must
 * load the appropriate bean factory defining the necessary beans
 * and their transaction attributes.
 * See the enterpriseServices.xml file for definitions of beans;
 * define the EnterpriseServices bean in a separate file to
 * change how attributes are source. 
 * @author Rod Johnson
 */
public abstract class AbstractMetadataAutoProxyTests extends TestCase {
	
	private static final String TXMANAGER_BEAN_NAME = "es.txManager";
	/**
	 * Constructor for ProxyFactoryBeanTests.
	 * @param arg0
	 */
	protected AbstractMetadataAutoProxyTests(String arg0) {
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
		assertTrue(AopUtils.isAopProxy(txClass));
	}
	
	public void testIntroductionIsProxied() throws Exception {
		BeanFactory bf = getBeanFactory();
		Object modifiable = bf.getBean("modifiable1");
		assertTrue(AopUtils.isAopProxy(modifiable));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	public void testDefaultTransactionAttributeOnMethod() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
		
		assertEquals(0, txMan.commits);
		int count = txClass.defaultTxAttribute();
		assertEquals("Return value was correct", 1, count);
		assertEquals("Transaction counts match", 1, txMan.commits);
	}
	
	
	/**
	 * Should not roll back on servlet exception
	 * @throws Exception
	 */
	public void testRollbackRulesOnMethodCauseRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");
		assertTrue(AopUtils.isAopProxy(txClass));
	
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
	
		assertEquals(0, txMan.commits);
		txClass.echoException(null);
		assertEquals("Transaction counts match", 1, txMan.commits);
		
		assertEquals(0, txMan.rollbacks);
		Exception ex = new Exception();
		try {
			txClass.echoException(ex);
		}
		catch (Exception actual) {
			assertEquals(ex, actual);
		}
		assertEquals("Transaction counts match", 1, txMan.rollbacks);
	}
	
	public void testRollbackRulesOnMethodPreventRollback() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClass txClass = (TxClass) bf.getBean("txClass");

		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);

		assertEquals(0, txMan.commits);
		// Should NOT roll back on ServletException 
		try {
			txClass.echoException(new ServletException());
		}
		catch (ServletException ex) {
			
		}
		assertEquals("Transaction counts match", 1, txMan.commits);
	}
	
	public void testProgrammaticRollback() throws Exception {
		BeanFactory bf = getBeanFactory();

		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
		
		TxClassWithClassAttribute txClass = (TxClassWithClassAttribute) bf.getBean("txClassWithClassAttribute");
		// No interface, so must be a CGLIB proxy
		assertTrue(AopUtils.isCglibProxy(txClass));
		assertEquals(0, txMan.commits);
		txClass.rollbackOnly(false);
		assertEquals("Transaction counts match", 1, txMan.commits);
		assertEquals(0, txMan.rollbacks);
		txClass.rollbackOnly(true);
		assertEquals(1, txMan.rollbacks);
	}


	public void testTransactionAttributeForMethodInheritedFromClass() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClassWithClassAttribute txClass = (TxClassWithClassAttribute) bf.getBean("txClassWithClassAttribute");
	
		CountingTxManager txMan = (CountingTxManager) bf.getBean(TXMANAGER_BEAN_NAME);
	
		assertEquals(0, txMan.commits);
		int ret = txClass.inheritClassTxAttribute(25);
		assertEquals("Return value was correct", 25, ret);
		assertEquals("Transaction counts match when inherited from class", 1, txMan.commits);

		// Check method attribute override class att
		 try {
			 txClass.echoException(new Exception());
		 }
		 catch (Exception ex) {
		
		 }
		 assertEquals("Transaction rollbacks correct", 1, txMan.rollbacks);
	}
	
	/**
	 * Plain old class with no special attributes
	 * @throws Exception
	 */
	public void testNoAutoProxying() throws Exception {
		BeanFactory bf = getBeanFactory();
		ITestBean test = (ITestBean) bf.getBean("rawTest");
		assertFalse(AopUtils.isAopProxy(test));
	}
	
	public void testAutoPrototype() throws Exception {
		BeanFactory bf = getBeanFactory();
		ITestBean test = (ITestBean) bf.getBean("protoTest");
		assertTrue(AopUtils.isAopProxy(test));
		Advised advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof PrototypeTargetSource );
		ITestBean test2 = (ITestBean) bf.getBean("protoTest");
		assertFalse(test == test2);
	}
	
	public void testAutoThreadLocal() throws Exception {
		final BeanFactory bf = getBeanFactory();
		final ITestBean test = (ITestBean) bf.getBean("threadLocalTest");
		assertTrue(AopUtils.isAopProxy(test));
		Advised advised = (Advised) test;
		assertTrue(advised.getTargetSource() instanceof ThreadLocalTargetSource );
		String nameForMainThread = "tom";
		test.setName(nameForMainThread);
		assertEquals(nameForMainThread, test.getName());
		// Check that in another thread we don't see that value back
		Runnable r = new Runnable() {
			public void run() {
				//System.err.println("RUN INNER CLASS");
				ITestBean myTest = (ITestBean) bf.getBean("threadLocalTest");
				assertNull(myTest.getName());
				String myName = "Fred";
				myTest.setName(myName);
				assertEquals(myName, myTest.getName());
				//assertEquals(myName, test.getName());
			}
		};
		Thread t = new Thread(r);
		t.start();
		t.join();
		// Inner class didn't change outer thread's value
		assertEquals(nameForMainThread, test.getName());
	}
	
	/**
	 * Test that the pooling TargetSourceCreator works.
	 */
	public void testAutoPooling() throws Exception {
		BeanFactory bf = getBeanFactory();
		TxClassWithClassAttribute txClass = (TxClassWithClassAttribute) bf.getBean("txClassWithClassAttribute");
		Advised advised = (Advised) txClass;
		assertTrue(advised.getTargetSource() instanceof AbstractPoolingTargetSource );
		AbstractPoolingTargetSource ts = (AbstractPoolingTargetSource) advised.getTargetSource();
		// Check pool size is that specified in the attribute
		assertEquals(10, ts.getMaxSize());
		// Check the object works
		txClass.rollbackOnly(false);
	}

	
	/**
	 * Tests an introduction pointcut. This is a prototype, so that it can add
	 * a Modifiable mixin. Tests that the autoproxy infrastructure can create
	 * advised objects with independent interceptor instances.
	 * The Modifiable behaviour of each instance of TestBean should be distinct.
	 */
	public void testIntroductionViaPrototype() throws Exception {
		BeanFactory bf = getBeanFactory();

		ITestBean modifiable1 = (ITestBean) bf.getBean("modifiable1");
		ITestBean modifiable2 = (ITestBean) bf.getBean("modifiable2");
		
		//Advised pc = (Advised) modifiable1;
		
		// For convenience only
		Modifiable mod1 = (Modifiable) modifiable1;  
		Modifiable mod2 = (Modifiable) modifiable2;  
		
		assertFalse(mod1.isModified());
		assertFalse(mod2.isModified());
		
		int newAge = 33;
		modifiable1.setAge(newAge);
		assertTrue(mod1.isModified());
		// Changes to one shouldn't have affected the other
		assertFalse("Instances of prototype introduction don't seem distinct", mod2.isModified());
		mod1.acceptChanges();
		assertFalse(mod1.isModified());
		assertEquals(modifiable1.getAge(), newAge);
		assertFalse(mod1.isModified());
	}
	
}
