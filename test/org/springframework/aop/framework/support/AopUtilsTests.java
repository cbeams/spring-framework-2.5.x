package org.springframework.aop.framework.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.intercept.AttributeRegistry;
import org.springframework.aop.framework.AbstractMethodPointcut;
import org.springframework.aop.framework.AlwaysInvoked;
import org.springframework.aop.framework.StaticMethodPointcut;
import org.springframework.aop.interceptor.DebugInterceptor;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * @author Juergen Hoeller
 * @since 29.09.2003
 */
public class AopUtilsTests extends TestCase {

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(AopUtils.getAllInterfaces(testBean));
		assertTrue("Correct number of interfaces", ifcs.size() == 3);
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
		class TestPointcut extends AbstractMethodPointcut implements StaticMethodPointcut {
			public boolean applies(Method method, AttributeRegistry attributeRegistry) {
				return false;
			}
		}
	
		StaticMethodPointcut no = new TestPointcut();
		assertFalse(AopUtils.canApply(no, null, Object.class, null));
	}

	public void testPointcutAlwaysApplies() {
		assertTrue(AopUtils.canApply(new AlwaysInvoked(new DebugInterceptor()), null, Object.class, null));
		assertTrue(AopUtils.canApply(new AlwaysInvoked(new DebugInterceptor()), null, TestBean.class, new Class[] { ITestBean.class }));
	}

	public void testPointcutAppliesToOneMethodOnObject() {
		class TestPointcut extends AbstractMethodPointcut implements StaticMethodPointcut {
			public boolean applies(Method method, AttributeRegistry attributeRegistry) {
				return method.getName().equals("hashCode");
			}
		}

		StaticMethodPointcut pc = new TestPointcut();
	
		// Will return true if we're not proxying interfaces
		assertTrue(AopUtils.canApply(pc, null, Object.class, null));
	
		// Will return false if we're proxying interfaces
		assertFalse(AopUtils.canApply(pc, null, Object.class, new Class[] { ITestBean.class }));
	}

	public void testPointcutAppliesToOneInterfaceOfSeveral() {
		class TestPointcut extends AbstractMethodPointcut implements StaticMethodPointcut {
			public boolean applies(Method method, AttributeRegistry attributeRegistry) {
				return method.getName().equals("getName");
			}
		}

		StaticMethodPointcut pc = new TestPointcut();

		// Will return true if we're proxying interfaces including ITestBean 
		assertTrue(AopUtils.canApply(pc, null, TestBean.class, new Class[] { ITestBean.class, Comparable.class }));
	
		// Will return true if we're proxying interfaces including ITestBean 
		assertFalse(AopUtils.canApply(pc, null, TestBean.class, new Class[] { Comparable.class }));
	}

}
