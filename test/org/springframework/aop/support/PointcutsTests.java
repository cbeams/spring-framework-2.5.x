/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.beans.TestBean;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @version $Id: PointcutsTests.java,v 1.2 2004-01-12 18:45:18 johnsonr Exp $
 */
public class PointcutsTests extends TestCase {
	
	static Method TEST_BEAN_SET_AGE;
	static Method TEST_BEAN_GET_AGE;
	static Method TEST_BEAN_GET_NAME;
	static Method TEST_BEAN_ABSQUATULATE;
	
	static {
		try {
			TEST_BEAN_SET_AGE = TestBean.class.getMethod("setAge", new Class[] { int.class });
			TEST_BEAN_GET_AGE = TestBean.class.getMethod("getAge", null);
			TEST_BEAN_GET_NAME = TestBean.class.getMethod("getName", null);
			TEST_BEAN_ABSQUATULATE = TestBean.class.getMethod("absquatulate", null);
		}
		catch (Exception ex) {
			throw new RuntimeException("Shouldn't happen: error in test suite");
		}
	}
	
	Pointcut allClassSetterPointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("set");
		}
	};
	
	// Subclass used for matching
	class MyTestBean extends TestBean {
	};
	
	Pointcut myTestBeanSetterPointcut = new StaticMethodMatcherPointcut() {
		public ClassFilter getClassFilter() {
			System.out.println("myTestBeanSetterPointcut");
			return new RootClassFilter(MyTestBean.class);
		}

		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("set");
		}
	};
	
	Pointcut allClassGetterPointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("get");
		}
	};
	
	Pointcut allClassGetAgePointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().equals("getAge");
		}
	};
	
	Pointcut allClassGetNamePointcut = new StaticMethodMatcherPointcut() {
		public boolean matches(Method m, Class targetClass) {
			return m.getName().equals("getName");
		}
	};
	
	
	public PointcutsTests(String s) {
		super(s);
	}

	public void testMatches() {
		assertTrue(Pointcuts.matches(allClassSetterPointcut, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(allClassSetterPointcut, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(allClassSetterPointcut, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		assertFalse(Pointcuts.matches(allClassGetterPointcut, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(allClassGetterPointcut, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(allClassGetterPointcut, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
	}
	
	/**
	 * Should match all setters and getters on any class
	 */
	public void testUnionOfSettersAndGetters() {
		Pointcut union = Pointcuts.union(allClassGetterPointcut, allClassSetterPointcut);
		assertTrue(Pointcuts.matches(union, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(union, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
	}
	
	public void testUnionOfSpecificGetters() {
		Pointcut union = Pointcuts.union(allClassGetAgePointcut, allClassGetNamePointcut);
		assertFalse(Pointcuts.matches(union, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(allClassGetAgePointcut, TEST_BEAN_GET_NAME, TestBean.class, null));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_NAME, TestBean.class, null));
		assertFalse(Pointcuts.matches(union, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		
		// Union with all setters
		union = Pointcuts.union(union, allClassSetterPointcut);
		assertTrue(Pointcuts.matches(union, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(allClassGetAgePointcut, TEST_BEAN_GET_NAME, TestBean.class, null));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_NAME, TestBean.class, null));
		assertFalse(Pointcuts.matches(union, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
		
		assertTrue(Pointcuts.matches(union, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
	}
	
	/**
	 * Tests vertical composition. First pointcut matches all setters.
	 * Second one matches all getters in the MyTestBean class. TestBean getters shouldn't pass.
	 */
	public void testUnionOfAllSettersAndSubclassSetters() {
		assertFalse(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_SET_AGE, MyTestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_GET_AGE, TestBean.class, null));
		
		Pointcut union = Pointcuts.union(myTestBeanSetterPointcut, allClassGetterPointcut);
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertTrue(Pointcuts.matches(union, TEST_BEAN_GET_AGE, MyTestBean.class, null));
		// Still doesn't match superclass setter
		assertTrue(Pointcuts.matches(union, TEST_BEAN_SET_AGE, MyTestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(union, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
	}
	
	/**
	 * Intersection should be subclass setters
	 *
	 */
	/*
	public void testIntersectionOfAllSettersAndSubclassSetters() {
		assertFalse(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertTrue(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_SET_AGE, MyTestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(myTestBeanSetterPointcut, TEST_BEAN_GET_AGE, TestBean.class, null));
		Pointcut union = Pointcuts.union(myTestBeanSetterPointcut, allClassGetterPointcut);
		assertFalse(Pointcuts.matches(union, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(union, TEST_BEAN_GET_AGE, MyTestBean.class, null));
	
		// Should
		Pointcut intersection = Pointcuts.intersection(union, allClassGetterPointcut);
		assertTrue(Pointcuts.matches(intersection, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertTrue(Pointcuts.matches(intersection, TEST_BEAN_GET_AGE, MyTestBean.class, null));
		// Still doesn't match superclass setter
		assertTrue(Pointcuts.matches(intersection, TEST_BEAN_SET_AGE, MyTestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(intersection, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
	}
	*/
	
	/**
	 * The intersection of these two pointcuts leaves nothing.
	 */
	public void testSimpleIntersection() {
		Pointcut intersection = Pointcuts.intersection(allClassGetterPointcut, allClassSetterPointcut);
		assertFalse(Pointcuts.matches(intersection, TEST_BEAN_SET_AGE, TestBean.class, new Object[] { new Integer(6)}));
		assertFalse(Pointcuts.matches(intersection, TEST_BEAN_GET_AGE, TestBean.class, null));
		assertFalse(Pointcuts.matches(intersection, TEST_BEAN_ABSQUATULATE, TestBean.class, null));
	}


}
