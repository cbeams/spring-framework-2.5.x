package org.springframework.transaction.annotation;

import junit.framework.TestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * @author Rob Harrop
 */
public class AnnotationTransactionAttributeSourceIntegrationTests extends TestCase {

	private AnnotationTransactionAttributeSource source;

	private TransactionInterceptor ti;

	private CallCountingPlatformTransactionManager ptm;

	public void setUp() {
		this.source = new AnnotationTransactionAttributeSource();
		this.ptm = new CallCountingPlatformTransactionManager();

		this.ti = new TransactionInterceptor();
		this.ti.setTransactionAttributeSource(source);
		this.ti.setTransactionManager(ptm);
	}

	public void testClassLevelOnly() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestClassLevelOnly());
		proxyFactory.addAdvice(this.ti);

		TestClassLevelOnly proxy = (TestClassLevelOnly) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);
	}

	public void testWithSingleMethodOverride() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithSingleMethodOverride());
		proxyFactory.addAdvice(this.ti);

		TestWithSingleMethodOverride proxy = (TestWithSingleMethodOverride) proxyFactory.getProxy();

		proxy.doSomething();
		assertFalse(this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertTrue(this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(2);
	}

	public void testWithMultiMethodOverride() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithMultiMethodOverride());
		proxyFactory.addAdvice(this.ti);

		TestWithMultiMethodOverride proxy = (TestWithMultiMethodOverride) proxyFactory.getProxy();

		proxy.doSomething();
		assertTrue(this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertTrue(this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(2);
	}


	public void testWithRollback() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithRollback());
		proxyFactory.addAdvice(this.ti);

		TestWithRollback proxy = (TestWithRollback) proxyFactory.getProxy();

		try {
			proxy.doSomethingErroneous();
			fail("Should throw IllegalStateException");
		}
		catch (IllegalStateException ex) {
			assertGetTransactionAndRollbackCount(1);
		}

		try {
			proxy.doSomethingElseErroneous();
			fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			assertGetTransactionAndRollbackCount(2);
		}
	}

	public void testWithInterface() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithInterfaceImpl());
		proxyFactory.setInterfaces(new Class[]{TestWithInterface.class});
		proxyFactory.addAdvice(this.ti);

		TestWithInterface proxy = (TestWithInterface) proxyFactory.getProxy();

		proxy.doSomething();
		assertFalse("Transaction should not be readOnly", this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertTrue("Transaction should be readOnly", this.ptm.isLastTransactionReadOnly());
		assertGetTransactionAndCommitCount(2);
	}

	private void assertGetTransactionAndCommitCount(int expectedCount) {
		assertEquals(expectedCount, this.ptm.getGetTransactionCount());
		assertEquals(expectedCount, this.ptm.getCommitCount());
	}

	private void assertGetTransactionAndRollbackCount(int expectedCount) {
		assertEquals(expectedCount, this.ptm.getGetTransactionCount());
		assertEquals(expectedCount, this.ptm.getRollbackCount());
	}

	@Transactional
	public static class TestClassLevelOnly {

		public void doSomething() {

		}

		public void doSomethingElse() {

		}
	}

	@Transactional
	public static class TestWithSingleMethodOverride {

		public void doSomething() {

		}

		@Transactional(readOnly = true)
		public void doSomethingElse() {

		}
	}

	@Transactional
	public static class TestWithMultiMethodOverride {

		@Transactional(readOnly = true)
		public void doSomething() {

		}

		@Transactional(readOnly = true)
		public void doSomethingElse() {

		}
	}

	@Transactional(rollbackFor = IllegalStateException.class)
	public static class TestWithRollback {

		public void doSomethingErroneous() {
			throw new IllegalStateException();
		}

		@Transactional(rollbackFor = IllegalArgumentException.class)
		public void doSomethingElseErroneous() {
			throw new IllegalArgumentException();
		}
	}

	@Transactional
	public static interface TestWithInterface {

		public void doSomething();

		@Transactional(readOnly = true)
		public void doSomethingElse();
	}

	public static class TestWithInterfaceImpl implements TestWithInterface {

		public void doSomething() {

		}

		public void doSomethingElse() {
		}
	}
}
