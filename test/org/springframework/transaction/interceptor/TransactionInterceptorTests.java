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

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * Mock object based tests for TransactionInterceptor.
 * True unit test in that it tests how the TransactionInterceptor uses
 * the PlatformTransactionManager helper, rather than indirectly
 * testing the helper implementation.
 * @author Rod Johnson
 * @since 16-Mar-2003
 * @version $Revision: 1.12 $
 */
public class TransactionInterceptorTests extends TestCase {

	public void testNoTransaction() throws Exception {
		// Could do this
		/*
		Method m = Object.class.getMethod("hashCode", null);
		Object proxy = new Object();
		MethodInvocationImpl mi = new MethodInvocationImpl(proxy, null, m.getDeclaringClass(), //?
		m, null,
		interceptors, // could customize here
		r);
		*/

		//TransactionAttribute txatt =

		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		// expect no calls
		ptxControl.replay();

		TestBean tb = new TestBean();
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		assertEquals(ptm, ti.getTransactionManager());
		TransactionAttributeSource tas = new MapTransactionAttributeSource();
		ti.setTransactionAttributeSource(tas);
		assertEquals(tas, ti.getTransactionAttributeSource());

		ProxyFactory pf = new ProxyFactory(tb);
		pf.addInterceptor(0, ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		// verification!?
		itb.getName();

		ptxControl.verify();
	}

	/**
	 * Check that a transaction is created and committed
	 * @throws java.lang.Exception
	 */
	public void testTransactionShouldSucceed() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = ITestBean.class.getMethod("getName", null);
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = new DefaultTransactionStatus(null, false, false, false, false, null);
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Expect a transaction
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();

		TestBean tb = new TestBean();
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		ti.setTransactionAttributeSource(tas);

		ProxyFactory pf = new ProxyFactory(tb);
		pf.addInterceptor(0, ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		// verification!?
		itb.getName();

		ptxControl.verify();
	}


	/**
	 * Test that TransactionControl.setRollbackOnly works
	 * @throws java.lang.Exception
	 */
	public void testProgrammaticRollback() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = ITestBean.class.getMethod("getName", null);
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = new DefaultTransactionStatus(null, false, false, false, false, null);
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);
		ptm.commit(status);
		ptxControl.setVoidCallable(1);
		ptxControl.replay();

		final String name = "jenny";
		TestBean tb = new TestBean() {
			public String getName() {
				TransactionStatus txStatus = TransactionInterceptor.currentTransactionStatus();
				txStatus.setRollbackOnly();
				return name;
			}
		};
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		ti.setTransactionAttributeSource(tas);

		ProxyFactory pf = new ProxyFactory(tb);
		// Need to use programmatic rollback
		//pf.setExposeInvocation(true);
		//pf.addInterceptor(new DebugInterceptor());
		pf.addInterceptor(ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		// verification!?
		assertTrue(name.equals(itb.getName()));

		ptxControl.verify();
	}

	public void testRollbackOnCheckedException() throws Throwable {
		testRollbackOnException(new Exception(), true, false);
	}

	public void testNoRollbackOnCheckedException() throws Throwable {
		testRollbackOnException(new Exception(), false, false);
	}

	public void testRollbackOnUncheckedException() throws Throwable {
		testRollbackOnException(new RuntimeException(), true, false);
	}

	public void testNoRollbackOnUncheckedException() throws Throwable {
		testRollbackOnException(new RuntimeException(), false, false);
	}

	public void testRollbackOnCheckedExceptionWithRollbackException() throws Throwable {
		testRollbackOnException(new Exception(), true, true);
	}

	public void testNoRollbackOnCheckedExceptionWithRollbackException() throws Throwable {
		testRollbackOnException(new Exception(), false, true);
	}

	public void testRollbackOnUncheckedExceptionWithRollbackException() throws Throwable {
		testRollbackOnException(new RuntimeException(), true, true);
	}

	public void testNoRollbackOnUncheckedExceptionWithRollbackException() throws Throwable {
		testRollbackOnException(new RuntimeException(), false, true);
	}

