package org.springframework.aop.interceptor;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.util.SerializationTestUtils;

/**
 * @author Juergen Hoeller
 * @since 06.04.2004
 */
public class ConcurrencyThrottleInterceptorTests extends TestCase {

	protected static final Log logger = LogFactory.getLog(ConcurrencyThrottleInterceptorTests.class);

	public static final int NR_OF_THREADS = 100;

	public static final int NR_OF_ITERATIONS = 1000;
	
	public void testSerializable() throws Exception {
		ConcurrencyThrottleInterceptor cti = new ConcurrencyThrottleInterceptor();
		SerializationTestUtils.testSerialization(cti);
	}

	public void testMultipleThreadsWithLimit1() {
		testMultipleThreads(1);
	}

	public void testMultipleThreadsWithLimit10() {
		testMultipleThreads(10);
	}

	private void testMultipleThreads(int concurrencyLimit) {
		TestBean tb = new TestBean();
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(new Class[] {ITestBean.class});
		ConcurrencyThrottleInterceptor cti = new ConcurrencyThrottleInterceptor();
		cti.setConcurrencyLimit(concurrencyLimit);
		proxyFactory.addAdvice(cti);
		proxyFactory.setTarget(tb);
		ITestBean proxy = (ITestBean) proxyFactory.getProxy();

		Thread[] threads = new Thread[NR_OF_THREADS];
		for (int i = 0; i < NR_OF_THREADS; i++) {
			threads[i] = new ConcurrencyTest(proxy, null);
			threads[i].start();
		}
		for (int i = 0; i < NR_OF_THREADS / 10; i++) {
			try {
				Thread.sleep(5);
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			threads[i] = new ConcurrencyTest(proxy, i % 2 == 0 ? (Throwable) new OutOfMemoryError() :
			                                        (Throwable) new IllegalStateException());
			threads[i].start();
		}
		for (int i = 0; i < NR_OF_THREADS; i++) {
			try {
				threads[i].join();
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}


	private static class ConcurrencyTest extends Thread {

		private ITestBean proxy;
		private Throwable ex;

		public ConcurrencyTest(ITestBean proxy, Throwable ex) {
			this.proxy = proxy;
			this.ex = ex;
		}

		public void run() {
			if (this.ex != null) {
				try {
					this.proxy.exceptional(this.ex);
				}
				catch (RuntimeException ex) {
					if (ex == this.ex) {
						logger.debug("Expected exception thrown", ex);
					}
					else {
						// should never happen
						ex.printStackTrace();
					}
				}
				catch (Error err) {
					if (err == this.ex) {
						logger.debug("Expected exception thrown", err);
					}
					else {
						// should never happen
						ex.printStackTrace();
					}
				}
				catch (Throwable ex) {
					// should never happen
					ex.printStackTrace();
				}
			}
			else {
				for (int i = 0; i < NR_OF_ITERATIONS; i++) {
					this.proxy.getName();
				}
			}
			logger.debug("finished");
		}
	}

}
