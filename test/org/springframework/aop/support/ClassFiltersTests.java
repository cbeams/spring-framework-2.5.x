
package org.springframework.aop.support;

import junit.framework.TestCase;

import org.springframework.aop.ClassFilter;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.core.NestedRuntimeException;

/**
 * @author Rod Johnson
 * @version $Id: ClassFiltersTests.java,v 1.3 2004-02-02 11:51:13 jhoeller Exp $
 */
public class ClassFiltersTests extends TestCase {
	
	ClassFilter exceptionFilter = new RootClassFilter(Exception.class);
	
	ClassFilter itbFilter = new RootClassFilter(ITestBean.class);
	
	ClassFilter hasRootCauseFilter = new RootClassFilter(NestedRuntimeException.class);

	/**
	 * Constructor for ClassFiltersTests.
	 * @param arg0
	 */
	public ClassFiltersTests(String arg0) {
		super(arg0);
	}
	
	public void testUnion() {
		assertTrue(exceptionFilter.matches(RuntimeException.class));
		assertFalse(exceptionFilter.matches(TestBean.class));
		assertFalse(itbFilter.matches(Exception.class));
		assertTrue(itbFilter.matches(TestBean.class));
		ClassFilter union = ClassFilters.union(exceptionFilter, itbFilter);
		assertTrue(union.matches(RuntimeException.class));
		assertTrue(union.matches(TestBean.class));
	}
	
	public void testIntersection() {
		assertTrue(exceptionFilter.matches(RuntimeException.class));
		assertTrue(hasRootCauseFilter.matches(NestedRuntimeException.class));
		
		ClassFilter intersection = ClassFilters.intersection(exceptionFilter, hasRootCauseFilter);
		assertFalse(intersection.matches(RuntimeException.class));
		assertFalse(intersection.matches(TestBean.class));
		assertTrue(intersection.matches(NestedRuntimeException.class));
	}

}
