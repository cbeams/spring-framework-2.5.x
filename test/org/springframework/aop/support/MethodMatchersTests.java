package org.springframework.aop.support;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.aop.MethodMatcher;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * $Id: MethodMatchersTests.java,v 1.1 2003-11-16 12:54:57 johnsonr Exp $
 */
public class MethodMatchersTests extends TestCase {

	final Method EXCEPTION_GETMESSAGE;

	final Method ITESTBEAN_SETAGE;

	/**
	 * Constructor for DefaultMethodMatcherTests.
	 * @param arg0
	 */
	public MethodMatchersTests(String arg0) throws Exception {
		super(arg0);
		EXCEPTION_GETMESSAGE = Exception.class.getMethod("getMessage", null);
		ITESTBEAN_SETAGE = ITestBean.class.getMethod("setAge", new Class[] { int.class });
	}

	public void testDefaultMatchesAll() throws Exception {
		MethodMatcher defaultMm = MethodMatcher.TRUE;
		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertTrue(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
	}

	public void testSingle() throws Exception {
		MethodMatcher defaultMm = MethodMatcher.TRUE;
		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertTrue(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
		defaultMm = MethodMatchers.intersection(defaultMm, new StaticMethodMatcher() {
			public boolean matches(Method m, Class targetClass) {
				return m.getName().startsWith("get");
			}
		});

		assertTrue(defaultMm.matches(EXCEPTION_GETMESSAGE, Exception.class));
		assertFalse(defaultMm.matches(ITESTBEAN_SETAGE, TestBean.class));
	}

	
	public void testDynamicAndStaticMethodMatcherIntersection() throws Exception {
		MethodMatcher mm1 = MethodMatcher.TRUE;
		MethodMatcher mm2 = new TestDynamicMethodMatcherWhichMatches();
		MethodMatcher intersection = MethodMatchers.intersection(mm1, mm2);
		boolean matches = intersection.matches(ITESTBEAN_SETAGE, TestBean.class);
		assertTrue("Intersection is a dynamic matcher", intersection.isRuntime());
		assertTrue("Matched setAge method", matches);

	}

	private static class TestDynamicMethodMatcherWhichMatches extends DynamicMethodMatcher {
		public boolean matches(Method m, Class targetClass, Object[] args) {
			return true;
		}
	}

	private static class TestDynamicMethodMatcherWhichDoesNotMatch extends DynamicMethodMatcher {
		public boolean matches(Method m, Class targetClass, Object[] args) {
			return false;
		}
	}

}
