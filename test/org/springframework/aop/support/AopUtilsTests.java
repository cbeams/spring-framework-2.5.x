/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.aop.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.aopalliance.aop.Advice;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.target.EmptyTargetSource;
import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.IOther;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.util.SerializationTestUtils;

/**
 * @author Rod Johnson
 */
public class AopUtilsTests extends TestCase {

	public void testGetAllInterfaces() {
		DerivedTestBean testBean = new DerivedTestBean();
		List ifcs = Arrays.asList(AopUtils.getAllInterfaces(testBean));
		assertEquals("Correct number of interfaces", 7, ifcs.size());
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
		assertTrue(AopUtils.canApply(new DefaultPointcutAdvisor(new NopInterceptor()), Object.class, null));
		assertTrue(AopUtils.canApply(new DefaultPointcutAdvisor(new NopInterceptor()), TestBean.class, new Class[] { ITestBean.class }));
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
	
	/**
	 * Test that when we serialize and deserialize various
	 * canonical instances of AOP classes, they return the same instance,
	 * not a new instance that's subverted the singleton construction
	 * limitation.
	 * @throws Exception
	 */
	public void testCanonicalFrameworkClassesStillCanonicalOnDeserialization() throws Exception {
		assertSame(MethodMatcher.TRUE, SerializationTestUtils.serializeAndDeserialize(MethodMatcher.TRUE));
		assertSame(ClassFilter.TRUE, SerializationTestUtils.serializeAndDeserialize(ClassFilter.TRUE));
		assertSame(Pointcut.TRUE, SerializationTestUtils.serializeAndDeserialize(Pointcut.TRUE));
		assertSame(EmptyTargetSource.INSTANCE, SerializationTestUtils.serializeAndDeserialize(EmptyTargetSource.INSTANCE));
		assertSame(Pointcuts.SETTERS, SerializationTestUtils.serializeAndDeserialize(Pointcuts.SETTERS));
		assertSame(Pointcuts.GETTERS, SerializationTestUtils.serializeAndDeserialize(Pointcuts.GETTERS));
		assertSame(ExposeInvocationInterceptor.INSTANCE, SerializationTestUtils.serializeAndDeserialize(ExposeInvocationInterceptor.INSTANCE));
	}
	

	public void testDynamicSuperclasses() {
		DynamicMethodMatcherPointcut mmpc = new DynamicMethodMatcherPointcut() {
			public boolean matches(Method m, Class targetClass, Object[] args) {
				throw new UnsupportedOperationException();
			}
		};
		assertSame(mmpc, mmpc.getMethodMatcher());
		assertSame(ClassFilter.TRUE, mmpc.getClassFilter());
		
		DynamicMethodMatcherPointcutAdvisor a = new DynamicMethodMatcherPointcutAdvisor() {
			public boolean matches(Method m, Class targetClass, Object[] args) {
				throw new UnsupportedOperationException();
			}
		};
		Advice advice = new NopInterceptor();
		a.setAdvice(advice);
		assertSame(a, a.getMethodMatcher());
		assertSame(ClassFilter.TRUE, a.getClassFilter());
		assertSame(a, a.getPointcut());
		assertSame(advice, a.getAdvice());
	}

}