	/**
	 * Check that the given exception thrown by the target can
	 * produce the desired behaviour with the appropriate transaction
	 * attribute
	 * @param ex exception to be thrown by the target
	 * @param shouldRollback whether this should cause a transaction rollback
	 * @throws java.lang.Throwable
	 */
	private void testRollbackOnException(final Exception ex, final boolean shouldRollback, boolean rollbackException)
	    throws Throwable {

		TransactionAttribute txatt = new DefaultTransactionAttribute() {
			public boolean rollbackOn(Throwable t) {
				assertTrue(t == ex);
				return shouldRollback;
			}
		};

		Method m = ITestBean.class.getMethod("exceptional", new Class[] { Throwable.class });
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		TransactionStatus status = new DefaultTransactionStatus(null, false, false, false, false, null);
		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Gets additional call(s) from TransactionControl

		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status, 1);

		if (shouldRollback) {
			ptm.rollback(status);
		}
		else {
			ptm.commit(status);
		}
		TransactionSystemException tex = new TransactionSystemException("system exception");
		if (rollbackException) {
			ptxControl.setThrowable(tex, 1);
		}
		else {
			ptxControl.setVoidCallable(1);
		}
		ptxControl.replay();

		TestBean tb = new TestBean();
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		ti.setTransactionAttributeSource(tas);

		ProxyFactory pf = new ProxyFactory(tb);
		pf.addInterceptor(0, ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		try {
			itb.exceptional(ex);
			fail("Should have thrown exception");
		}
		catch (Throwable t) {
			if (rollbackException) {
				assertEquals("Caught wrong exception", tex, t );
			}
			else {
				assertEquals("Caught wrong exception", ex, t);
			}
		}

		ptxControl.verify();
	}

	/**
	 * Simulate a transaction infrastructure failure.
	 * Shouldn't invoke target method.
	 * @throws java.lang.Exception
	 */
	public void testCannotCreateTransaction() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = ITestBean.class.getMethod("getName", null);
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);

		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();
		// Expect a transaction
		ptm.getTransaction(txatt);
		CannotCreateTransactionException ex = new CannotCreateTransactionException("foobar", null);
		ptxControl.setThrowable(ex);
		ptxControl.replay();

		TestBean tb = new TestBean() {
			public String getName() {
				throw new UnsupportedOperationException("Shouldn't have invoked target method when couldn't create transaction for transactional method");
			}
		};
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		ti.setTransactionAttributeSource(tas);

		ProxyFactory pf = new ProxyFactory(tb);
		pf.addInterceptor(0, ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		try {
			itb.getName();
			fail("Shouldn't have invoked method");
		}
		catch (CannotCreateTransactionException thrown) {
			assertTrue(thrown == ex);
		}
		ptxControl.verify();
	}

	/**
	 * Simulate failure of the underlying transaction infrastructure
	 * to commit.
	 * Check that the target method was invoked, but that the
	 * transaction infrastructure exception was thrown to the
	 * client
	 * @throws java.lang.Exception
	 */
	public void testCannotCommitTransaction() throws Exception {
		TransactionAttribute txatt = new DefaultTransactionAttribute();

		Method m = ITestBean.class.getMethod("setName", new Class[] { String.class} );
		MapTransactionAttributeSource tas = new MapTransactionAttributeSource();
		tas.register(m, txatt);
		Method m2 = ITestBean.class.getMethod("getName", null);
		// No attributes for m2

		MockControl ptxControl = MockControl.createControl(PlatformTransactionManager.class);
		PlatformTransactionManager ptm = (PlatformTransactionManager) ptxControl.getMock();

		TransactionStatus status = new DefaultTransactionStatus(null, false, false, false, false, null);
		ptm.getTransaction(txatt);
		ptxControl.setReturnValue(status);
		UnexpectedRollbackException ex = new UnexpectedRollbackException("foobar", null);
		ptm.commit(status);
		ptxControl.setThrowable(ex);
		ptxControl.replay();

		TestBean tb = new TestBean();
		TransactionInterceptor ti = new TransactionInterceptor();
		ti.setTransactionManager(ptm);
		ti.setTransactionAttributeSource(tas);

		ProxyFactory pf = new ProxyFactory(tb);
		pf.addInterceptor(0, ti);
		ITestBean itb = (ITestBean) pf.getProxy();

		String name = "new name";
		try {
			itb.setName(name);
			fail("Shouldn't have succeeded");
		}
		catch (UnexpectedRollbackException thrown) {
			assertTrue(thrown == ex);
		}

		// Should have invoked target and changed name
		assertTrue(itb.getName() == name);
		ptxControl.verify();
	}

}
