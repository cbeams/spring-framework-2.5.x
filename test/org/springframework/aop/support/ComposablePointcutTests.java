
package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.Pointcut;


public class ComposablePointcutTests extends TestCase {
	

	/**
	 * Constructor for PointcutSupportTests.
	 * @param arg0
	 */
	public ComposablePointcutTests(String arg0) throws Exception {
		super(arg0);
	}
	
	public void testMatchAll() throws NoSuchMethodException {
		Pointcut pc = new ComposablePointcut();
		
		assertTrue(pc.getClassFilter().matches(Object.class));
		assertTrue(pc.getMethodMatcher().matches(Object.class.getMethod("hashCode", null), Exception.class));
	}
	
	// TODO fix this
	/*
	public void testFilterByClass() throws NoSuchMethodException {
		ComposablePointcut pc = new ComposablePointcut();
	
		assertTrue(pc.getClassFilter().matches(Object.class));
		
		ClassFilter cf = new RootClassFilter(Exception.class);
		pc.union(cf);
		assertFalse(pc.getClassFilter().matches(Object.class));
		assertTrue(pc.getClassFilter().matches(Exception.class));
		cf = ClassFilters.intersection(cf, new RootClassFilter(HasRootCause.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(NestedCheckedException.class));
		assertFalse(pc.getClassFilter().matches(String.class));
		cf = ClassFilters.union(cf, new RootClassFilter(String.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(String.class));
	}
	*/

}
