package org.springframework.transaction;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.beans.ITestBean;
import org.springframework.aop.support.AopUtils;

/**
 * @author robh
 */
public class TransactionNamespaceHandlerTests extends TestCase {

	private ApplicationContext context;

	public void setUp() {
		this.context = new ClassPathXmlApplicationContext("org/springframework/transaction/transactionNamespaceHandlerTests.xml");
	}

	public void testIsProxy() throws Exception {
		ITestBean bean = getTestBean();
		assertTrue("testBean is not a proxy", AopUtils.isAopProxy(bean));
	}

	public void testInvokeTransactional() throws Exception {
		ITestBean testBean = getTestBean();
		CallCountingTransactionManager ptm = (CallCountingTransactionManager) context.getBean("transactionManager");

		// try with transactional
		assertEquals("Should not have any started transactions", 0, ptm.begun);
		testBean.getName();
		assertEquals("Should have 1 started transaction", 1, ptm.begun);
		assertEquals("Should have 1 committed transaction", 1, ptm.commits);

		// try with non-transaction
		testBean.haveBirthday();
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

	private ITestBean getTestBean() {
		return (ITestBean)context.getBean("testBean");
	}
}
