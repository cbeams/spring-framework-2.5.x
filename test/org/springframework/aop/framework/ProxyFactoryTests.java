/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop.framework;

import junit.framework.TestCase;

import org.aopalliance.intercept.Interceptor;
import org.springframework.aop.interceptor.AbstractQaInterceptor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.SimpleIntroductionAdvice;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.core.TimeStamped;
import org.springframework.util.StringUtils;

/**
 * Also tests AdvisedSupport superclass.
 * @author Rod Johnson
 * @since 14-Mar-2003
 * @version $Id: ProxyFactoryTests.java,v 1.6 2003-11-30 17:17:33 johnsonr Exp $
 */
public class ProxyFactoryTests extends TestCase {

	/**
	 * Constructor for ProxyFactoryTests.
	 * @param arg0
	 */
	public ProxyFactoryTests(String arg0) {
		super(arg0);
	}

	public void testNullTarget() {

		try {
			// Use the constructor taking Object
			new ProxyFactory((Object) null);
			fail("Should't allow proxy with null target");
		} catch (AopConfigException ex) {
		}
	}

	public static class Concrete {
		public void foo() {
		}
	}

	public void testAddRepeatedInterface() {
		TimeStamped tst = new TimeStamped() {
			public long getTimeStamp() {
				throw new UnsupportedOperationException("getTimeStamp");
			}
		};
		ProxyFactory pf = new ProxyFactory(tst);
		// We've already implicitly added this interface.
		// This call should be ignored without error
		pf.addInterface(TimeStamped.class);
		// All cool
		TimeStamped ts = (TimeStamped) pf.getProxy();
	}

	public void testGetsAllInterfaces() throws Exception {
		// Extend to get new interface
		class TestBeanSubclass extends TestBean implements Comparable {
			public int compareTo(Object arg0) {
				throw new UnsupportedOperationException("compareTo");
			}
		};
		TestBeanSubclass raw = new TestBeanSubclass();
		ProxyFactory factory = new ProxyFactory(raw);
		assertTrue("Found 3 interfaces", factory.getProxiedInterfaces().length == 3);
		System.out.println("Proxied interfaces are " + StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), ","));
		ITestBean tb = (ITestBean) factory.getProxy();
		assertTrue("Picked up secondary interface", tb instanceof IOther);
				
		raw.setAge(25);
		assertTrue(tb.getAge() == raw.getAge());

		long t = 555555L;
		TimestampIntroductionInterceptor ti = new TimestampIntroductionInterceptor(t);
		
		System.out.println(StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), "/"));
		
		factory.addAdvisor(0, new SimpleIntroductionAdvice(ti, TimeStamped.class));
		
		System.out.println(StringUtils.arrayToDelimitedString(factory.getProxiedInterfaces(), "/"));
		

		TimeStamped ts = (TimeStamped) factory.getProxy();
		assertTrue(ts.getTimeStamp() == t);
		// Shouldn't fail;
		 ((IOther) ts).absquatulate();
	}
	
	public void testCanOnlyAddMethodInterceptors() {
		ProxyFactory factory = new ProxyFactory(new TestBean());
		factory.addInterceptor(0, new NopInterceptor());
		try {
			factory.addInterceptor(0, new Interceptor() {
			});
			fail("Should only be able to add MethodInterceptors");
		}
		catch (AopConfigException ex) {
		}
		
		// Check we can still use it
		IOther other = (IOther) factory.getProxy();
		other.absquatulate();
	}
	
	public void testInterceptorInclusionMethods() {
		NopInterceptor di = new NopInterceptor();
		NopInterceptor diUnused = new NopInterceptor();
		ProxyFactory factory = new ProxyFactory(new TestBean());
		factory.addInterceptor(0, di);
		ITestBean tb = (ITestBean) factory.getProxy();
		assertTrue(factory.interceptorIncluded(di));
		assertTrue(!factory.interceptorIncluded(diUnused));
		assertTrue(factory.countInterceptorsOfType(NopInterceptor.class) == 1);
		assertTrue(factory.countInterceptorsOfType(AbstractQaInterceptor.class) == 0);
	
		factory.addInterceptor(0, diUnused);
		assertTrue(factory.interceptorIncluded(diUnused));
		assertTrue(factory.countInterceptorsOfType(NopInterceptor.class) == 2);
	}

}
