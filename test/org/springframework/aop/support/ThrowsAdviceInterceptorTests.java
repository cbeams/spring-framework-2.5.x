/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.transaction.TransactionRolledbackException;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.aop.framework.AbstractAopProxyTests;
import org.springframework.aop.framework.AopConfigException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ThrowsAdviceInterceptorTests.java,v 1.1 2003-12-05 16:28:26 johnsonr Exp $
 */
public class ThrowsAdviceInterceptorTests extends TestCase {

	/**
	 * Constructor for ThrowsAdviceInterceptorTest.
	 * @param arg0
	 */
	public ThrowsAdviceInterceptorTests(String arg0) {
		super(arg0);
	}
	
	public void testNoHandlerMethods() {
		Object o = new Object();
		try {
			new ThrowsAdviceInterceptor(o);
			fail("Should require one handler method at least");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}
	
	public void testNotInvoked() throws Throwable {
		MyThrowsHandler th = new MyThrowsHandler();
		ThrowsAdviceInterceptor ti = new ThrowsAdviceInterceptor(th);
		Object ret = new Object();
		MockControl mc = MockControl.createControl(MethodInvocation.class);
		MethodInvocation mi = (MethodInvocation) mc.getMock();
		mi.proceed();
		mc.setReturnValue(ret, 1);
		mc.replay();
		assertEquals(ret, ti.invoke(mi));
		assertEquals(0, th.getCalls());
		mc.verify();
	}
	
	public void testNoHandlerMethodForThrowable() throws Throwable {
		MyThrowsHandler th = new MyThrowsHandler();
		ThrowsAdviceInterceptor ti = new ThrowsAdviceInterceptor(th);
		assertEquals(2, ti.getHandlerMethodCount());
		Exception ex = new Exception();
		MockControl mc = MockControl.createControl(MethodInvocation.class);
		MethodInvocation mi = (MethodInvocation) mc.getMock();
		mi.proceed();
		mc.setThrowable(ex);
		mc.replay();
		try {
			ti.invoke(mi);
			fail();
		}
		catch (Exception caught) {
			assertEquals(ex, caught);
		}
		assertEquals(0, th.getCalls());
		mc.verify();
	}
	
	public void testCorrectHandlerUsed() throws Throwable {
		MyThrowsHandler th = new MyThrowsHandler();
		ThrowsAdviceInterceptor ti = new ThrowsAdviceInterceptor(th);
		ServletException ex = new ServletException();
		MockControl mc = MockControl.createControl(MethodInvocation.class);
		MethodInvocation mi = (MethodInvocation) mc.getMock();
		mi.proceed();
		mc.setThrowable(ex);
		mc.replay();
		try {
			ti.invoke(mi);
			fail();
		}
		catch (Exception caught) {
			assertEquals(ex, caught);
		}
		assertEquals(1, th.getCalls());
		assertEquals(1, th.getCalls("servletException"));
		mc.verify();
	}
	
	public void testCorrectHandlerUsedForSubclass() throws Throwable {
		MyThrowsHandler th = new MyThrowsHandler();
		ThrowsAdviceInterceptor ti = new ThrowsAdviceInterceptor(th);
		// Extends RemoteException
		TransactionRolledbackException ex = new TransactionRolledbackException();
		MockControl mc = MockControl.createControl(MethodInvocation.class);
		MethodInvocation mi = (MethodInvocation) mc.getMock();
		mi.proceed();
		mc.setThrowable(ex);
		mc.replay();
		try {
			ti.invoke(mi);
			fail();
		}
		catch (Exception caught) {
			assertEquals(ex, caught);
		}
		assertEquals(1, th.getCalls());
		assertEquals(1, th.getCalls("remoteException"));
		mc.verify();
	}
	
	public void testHandlerMethodThrowsException() throws Throwable {
		final Throwable t = new Throwable();
		MyThrowsHandler th = new MyThrowsHandler() {
			public void afterThrowing(RemoteException ex) throws Throwable {
				super.afterThrowing(ex);
				throw t;
			}
		};
		ThrowsAdviceInterceptor ti = new ThrowsAdviceInterceptor(th);
		// Extends RemoteException
		TransactionRolledbackException ex = new TransactionRolledbackException();
		MockControl mc = MockControl.createControl(MethodInvocation.class);
		MethodInvocation mi = (MethodInvocation) mc.getMock();
		mi.proceed();
		mc.setThrowable(ex);
		mc.replay();
		try {
			ti.invoke(mi);
			fail();
		}
		catch (Throwable caught) {
			assertEquals(t, caught);
		}
		assertEquals(1, th.getCalls());
		assertEquals(1, th.getCalls("remoteException"));
		mc.verify();
	}
	
	public static class MyThrowsHandler extends AbstractAopProxyTests.MethodCounter {
		 public void afterThrowing(ServletException ex) {
		 	count("servletException");
		 }
		public void afterThrowing(RemoteException ex) throws Throwable {
			count("remoteException");
		 }
	}
	
	public interface IEcho {
		int echoException(int i, Throwable t) throws Throwable;
		int getA();
		void setA(int a);
	}
	
	public static class Echo implements IEcho {
		private int a;
		
		public int echoException(int i, Throwable t) throws Throwable {
			if (t != null)
				throw t;
			return i;
		}
		public void setA(int a) {
			this.a = a;
		}
		public int getA() {
			return a;
		}
	}

}
