/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.transaction.interceptor;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.aop.framework.support.StaticMethodMatcherPointcut;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Test cases for AOP transaction management.
 * @author Rod Johnson
 * @since 23-Apr-2003
 * @version $Id: BeanFactoryTransactionTests.java,v 1.9 2003-11-12 20:14:32 johnsonr Exp $
 */
public class BeanFactoryTransactionTests extends TestCase {

	private BeanFactory factory;

	public void setUp() {
		InputStream is = getClass().getResourceAsStream("transactionalBeanFactory.xml");
		this.factory = new XmlBeanFactory(is, null);
		ITestBean testBean = (ITestBean) factory.getBean("target");
		testBean.setAge(666);
	}

	public void testGetsAreNotTransactionalWithProxyFactory1() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory1");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory2() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory2");
		assertTrue("testBean is a dynamic proxy", Proxy.isProxyClass(testBean.getClass()));
		executeGetsAreNotTransactional(testBean);
	}

	public void testGetsAreNotTransactionalWithProxyFactory3() throws NoSuchMethodException {
		ITestBean testBean = (ITestBean) factory.getBean("proxyFactory3");
		assertTrue("testBean is a full proxy", testBean instanceof DerivedTestBean);
		InvocationCounterPointcut txnCounter = (InvocationCounterPointcut) factory.getBean("txnInvocationCounterPointcut");
		InvocationCounterInterceptor preCounter = (InvocationCounterInterceptor) factory.getBean("preInvocationCounterInterceptor");
		InvocationCounterInterceptor postCounter = (InvocationCounterInterceptor) factory.getBean("postInvocationCounterInterceptor");
		txnCounter.counter = 0;
		preCounter.counter = 0;
		postCounter.counter = 0;
		executeGetsAreNotTransactional(testBean);
		// Can't assert it's equal to 4 as the pointcut may be optimized and only invoked once
		assertTrue(0 < txnCounter.counter && txnCounter.counter <= 4);
		assertEquals(4, preCounter.counter);
		assertEquals(4, postCounter.counter);
	}

	public void executeGetsAreNotTransactional(ITestBean testBean) throws NoSuchMethodException {
		// Install facade
		MockControl ptmControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptmControl.getMock();
		// Expect no methods
		ptmControl.replay();
		PlatformTransactionManagerFacade.delegate = ptm;

		assertTrue("Age should not be " + testBean.getAge(), testBean.getAge() == 666);
		// Check no calls
		ptmControl.verify();

		// Install facade expecting a call
		ptmControl = MockControl.createControl(PlatformTransactionManager.class);
		ptm = (PlatformTransactionManager) ptmControl.getMock();
		TransactionStatus txStatus = new TransactionStatus(null, true);
		TransactionInterceptor txInterceptor = (TransactionInterceptor) factory.getBean("txInterceptor");
		MethodMapTransactionAttributeSource txAttSrc = (MethodMapTransactionAttributeSource) txInterceptor.getTransactionAttributeSource();
		ptm.getTransaction((TransactionDefinition) txAttSrc.methodMap.values().iterator().next());
		//ptm.getTransaction(null);
		ptmControl.setReturnValue(txStatus);
		ptm.commit(txStatus);
		ptmControl.setVoidCallable();
		ptmControl.replay();
		PlatformTransactionManagerFacade.delegate = ptm;

		// TODO same as old age to avoid ordering effect for now
		int age = 666;
		testBean.setAge(age);
		assertTrue(testBean.getAge() == age);
		ptmControl.verify();
	}


	public static class InvocationCounterPointcut extends StaticMethodMatcherPointcut {

		int counter = 0;

		public boolean matches(Method method, Class clazz) {
			counter++;
			return true;
		}
	}


	public static class InvocationCounterInterceptor implements MethodInterceptor {

		int counter = 0;

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			counter++;
			return methodInvocation.proceed();
		}
	}

}
