/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import net.sf.cglib.CodeGenerationException;

import org.aopalliance.intercept.AspectException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;
import org.springframework.aop.InterceptionAroundAdvisor;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.aop.interceptor.InvokerInterceptor;
import org.springframework.aop.support.DynamicMethodMatcherPointcutAroundAdvisor;
import org.springframework.aop.support.SimpleIntroductionAdvice;
import org.springframework.aop.support.StaticMethodMatcherPointcutAroundAdvisor;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.core.TimeStamped;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13-Mar-2003
 * @version $Id: AopProxyTests.java,v 1.13 2003-11-16 12:54:58 johnsonr Exp $
 */
public class AopProxyTests extends TestCase {

	public AopProxyTests(String arg0) {
		super(arg0);
	}

	public void testNullConfig() {
		try {
			AopProxy aop = new AopProxy(null, new MethodInvocationFactorySupport());
			aop.getProxy();
			fail("Shouldn't allow null interceptors");
		} catch (AopConfigException ex) {
			// Ok
		}
	}

	public void testNoInterceptors() {
		Advised pc =
			new AdvisedSupport(new Class[] { ITestBean.class }, false);
		// Add no interceptors
		try {
			AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());
			aop.getProxy();
			fail("Shouldn't allow no interceptors");
		} catch (AopConfigException ex) {
			// Ok
		}
	}
	
	/**
	 * Check that the two MethodInvocations necessary are independent and
	 * don't conflict
	 */
	public void testOneAdvisedObjectCallsAnother() {
		int age1 = 33;
		int age2 = 37;
		
		TestBean target1 = new TestBean();
		ProxyFactory pf1 = new ProxyFactory(target1);
		DebugInterceptor di1 = new DebugInterceptor();
		pf1.addInterceptor(0, di1);
		ITestBean advised1 = (ITestBean) pf1.getProxy();
		advised1.setAge(age1); // = 1 invocation
		
		TestBean target2 = new TestBean();
		ProxyFactory pf2 = new ProxyFactory(target2);
		DebugInterceptor di2 = new DebugInterceptor();
		pf2.addInterceptor(0, di2);
		ITestBean advised2 = (ITestBean) pf2.getProxy();
		advised2.setAge(age2);
		advised1.setSpouse(advised2); // = 2 invocations
		
		assertEquals("Advised one has correct age", age1, advised1.getAge()); // = 3 invocations
		assertEquals("Advised two has correct age", age2, advised2.getAge());
		// Means extra call on advised 2
		assertEquals("Advised one spouse has correct age", age2, advised1.getSpouse().getAge()); // = 4 invocations on 1 and another one on 2
		
		assertEquals("one was invoked correct number of times", 4, di1.getCount());
		// Got hit by call to advised1.getSpouse().getAge()
		assertEquals("one was invoked correct number of times", 3, di2.getCount());
	}
	
	
	public void testReentrance() {
		int age1 = 33;
	
		TestBean target1 = new TestBean();
		ProxyFactory pf1 = new ProxyFactory(target1);
		DebugInterceptor di1 = new DebugInterceptor();
		pf1.addInterceptor(0, di1);
		ITestBean advised1 = (ITestBean) pf1.getProxy();
		advised1.setAge(age1); // = 1 invocation
		advised1.setSpouse(advised1); // = 2 invocations
	
		assertEquals("one was invoked correct number of times", 2, di1.getCount());
		
		assertEquals("Advised one has correct age", age1, advised1.getAge()); // = 3 invocations
		assertEquals("one was invoked correct number of times", 3, di1.getCount());
		
		// = 5 invocations, as reentrant call to spouse is advised also
		assertEquals("Advised spouse has correct age", age1, advised1.getSpouse().getAge()); 
		
		assertEquals("one was invoked correct number of times", 5, di1.getCount());
	}
	
	public interface INeedsToSeeProxy {
		int getCount();
		void incrementViaThis();
		void incrementViaProxy();
		void increment();
	}
	
	public static class NeedsToSeeProxy implements INeedsToSeeProxy {
		private int count;
		public int getCount() {
			return count;
		}
		public void incrementViaThis() {
			this.increment();
		}
		public void incrementViaProxy() {
			INeedsToSeeProxy thisViaProxy = (INeedsToSeeProxy) AopContext.currentProxy();
			thisViaProxy.increment();
			Advised advised = (Advised) thisViaProxy;
			checkAdvised(advised);
		}
		
		protected void checkAdvised(Advised advised) {
		}
	
		public void increment() {
			++count;
		}
	};
	
	public static class TargetChecker extends NeedsToSeeProxy {
		protected void checkAdvised(Advised advised) {
			assertEquals(advised.getTarget(), this);
		}
	};
	
	public void testTargetCanGetProxyViaDP() {
		DebugInterceptor di = new DebugInterceptor();
		INeedsToSeeProxy et = new TargetChecker();
		ProxyFactory pf1 = new ProxyFactory(et);
		pf1.setExposeProxy(true);
		assertTrue(pf1.getExposeProxy());
	
		pf1.addInterceptor(0, di);
		INeedsToSeeProxy proxied = (INeedsToSeeProxy) pf1.getProxy();
		assertEquals(0, di.getCount());
		assertEquals(0, et.getCount());
		proxied.incrementViaThis();
		assertEquals("Increment happened", 1, et.getCount());
		assertEquals("Only one invocation via AOP as use of this wasn't proxied", 1, di.getCount());
	
		proxied.incrementViaProxy();
		assertEquals("Increment happened", 2, et.getCount());
		assertEquals("Two more invocations via AOP as the first call was reentrant through the proxy", 3, di.getCount());
	}
			
	public void testTargetCanGetProxyViaCGLIB() {
		DebugInterceptor di = new DebugInterceptor();
		NeedsToSeeProxy et = new TargetChecker();
		ProxyFactory pf1 = new ProxyFactory(et);
		// Force it to use CGLIB
		pf1.setProxyTargetClass(true);
		pf1.setExposeProxy(true);
		assertTrue(pf1.getExposeProxy());
		
		pf1.addInterceptor(0, di);
		NeedsToSeeProxy proxied = (NeedsToSeeProxy) pf1.getProxy();
		assertEquals(0, di.getCount());
		assertEquals(0, et.count);
		proxied.incrementViaThis();
		assertEquals("Increment happened", 1, et.count);
		assertEquals("Only one invocation via AOP as use of this wasn't proxied", 1, di.getCount());
		
		proxied.incrementViaProxy();
		assertEquals("Increment happened", 2, et.count);
		assertEquals("Two more invocations via AOP as the first call was reentrant through the proxy", 3, di.getCount());
	}
	
	public void testTargetCantGetProxyByDefault() {
		NeedsToSeeProxy et = new NeedsToSeeProxy();
		ProxyFactory pf1 = new ProxyFactory(et);
		assertFalse(pf1.getExposeProxy());
		INeedsToSeeProxy proxied = (INeedsToSeeProxy) pf1.getProxy();
		try {
			proxied.incrementViaProxy();
			fail("Should have failed to get proxy as exposeProxy wasn't set to true");
		}
		catch (AspectException ex) {
			// Ok
		}
	}
	

	public void testInterceptorIsInvoked() throws Throwable {
		// Test return value
		int age = 25;
		MockControl miControl = MockControl.createControl(MethodInterceptor.class);
		MethodInterceptor mi = (MethodInterceptor) miControl.getMock();

		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class }, false);
		pc.addInterceptor(mi);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		// Really would like to permit null arg:can't get exact mi
		mi.invoke(null);
		//mi.invoke(new MethodInvocationImpl(aop, null, ITestBean.class,
		//	ITestBean.class.getMethod("getAge", null),
		//	null, l, r));
		//miControl.
		//miControl.setReturnValue(new Integer(age));
		// Have disabled strong argument checking
		miControl.setDefaultReturnValue(new Integer(age));
		miControl.replay();

		ITestBean tb = (ITestBean) aop.getProxy();
		assertTrue("correct return value", tb.getAge() == age);
		miControl.verify();
	}

	public void testContext() throws Throwable {
		testContext(true);
	}

	public void testNoContext() throws Throwable {
		testContext(false);
	}

	/**
	 * @param context if true, want context
	 * @throws Throwable
	 */
	private void testContext(final boolean context) throws Throwable {
		final String s = "foo";
		// Test return value
		MethodInterceptor mi = new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				if (!context) {
					assertNoInvocationContext();
				} else {
					assertTrue("have context", AopContext.currentInvocation() != null);
				}
				return s;
			}
		};
		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class }, context);
		pc.addInterceptor(mi);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		assertNoInvocationContext();
		ITestBean tb = (ITestBean) aop.getProxy();
		assertNoInvocationContext();
		assertTrue("correct return value", tb.getName() == s);
	}

	/**
	 * Test that the proxy returns itself when the
	 * target returns <code>this</code>
	 * @throws Throwable
	 */
	public void testTargetReturnsThis() throws Throwable {
		// Test return value
		TestBean raw = new TestBean() {
			public ITestBean getSpouse() {
				return this;
			}
		};
		InvokerInterceptor ii = new InvokerInterceptor(raw);

		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class }, false);
		pc.addInterceptor(ii);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		ITestBean tb = (ITestBean) aop.getProxy();
		assertTrue("this is wrapped in a proxy", Proxy.isProxyClass(tb.getSpouse().getClass()));

		assertTrue("this return is wrapped in proxy", tb.getSpouse() == tb);
	}

	public void testProxyIsJustInterface() throws Throwable {
		TestBean raw = new TestBean();
		raw.setAge(32);
		InvokerInterceptor ii = new InvokerInterceptor(raw);
		Advised pc = new AdvisedSupport(new Class[] {ITestBean.class}, false);
		pc.addInterceptor(ii);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		Object proxy = aop.getProxy();
		assertTrue(proxy instanceof ITestBean);
		assertTrue(!(proxy instanceof TestBean));
	}

	public void testProxyCanBeFullClass() throws Throwable {
		TestBean raw = new TestBean();
		raw.setAge(32);
		InvokerInterceptor ii = new InvokerInterceptor(raw);
		Advised pc = new AdvisedSupport(new Class[] {}, false);
		pc.addInterceptor(ii);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		Object proxy = aop.getProxy();
		assertTrue(proxy instanceof ITestBean);
		assertTrue(proxy instanceof TestBean);
		TestBean tb = (TestBean) proxy;
		assertTrue("Correct age", tb.getAge() == 32);
	}

	/**
	 * Equality means set of interceptors and
	 * set of interfaces are equal
	 * @throws Throwable
	 */
	public void testEqualsWithJdkProxy() throws Throwable {
		TestBean raw = new EqualsTestBean();
		InvokerInterceptor ii = new InvokerInterceptor(raw);

		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class }, false);
		pc.addInterceptor(ii);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		ITestBean tb = (ITestBean) aop.getProxy();
		assertTrue("proxy equals itself", tb.equals(tb));
		assertTrue("proxy isn't equal to proxied object", !tb.equals(raw));
		//test null eq
		assertTrue("tb.equals(null) is false", !tb.equals(null));
		assertTrue("test equals proxy", tb.equals(aop));

		// Test with AOP proxy with additional interceptor
		Advised pc2 = new AdvisedSupport(new Class[] { ITestBean.class }, false);
		pc2.addInterceptor(new DebugInterceptor());
		assertTrue(!tb.equals(new AopProxy(pc2, new MethodInvocationFactorySupport())));

		// Test with any old dynamic proxy
		assertTrue(!tb.equals(Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ITestBean.class }, new InvocationHandler() {
			public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
				throw new UnsupportedOperationException("invoke");
			}
		})));
	}

