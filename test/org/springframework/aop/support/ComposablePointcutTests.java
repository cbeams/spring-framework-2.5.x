
package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.core.HasRootCause;
import org.springframework.core.NestedCheckedException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ComposablePointcutTests.java,v 1.2 2004-01-12 18:45:18 johnsonr Exp $
 */
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
	

	public void testFilterByClass() throws NoSuchMethodException {
		ComposablePointcut pc = new ComposablePointcut();
	
		assertTrue(pc.getClassFilter().matches(Object.class));
		
		ClassFilter cf = new RootClassFilter(Exception.class);
		pc.intersection(cf);
		assertFalse(pc.getClassFilter().matches(Object.class));
		assertTrue(pc.getClassFilter().matches(Exception.class));
		pc.intersection(new RootClassFilter(HasRootCause.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(NestedCheckedException.class));
		assertFalse(pc.getClassFilter().matches(String.class));
		pc.union(new RootClassFilter(String.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(String.class));
		assertTrue(pc.getClassFilter().matches(NestedCheckedException.class));
	}


}
