package org.springframework.aop.framework.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.aop.Pointcut;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.DefaultInterceptionAroundAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @version $Id: AbstractVetoableChangeListener.java,v 1.1.1.1 2003/08/14 16:20:14 trisberg
 * @author Rod Johnson
 */
public class AopUtilsTests extends TestCase {

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(AopUtils.getAllInterfaces(testBean));
		assertEquals("Correct number of interfaces", 4, ifcs.size());
		assertTrue("Contains Serializable", ifcs.contains(Serializable.class));
		assertTrue("Contains ITestBean", ifcs.contains(ITestBean.class));
		assertTrue("Contains IOther", ifcs.contains(IOther.class));
	}
	
	
	public void testIsMethodDeclaredOnOneOfTheseInterfaces() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, null));
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { ITestBean.class }));
		m = TestBean.class.getMethod("getName", null);
		assertTrue(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { ITestBean.class }));
		assertTrue(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { Comparable.class, ITestBean.class }));
		assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { Comparable.class }));
	}
	
	/*
	public void testIsMethodDeclaredOnOneOfTheseInterfacesWithSameNameMethodNotFromInterface() throws Exception {
		class Unconnected {
			public String getName() { throw new UnsupportedOperationException(); }
		}
		Method m = Unconnected.class.getMethod("getName", null);
		// TODO What do do?
	}
	*/
	
	public void testIsMethodDeclaredOnOneOfTheseInterfacesRequiresInterfaceArguments() throws Exception {
		Method m = Object.class.getMethod("hashCode", null);
		try {
			assertFalse(AopUtils.methodIsOnOneOfTheseInterfaces(m, new Class[] { TestBean.class }));
			fail();
		}
		catch (IllegalArgumentException ex) {
			// Ok
		}
	}
	
	
	public void testPointcutCanNeverApply() {
		class TestPointcut extends StaticMethodMatcherPointcut {
			public boolean matches(Method method, Class clazzy) {
				return false;
			}
		}
	
		Pointcut no = new TestPointcut();
		assertFalse(AopUtils.canApply(no, Object.class, null));
	}

	public void testPointcutAlwaysApplies() {
		assertTrue(AopUtils.canApply(new DefaultInterceptionAroundAdvisor(new NopInterceptor()), Object.class, null));
		assertTrue(AopUtils.canApply(new DefaultInterceptionAroundAdvisor(new NopInterceptor()), TestBean.class, new Class[] { ITestBean.class }));
	}

	public void testPointcutAppliesToOneMethodOnObject() {
		class TestPointcut extends StaticMethodMatcherPointcut {
			public boolean matches(Method method, Class clazz) {
				return method.getName().equals("hashCode");
			}
		}

		Pointcut pc = new TestPointcut();
	
		// Will return true if we're not proxying interfaces
		assertTrue(AopUtils.canApply(pc, Object.class, null));
	
		// Will return false if we're proxying interfaces
		assertFalse(AopUtils.canApply(pc, Object.class, new Class[] { ITestBean.class }));
	}

	public void testPointcutAppliesToOneInterfaceOfSeveral() {
		class TestPointcut extends StaticMethodMatcherPointcut {
			public boolean matches(Method method, Class clazz) {
				return method.getName().equals("getName");
			}
		}

		Pointcut pc = new TestPointcut();

		// Will return true if we're proxying interfaces including ITestBean 
		assertTrue(AopUtils.canApply(pc, TestBean.class, new Class[] { ITestBean.class, Comparable.class }));
	
		// Will return true if we're proxying interfaces including ITestBean 
		assertFalse(AopUtils.canApply(pc, TestBean.class, new Class[] { Comparable.class }));
	}

}
