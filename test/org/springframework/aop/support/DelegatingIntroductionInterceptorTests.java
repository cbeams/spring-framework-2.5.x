/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.easymock.MockControl;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.TimeStamped;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * 
 * @author Rod Johnson
 * @since 13-May-2003
 * @version $Revision: 1.5 $
 */
public class DelegatingIntroductionInterceptorTests extends TestCase {

	/**
	 * Constructor for DelegatingIntroductionInterceptorTests.
	 * @param arg0
	 */
	public DelegatingIntroductionInterceptorTests(String arg0) {
		super(arg0);
	}
	
	public void testNullTarget() throws Exception {
		try {
			IntroductionInterceptor ii = new DelegatingIntroductionInterceptor(null);
			fail("Shouldn't accept null target");
		}
		catch (IllegalArgumentException ex) {
			// OK
		}
	}
	
	public void testIntroductionInterceptorWithDelegation() throws Exception {
		TestBean raw = new TestBean();
		assertTrue(! (raw instanceof TimeStamped));
		ProxyFactory factory = new ProxyFactory(raw);
	
		MockControl tsControl = MockControl.createControl(TimeStamped.class);
		TimeStamped ts = (TimeStamped) tsControl.getMock();
		ts.getTimeStamp();
		long timestamp = 111L;
		tsControl.setReturnValue(timestamp, 1);
		tsControl.replay();

		factory.addAdvisor(0, new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts)));
		
		TimeStamped tsp = (TimeStamped) factory.getProxy();
		assertTrue(tsp.getTimeStamp() == timestamp);
	
		tsControl.verify();
	}
	
	
	public void testAutomaticInterfaceRecognitionInDelegate() throws Exception {
		final long t = 1001L;
		class Test implements TimeStamped, ITest {
			public void foo() throws Exception {
			}
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new DelegatingIntroductionInterceptor(new Test());
		
		TestBean target = new TestBean();
		
		ProxyFactory pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));;
		
		//assertTrue(Arrays.binarySearch(pf.getProxiedInterfaces(), TimeStamped.class) != -1);
		TimeStamped ts = (TimeStamped) pf.getProxy();
		
		assertTrue(ts.getTimeStamp() == t);
		((ITest) ts).foo();
		
		((ITestBean) ts).getAge();
	}
	
	
	public void testAutomaticInterfaceRecognitionInSubclass() throws Exception {
		final long t = 1001L;
		class TestII extends DelegatingIntroductionInterceptor implements TimeStamped, ITest {
			public void foo() throws Exception {
			}
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new TestII();
		
		TestBean target = new TestBean();
		
		ProxyFactory pf = new ProxyFactory(target);
		IntroductionAdvisor ia = new DefaultIntroductionAdvisor(ii);
		assertTrue(ia.isPerInstance());
		pf.addAdvisor(0, ia);
		
		//assertTrue(Arrays.binarySearch(pf.getProxiedInterfaces(), TimeStamped.class) != -1);
		TimeStamped ts = (TimeStamped) pf.getProxy();
		
		assertTrue(ts instanceof TimeStamped);
		// Shoulnd't proxy framework interfaces
		assertTrue(!(ts instanceof MethodInterceptor));
		assertTrue(!(ts instanceof IntroductionInterceptor));
		
		assertTrue(ts.getTimeStamp() == t);
		((ITest) ts).foo();
		((ITestBean) ts).getAge();
		
		// Test removal
		ii.suppressInterface(TimeStamped.class);
		// Note that we need to construct a new proxy factory,
		// or suppress the interface on the proxy factory
		pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));
		Object o = pf.getProxy();
		assertTrue(!(o instanceof TimeStamped));
	}
	
	
	// should fail with delegate itself and no subclass?
	//public void testNoInterfaces() {
	//}
	
	public static class TargetClass extends TestBean implements TimeStamped {
		long t;
		public TargetClass(long t) {
			this.t = t;
		}
		public long getTimeStamp() {
			return t;
		}
	};
	
	// test when target implements the interface: should get interceptor by preference
	public void testIntroductionMasksTargetImplementation() throws Exception {
		final long t = 1001L;
		class TestII extends DelegatingIntroductionInterceptor implements TimeStamped {
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new TestII();
	
		// != t
		TestBean target = new TargetClass(t + 1);
	
		ProxyFactory pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));
	
		TimeStamped ts = (TimeStamped) pf.getProxy();
		// From introduction interceptor, not target
		assertTrue(ts.getTimeStamp() == t);
	}
	
	static interface ITest {
		void foo() throws Exception;
	}

}