/*
	public void testCanAttach() throws Throwable {
		final TrapInterceptor tii = new TrapInvocationInterceptor();

		ProxyConfig pc = new ProxyConfigSupport(new Class[] { ITestBean.class }, false);
		pc.addInterceptor(tii);
		pc.addInterceptor(new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				assertTrue("Saw same interceptor", invocation == tii.invocation);
				return null;
			}
		});
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		ITestBean tb = (ITestBean) aop.getProxy();
		tb.getSpouse();
		assertTrue(tii.invocation != null);
		
		// TODO strengthen this
	//	assertTrue(tii.invocation.getProxy() == tb);
		assertTrue(tii.invocation.getThis() == null);
	}
*/

	/**
	 * TODO also test undeclared: decide what it should do!
	 */
	public void testDeclaredException() throws Throwable {
		final Exception ex = new Exception();
		// Test return value
		MethodInterceptor mi = new MethodInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				throw ex;
			}
		};
		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class }, true);
		pc.addInterceptor(mi);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		try {
			ITestBean tb = (ITestBean) aop.getProxy();
			// Note: exception param below isn't used
			tb.exceptional(ex);
			fail("Should have thrown exception raised by interceptor");
		} catch (Exception thrown) {
			assertTrue("exception matches: not " + thrown, ex == thrown);
		}
	}

	public void testTargetCanGetInvocation() throws Throwable {
		final ContextTestBean expectedTarget = new ContextTestBean() {
			protected void assertions(MethodInvocation invocation) {
				assertTrue(invocation.getThis() == this);
				assertTrue("Invocation should be on ITestBean: " + invocation.getMethod(), 
					invocation.getMethod().getDeclaringClass() == ITestBean.class);
			}
		};
		
		Advised pc = new AdvisedSupport(new Class[] { ITestBean.class, IOther.class }, true);
		TrapTargetInterceptor tii = new TrapTargetInterceptor() {
			public Object invoke(MethodInvocation invocation) throws Throwable {
				// Assert that target matches BEFORE invocation returns
				assertTrue(invocation.getThis() == expectedTarget);
				return super.invoke(invocation);
			}
		};
		pc.addInterceptor(tii);
		InvokerInterceptor ii = new InvokerInterceptor(expectedTarget);
		pc.addInterceptor(ii);
		AopProxy aop = new AopProxy(pc, new MethodInvocationFactorySupport());

		ITestBean tb = (ITestBean) aop.getProxy();
		tb.getName();
		// Not safe to trap invocation
		//assertTrue(tii.invocation == target.invocation);
		
		//assertTrue(target.invocation.getProxy() == tb);

	//	((IOther) tb).absquatulate();
		//MethodInvocation minv =  tii.invocation;
		//assertTrue("invoked on iother, not " + minv.getMethod().getDeclaringClass(), minv.getMethod().getDeclaringClass() == IOther.class);
		//assertTrue(target.invocation == tii.invocation);
	}

	/**
	 * Throw an exception if there is an Invocation
	 */
	private void assertNoInvocationContext() {
		try {
			AopContext.currentInvocation();
			fail("Expected no invocation context");
		} catch (AspectException ex) {
			// ok
		}
	}
	
	

	/**
	 * Test stateful interceptor
	 * @throws Throwable
	 */
	public void testMixin() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { Lockable.class, ITestBean.class });
		pc.addAdvisor(new SimpleIntroductionAdvice(new LockMixin(), Lockable.class));
		pc.addInterceptor(new InvokerInterceptor(tb));
		
		int newAge = 65;
		ITestBean itb = (ITestBean) pc.getProxy();
		itb.setAge(newAge);
		assertTrue(itb.getAge() == newAge);

		Lockable lockable = (Lockable) itb;
		assertFalse(lockable.locked());
		lockable.lock();
		
		assertTrue(itb.getAge() == newAge);
		try {
			itb.setAge(1);
			fail("Setters should fail when locked");
		} 
		catch (LockedException ex) {
			// ok
		}
		assertTrue(itb.getAge() == newAge);
		
		// Unlock
		assertTrue(lockable.locked());
		lockable.unlock();
		itb.setAge(1);
		assertTrue(itb.getAge() == 1);
	}
	
	
	public void testReplaceArgument() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		pc.addAdvisor(new StringSetterNullReplacementAdvice());
		pc.addInterceptor(new InvokerInterceptor(tb));
	
		ITestBean t = (ITestBean) pc.getProxy();
		int newAge = 5;
		t.setAge(newAge);
		assertTrue(t.getAge() == newAge);
		String newName = "greg";
		t.setName(newName);
		assertEquals(newName, t.getName());
		
		t.setName(null);
		// Null replacement magic should work
		assertTrue(t.getName().equals(""));
	}
	
	public void testCanCastJdkProxyToProxyConfig() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(tb);
		DebugInterceptor di = new DebugInterceptor();
		pc.addInterceptor(0, di);

		ITestBean t = (ITestBean) pc.getProxy();
		assertEquals(0, di.getCount());
		t.setAge(23);
		assertEquals(23, t.getAge());
		assertEquals(2, di.getCount());
		
		Advised config = (Advised) t;
		assertEquals("Have two advisors", 2, config.getAdvisors().length);
		assertEquals(di, ((InterceptionAroundAdvisor) config.getAdvisors()[0]).getInterceptor());
		DebugInterceptor di2 = new DebugInterceptor();
		config.addInterceptor(1, di2);
		t.getName();
		assertEquals(3, di.getCount());
		assertEquals(1, di2.getCount());
		config.removeInterceptor(di);
		t.getAge();
		// Unchanged
		assertEquals(3, di.getCount());
		assertEquals(2, di2.getCount());
	}
	
	
	public static class NoInterfaces {
		private int age;
		public int getAge() { 
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
	}
	
	public void testCanCastCglibProxyToProxyConfig() throws Throwable {
		NoInterfaces target = new NoInterfaces();
		ProxyFactory pc = new ProxyFactory(target);
		DebugInterceptor di = new DebugInterceptor();
		pc.addInterceptor(0, di);

		NoInterfaces t = (NoInterfaces) pc.getProxy();
		assertEquals(0, di.getCount());
		t.setAge(23);
		assertEquals(23, t.getAge());
		assertEquals(2, di.getCount());
	
		Advised config = (Advised) t;
		assertEquals("Have two interceptors", 2, config.getAdvisors().length);
		assertEquals(di, ((InterceptionAroundAdvisor) config.getAdvisors()[0]).getInterceptor());
		DebugInterceptor di2 = new DebugInterceptor();
		config.addInterceptor(1, di2);
		t.getAge();
		assertEquals(3, di.getCount());
		assertEquals(1, di2.getCount());
		config.removeInterceptor(di);
		t.getAge();
		// Unchanged
		assertEquals(3, di.getCount());
		assertEquals(2, di2.getCount());
	}
	
	public void testIntroductionWithCglibProxy() throws Throwable {
		NoInterfaces target = new NoInterfaces();
		ProxyFactory pc = new ProxyFactory(target);
		// Override default behaviour to force use of CGLIB
		pc.setProxyTargetClass(true);
		DebugInterceptor di = new DebugInterceptor();
		pc.addInterceptor(0, di);
		long time = 2222;
		TimestampIntroductionInterceptor tii = new TimestampIntroductionInterceptor(time);
		pc.addAdvisor(0, new SimpleIntroductionAdvice(tii, TimeStamped.class));
		
		TimeStamped ts = (TimeStamped) pc.getProxy();
		assertEquals(time, ts.getTimeStamp());
		assertTrue(ts.getClass().getName() + "should be a CGLIB subclass of NoInterfaces class", ts instanceof NoInterfaces);
		
		NoInterfaces proxied = (NoInterfaces) pc.getProxy();
		proxied.setAge(25);
		assertEquals(25, proxied.getAge());
	}
	
	public void testCGLIBProxyingGivesMeaningfulExceptionIfAskedToProxyNonvisibleClass() {
		class YouCantSeeThis {
			void hidden() {
			}
		};
		YouCantSeeThis mine = new YouCantSeeThis();
		try {
			ProxyFactory pf = new ProxyFactory(mine);
			pf.getProxy();
			fail("Shouldn't be able to proxy non-visible class with CGLIB");
		}
		catch (AspectException ex) {
			// Check that stack trace is preserved
			assertTrue(ex.getRootCause() instanceof CodeGenerationException);
			
			// Check that error message is helpful
			
			// TODO check why these methods fail with NPE on AOP Alliance code
			//assertTrue(ex.getMessage().indexOf("final") != -1);
			//assertTrue(ex.getMessage().indexOf("visible") != -1);
		}
		
	}
	
	
	public void testCannotAddIntroductionInterceptorExceptInIntroductionAdvice() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addInterceptor(0, new TimestampIntroductionInterceptor());
			fail("Shouldn't be able to add introduction interceptor except via introduction advice");
		}
		catch (AopConfigException ex) {
			assertTrue(ex.getMessage().indexOf("ntroduction") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) pc.getProxy();
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	/**
	 * Check that the introduction advice isn't allowed to introduce interfaces
	 * that are unsupported by the IntroductionInterceptor
	 * @throws Throwable
	 */
	public void testCannotAddIntroductionAdviceWithUnimplementedInterface() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addAdvisor(0, new SimpleIntroductionAdvice(new TimestampIntroductionInterceptor(), ITestBean.class));
			fail("Shouldn't be able to add introduction advice introducing an unimplemented interface");
		}
		catch (AopConfigException ex) {
			//assertTrue(ex.getMessage().indexOf("ntroduction") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) pc.getProxy();
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	/**
	 * Should only be able to introduce interfaces, not classes
	 * @throws Throwable
	 */
	public void testCannotAddIntroductionAdviceToIntroduceClass() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		ProxyFactory pc = new ProxyFactory(target);
		try {
			pc.addAdvisor(0, new SimpleIntroductionAdvice(new TimestampIntroductionInterceptor(), TestBean.class));
			fail("Shouldn't be able to add introduction advice that introduces a class, rather than an interface");
		}
		catch (AopConfigException ex) {
			assertTrue(ex.getMessage().indexOf("interface") > -1);
		}
		// Check it still works: proxy factory state shouldn't have been corrupted
		ITestBean proxied = (ITestBean) pc.getProxy();
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	public void testAdviceChangeCallbacks() throws Throwable {
		TestBean target = new TestBean();
		target.setAge(21);
		
		AdviceChangeCountingProxyFactory pc = new AdviceChangeCountingProxyFactory(target);
		RefreshCountingMethodInvocationFactory mif = new RefreshCountingMethodInvocationFactory();
		pc.setMethodInvocationFactory(mif);
		assertFalse(pc.isActive());
		assertEquals(0, mif.refreshes);
		ITestBean proxied = (ITestBean) pc.getProxy();
		assertEquals(1, mif.refreshes);
		assertTrue(pc.isActive());
		assertEquals(target.getAge(), proxied.getAge());
		assertEquals(0, pc.adviceChanges);
		DebugInterceptor di = new DebugInterceptor();
		pc.addInterceptor(0, di);
		assertEquals(1, pc.adviceChanges);
		assertEquals(2, mif.refreshes);
		assertEquals(target.getAge(), proxied.getAge());
		pc.removeInterceptor(di);
		assertEquals(2, pc.adviceChanges);
		assertEquals(3, mif.refreshes);
		assertEquals(target.getAge(), proxied.getAge());
	}
	
	
	public static class AdviceChangeCountingProxyFactory extends ProxyFactory {
		public int adviceChanges;
		public AdviceChangeCountingProxyFactory(Object target) {
			super(target);
		}
		protected void onAdviceChanged() {
			++adviceChanges;
		}
	}
	
	public class RefreshCountingMethodInvocationFactory extends MethodInvocationFactorySupport {
		public int refreshes;
		
		public void refresh(Advised pc) {
			++refreshes;
		}

	}
	
	/**
	 * Fires on setter methods that take a string. Replaces null arg
	 * with ""
	 */
	public static class StringSetterNullReplacementAdvice extends DynamicMethodMatcherPointcutAroundAdvisor {
		
		private static MethodInterceptor cleaner = new MethodInterceptor() {
			public Object invoke(MethodInvocation mi) throws Throwable {
				// We know it can only be invoked if there's a single parameter of type string
				mi.setArgument(0, "");
				return mi.proceed();
			}
		};
		
		public StringSetterNullReplacementAdvice() {
			super(cleaner);
		}

		public boolean matches(Method m, Class targetClass, Object[] args){//, AttributeRegistry attributeRegistry) {
			return args[0] == null;
		}

		public boolean matches(Method m, Class targetClass){//, AttributeRegistry attributeRegistry) {
			return m.getName().startsWith("set") &&
				m.getParameterTypes().length == 1 &&
				m.getParameterTypes()[0].equals(String.class);
		}

	}
	
	
	
	public void testDynamicMethodPointcutThatAlwaysAppliesStatically() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		TestDynamicPointcutAdvice dp = new TestDynamicPointcutAdvice(new DebugInterceptor(), "getAge");
		pc.addAdvisor(dp);
		pc.addInterceptor(new InvokerInterceptor(tb));
		ITestBean it = (ITestBean) pc.getProxy();
		assertEquals(dp.count, 0);
		int age = it.getAge();
		assertEquals(dp.count, 1);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(dp.count, 2);
	}
	
	public void testDynamicMethodPointcutThatAppliesStaticallyOnlyToSetters() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		// Could apply dynamically to getAge/setAge but not to getName
		TestDynamicPointcutAdvice dp = new TestDynamicPointcutForSettersOnly(new DebugInterceptor(), "Age");
		pc.addAdvisor(dp);
		pc.addInterceptor(new InvokerInterceptor(tb));
		ITestBean it = (ITestBean) pc.getProxy();
		assertEquals(dp.count, 0);
		int age = it.getAge();
		// Statically vetoed
		assertEquals(0, dp.count);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(dp.count, 1);
		// Applies statically but not dynamically
		it.setName("joe");
		assertEquals(dp.count, 1);
	}
	
	
	public void testStaticMethodPointcut() throws Throwable {
		TestBean tb = new TestBean();
		ProxyFactory pc = new ProxyFactory(new Class[] { ITestBean.class });
		DebugInterceptor di = new DebugInterceptor();
		TestStaticPointcutAdvice sp = new TestStaticPointcutAdvice(di, "getAge");
		pc.addAdvisor(sp);
		pc.addInterceptor(new InvokerInterceptor(tb));
		ITestBean it = (ITestBean) pc.getProxy();
		assertEquals(di.getCount(), 0);
		int age = it.getAge();
		assertEquals(di.getCount(), 1);
		it.setAge(11);
		assertEquals(it.getAge(), 11);
		assertEquals(di.getCount(), 2);
	}
	
	
	private static class TestDynamicPointcutAdvice extends DynamicMethodMatcherPointcutAroundAdvisor {
		
		private String pattern;
		private int count;
		
		public TestDynamicPointcutAdvice(MethodInterceptor mi, String pattern) {
			super(mi);
			this.pattern = pattern;
		}
		/**
		 * @see org.springframework.aop.framework.DynamicMethodPointcut#applies(java.lang.reflect.Method, java.lang.Object[], org.aopalliance.AttributeRegistry)
		 */
		public boolean matches(Method m, Class targetClass, Object[] args) {
			boolean run = m.getName().indexOf(pattern) != -1;
			if (run) ++count;
			return run;
		}
	}
	
	private static class TestDynamicPointcutForSettersOnly extends TestDynamicPointcutAdvice {
		public TestDynamicPointcutForSettersOnly(MethodInterceptor mi, String pattern) {
			super(mi, pattern);
		}
		
		public boolean matches(Method m, Class clazz) {
			return m.getName().startsWith("set");
		}
	}
	
	private static class TestStaticPointcutAdvice extends StaticMethodMatcherPointcutAroundAdvisor {
		
		private String pattern;
		private int count;
	
		public TestStaticPointcutAdvice(MethodInterceptor mi, String pattern) {
			super(mi);
			this.pattern = pattern;
		}
		public boolean matches(Method m, Class targetClass) {
			boolean run = m.getName().indexOf(pattern) != -1;
			if (run) ++count;
			return run;
		}

	}


	/**
	 * Note that trapping the Invocation as in previous version of this test
	 * isn't safe, as invocations may be reused
	 * and hence cleared at the end of each invocation.
	 * So we trap only the targe.
	 */
	private static class TrapTargetInterceptor implements MethodInterceptor {

		public Object target;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			this.target = invocation.getThis();
			return invocation.proceed();
		}
	}

	private abstract static class ContextTestBean extends TestBean {

		public String getName() {
			MethodInvocation invocation = AopContext.currentInvocation();
			assertions(invocation);
			return super.getName();
		}

		public void absquatulate() {
			MethodInvocation invocation = AopContext.currentInvocation();
			assertions(invocation);
			super.absquatulate();
		}
		
		protected abstract void assertions(MethodInvocation invocation);
	}

	public static class EqualsTestBean extends TestBean {

		public ITestBean getSpouse() {
			return this;
		}
	};

}
