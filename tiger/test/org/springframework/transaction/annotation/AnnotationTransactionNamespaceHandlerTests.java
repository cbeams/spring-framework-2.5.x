package org.springframework.transaction.annotation;

import junit.framework.TestCase;

import java.util.Collection;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.ITestBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.CallCountingTransactionManager;

/**
 * @author robh
 */
public class AnnotationTransactionNamespaceHandlerTests extends TestCase {

	private ApplicationContext context;

	public void setUp() {
		this.context = new ClassPathXmlApplicationContext("org/springframework/transaction/annotation/annotationTransactionNamespaceHandlerTests.xml");
	}

	public void testIsProxy() throws Exception {
		TransactionalTestBean bean = getTestBean();
		assertTrue("testBean is not a proxy", AopUtils.isAopProxy(bean));
	}

	public void testInvokeTransactional() throws Exception {
		TransactionalTestBean testBean = getTestBean();
		CallCountingTransactionManager ptm = (CallCountingTransactionManager) context.getBean("transactionManager");

		// try with transactional
		assertEquals("Should not have any started transactions", 0, ptm.begun);
		testBean.findAllFoos();
		assertEquals("Should have 1 started transaction", 1, ptm.begun);
		assertEquals("Should have 1 committed transaction", 1, ptm.commits);

		// try with non-transaction
		testBean.doSomething();
		assertEquals("Should not have started another transaction", 1, ptm.begun);

		// try with exceptional
		try {
			testBean.exceptional(new IllegalArgumentException("foo"));
			fail("Should NEVER get here");
		}
		catch (Throwable throwable) {
			assertEquals("Should have another started transaction", 2, ptm.begun);
			assertEquals("Should have 1 rolled back transaction", 1, ptm.rollbacks);

		}
	}
	
	private TransactionalTestBean getTestBean() {
		return (TransactionalTestBean)context.getBean("testBean");
	}

	@Transactional
	public static class TransactionalTestBean {

		@Transactional(readOnly = true)
		public Collection findAllFoos() {
			return null;
		}

		@Transactional
		public void saveFoo() {

		}

		@Transactional
		public void exceptional(Throwable t) throws Throwable {
			throw t;
		}

		public void doSomething() {

		}
	}
}
