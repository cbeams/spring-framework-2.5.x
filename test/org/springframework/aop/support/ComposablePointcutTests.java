
package org.springframework.aop.support;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.beans.TestBean;
import org.springframework.core.NestedRuntimeException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: ComposablePointcutTests.java,v 1.4 2004-02-02 11:51:13 jhoeller Exp $
 */
public class ComposablePointcutTests extends TestCase {
	
	public static MethodMatcher GETTER_METHOD_MATCHER = new StaticMethodMatcher() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("get");
		}
	};
	
	public static MethodMatcher GET_AGE_METHOD_MATCHER = new StaticMethodMatcher() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().equals("getAge");
		}
	};
	
	public static MethodMatcher ABSQUATULATE_METHOD_MATCHER = new StaticMethodMatcher() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().equals("absquatulate");
		}
	};
	
	public static MethodMatcher SETTER_METHOD_MATCHER = new StaticMethodMatcher() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("set");
		}
	};
	
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
		pc.intersection(new RootClassFilter(NestedRuntimeException.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(NestedRuntimeException.class));
		assertFalse(pc.getClassFilter().matches(String.class));
		pc.union(new RootClassFilter(String.class));
		assertFalse(pc.getClassFilter().matches(Exception.class));
		assertTrue(pc.getClassFilter().matches(String.class));
		assertTrue(pc.getClassFilter().matches(NestedRuntimeException.class));
	}

	public void testUnionMethodMatcher() {
		// Matches the getAge() method in any class
		ComposablePointcut pc = new ComposablePointcut(ClassFilter.TRUE, GET_AGE_METHOD_MATCHER);
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class, null));
		
		pc.union(GETTER_METHOD_MATCHER);
		// Should now match all getter methods
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class, null));
		
		pc.union(ABSQUATULATE_METHOD_MATCHER);
		// Should now match absquatulate() as well 
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class, null));
		// But it doesn't match everything
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_SET_AGE, TestBean.class, null));
	}

	public void testIntersectionMethodMatcher() {
		ComposablePointcut pc = new ComposablePointcut();
		assertTrue(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class));
		assertTrue(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class));
		assertTrue(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class));
		pc.intersection(GETTER_METHOD_MATCHER);
		assertFalse(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class));
		assertTrue(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class));
		assertTrue(pc.getMethodMatcher().matches(PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class));
		pc.intersection(GET_AGE_METHOD_MATCHER);
		// Use the Pointcuts matches method
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		assertTrue(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(pc, PointcutsTests.TEST_BEAN_GET_NAME, TestBean.class, null));
	}

}
