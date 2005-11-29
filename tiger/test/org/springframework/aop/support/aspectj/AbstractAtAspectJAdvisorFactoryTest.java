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

package org.springframework.aop.support.aspectj;

import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * Abstract tests for AspectJAdvisorFactory. See subclasses
 * for tests of concrete factories.
 * 
 * TODO introductions are broken due to Alex's changes.
 * Still processing the old way
 * 
 * @author Rod Johnson
 *
 */
public abstract class AbstractAtAspectJAdvisorFactoryTest extends TestCase {
	
	protected abstract AtAspectJAdvisorFactory getFixture();
	
	@Aspect
	public static class NamedPointcutAspectWithFQN {
		@Pointcut("execution(* getAge())")
		public void getAge() {			
		}
		
		@Around("org.springframework.aop.support.aspectj.AbstractAtAspectJAdvisorFactoryTest.NamedPointcutAspectWithFQN.getAge()")
		public int changeReturnType(ProceedingJoinPoint pjp) {
			return -1;
		}
	}
	
	public void testNamedPointcutAspectWithFQN() {
		testNamedPointcuts(new NamedPointcutAspectWithFQN());
	}
	
	@Aspect
	public static class NamedPointcutAspectWithoutFQN {
		@Pointcut("execution(* getAge())")
		public void getAge() {			
		}
		
		@Around("getAge()")
		public int changeReturnType(ProceedingJoinPoint pjp) {
			return -1;
		}
	}
	
	// TODO fix me
//	public void testNamedPointcutAspectWithoutFQN() {
//		testNamedPointcuts(new NamedPointcutAspectWithoutFQN());
//	}
	
	@Aspect
	public static class NamedPointcutAspectFromLibrary {

		@Around("org.springframework.aop.aspectj.support.CommonPointcuts.anyGetter()")
		public int changeReturnType(ProceedingJoinPoint pjp) {
			return -1;
		}
		
		@Around(value="org.springframework.aop.aspectj.support.CommonPointcuts.anyIntArg(x)", argNames="x")
		public void doubleArg(ProceedingJoinPoint pjp, int x) throws Throwable {
			pjp.proceed(new Object[]{x*2});
		}
	}

	// TODO fix me fails can't bind type name
	public void xtestNamedPointcutFromAspectLibrary() {
		testNamedPointcuts(new NamedPointcutAspectFromLibrary());
	}
	
	
	@Aspect
	public static class NamedPointcutAspectFromLibraryWithBinding {
		
		@Around(value="org.springframework.aop.aspectj.support.CommonPointcuts.anyIntArg(x)", argNames="x")
		public void doubleArg(ProceedingJoinPoint pjp, int x) throws Throwable {
			pjp.proceed(new Object[]{x*2});
		}
	}
	
//	 TODO fix me fails can't bind type name
	public void xtestNamedPointcutFromAspectLibraryWithBinding() {
		TestBean target = new TestBean();
		ITestBean itb = (ITestBean) createProxy(target, 
				getFixture().getAdvisors(new NamedPointcutAspectFromLibraryWithBinding()), 
				ITestBean.class);
		itb.setAge(10);
		assertEquals("Around advice must apply", 20, itb.getAge());
		assertEquals(20,target.getAge());
	}
	
	private void testNamedPointcuts(Object aspectInstance) {
		TestBean target = new TestBean();
		int realAge = 65;
		target.setAge(realAge);
		ITestBean itb = (ITestBean) createProxy(target, 
				getFixture().getAdvisors(aspectInstance), 
				ITestBean.class);
		assertEquals("Around advice must apply", -1, itb.getAge());
		assertEquals(realAge, target.getAge());
	}
	
	@Aspect
	public static class BindingAspectWithSingleArg {
		
		@Pointcut(value="args(a)", argNames="a")
		public void setAge(int a) {}
		
		@Around(value="setAge(age)",argNames="age")
//		@ArgNames({"age"})   // AMC needs more work here? ignoring pjp arg... ok??
//		                       // argNames should be suported in Around as it is in Pointcut
		public void changeReturnType(ProceedingJoinPoint pjp, int age) throws Throwable {
			pjp.proceed(new Object[]{age*2});
		}
	}

	// TODO fix me, breaks unable to find referenced pointcut
//	public void testBindingWithSingleArg() {
//		TestBean target = new TestBean();
//		ITestBean itb = (ITestBean) createProxy(target, 
//				getFixture().getAdvisors(new BindingAspectWithSingleArg()), 
//				ITestBean.class);
//		itb.setAge(10);
//		assertEquals("Around advice must apply", 20, itb.getAge());
//		assertEquals(20,target.getAge());
//	}
	
	
	@Aspect
	public static class ManyValuedArgs {
		public String mungeArgs(String a, int b, int c, String d, StringBuffer e) {
			return a + b + c + d + e;
		}
		
		@Around(value="execution(String mungeArgs(..)) && args(a, b, c, d, e)",
				argNames="b,c,d,e,a")
		public String reverseAdvice(ProceedingJoinPoint pjp, int b, int c, String d, StringBuffer e, String a) throws Throwable {
			assertEquals(a + b+ c+ d+ e, pjp.proceed());
			return a + b + c + d + e;
		}
	}
	
	public void testBindingWithMultipleArgsDifferentlyOrdered() {
		ManyValuedArgs target = new ManyValuedArgs();
		ManyValuedArgs mva = (ManyValuedArgs) createProxy(target, 
				getFixture().getAdvisors(new ManyValuedArgs()), 
				ManyValuedArgs.class);
		
		String a = "a";
		int b = 12;
		int c = 25;
		String d = "d";
		StringBuffer e = new StringBuffer("stringbuf");
		String expectedResult = a + b+ c + d + e;
		assertEquals(expectedResult, mva.mungeArgs(a, b, c, d, e));
	}
	
	public void testIntroductionOnTargetNotImplementingInterface() {
		NotLockable target = new NotLockable();
		NotLockable proxy = (NotLockable) createProxy(target,
				getFixture().getAdvisors(new MakeLockable()),
				NotLockable.class);
		System.out.println(((Advised) proxy).toProxyConfigString());
		assertTrue(proxy instanceof Lockable);
		Lockable lockable = (Lockable) proxy;
		assertFalse(lockable.isLocked());
		lockable.lock();
		assertTrue(lockable.isLocked());
	}
	
	public void testIntroductionAdvisorExcludedFromTargetImplementingInterface() {
		assertTrue(AopUtils.findAdvisorsThatCanApply(getFixture().getAdvisors(new MakeLockable()), CannotBeUnlocked.class).isEmpty());
		assertEquals(1, AopUtils.findAdvisorsThatCanApply(getFixture().getAdvisors(new MakeLockable()), NotLockable.class).size());
	}
	
	public void testIntroductionOnTargetImplementingInterface() {
		CannotBeUnlocked target = new CannotBeUnlocked();
		Lockable proxy = (Lockable) createProxy(target,
				// Ensure that we exclude
				AopUtils.findAdvisorsThatCanApply(
						getFixture().getAdvisors(new MakeLockable()),
						CannotBeUnlocked.class
				),
				CannotBeUnlocked.class);
		assertTrue(proxy instanceof Lockable);
		Lockable lockable = (Lockable) proxy;
		assertTrue("Already locked", lockable.isLocked());
		lockable.lock();
		assertTrue("Real target ignores locking", lockable.isLocked());
		try {
			lockable.unlock();
			fail();
		}
		catch (UnsupportedOperationException ex) {
			// Ok
		}
	}
	
	
	@Aspect
	public static class ExceptionAspect {
		private final Exception ex;
		
		public ExceptionAspect(Exception ex) {
			this.ex = ex;
		}
		
		@Before("execution(* getAge())")		
		public void throwException() throws Exception {
			throw ex;
		}
	}
		
	public void testAspectMethodThrowsExceptionLegalOnSignature() {
		TestBean target = new TestBean();
		UnsupportedOperationException expectedException = new UnsupportedOperationException();
		List<Advisor> advisors = getFixture().getAdvisors(new ExceptionAspect(expectedException));
		assertEquals("One advice method was found", 1, advisors.size());
		ITestBean itb = (ITestBean) createProxy(target, 
				advisors, 
				ITestBean.class);
		try {
			itb.getAge();
			fail();
		}
		catch (UnsupportedOperationException ex) {
			assertSame(expectedException, ex);
		}
	}
	
	// TODO document this behaviour.
	// Is it different AspectJ behaviour, at least for checked exceptions?
	public void testAspectMethodThrowsExceptionIllegalOnSignature() {
		TestBean target = new TestBean();
		RemoteException expectedException = new RemoteException();
		List<Advisor> advisors = getFixture().getAdvisors(new ExceptionAspect(expectedException));
		assertEquals("One advice method was found", 1, advisors.size());
		ITestBean itb = (ITestBean) createProxy(target, 
				advisors, 
				ITestBean.class);
		try {
			itb.getAge();
			fail();
		}
		catch (UndeclaredThrowableException ex) {
			assertSame(expectedException, ex.getCause());
		}
	}
	
	protected Object createProxy(Object target, List<Advisor> advisors, Class ... interfaces) {
		ProxyFactory pf = new ProxyFactory(target);
		if (interfaces.length > 1 || interfaces[0].isInterface()) {
			pf.setInterfaces(interfaces);
		}
		else {
			pf.setProxyTargetClass(true);
		}
		
		// TODO this has to go in everywhere we use AspectJ proxies
		pf.addAdvice(ExposeInvocationInterceptor.INSTANCE);
		
		for (Advisor a : advisors) {
			pf.addAdvisor(a);
		}
		return pf.getProxy();
	}
	
	@Aspect
	public static class TwoAdviceAspect {
		private int totalCalls;
		
		@Around("execution(* getAge())")		
		public int returnCallCount(ProceedingJoinPoint pjp) throws Exception {
			return totalCalls;
		}
		
		@Before("execution(* setAge(int))")		
		public void countSet(int newAge) throws Exception {
			++totalCalls;
		}
		
	}
		
	public void testTwoAdvicesOnOneAspect() {
		TestBean target = new TestBean();
	
		TwoAdviceAspect twoAdviceAspect = new TwoAdviceAspect();
		List<Advisor> advisors = getFixture().getAdvisors(twoAdviceAspect);
		assertEquals("Two advice methods found", 2, advisors.size());
		ITestBean itb = (ITestBean) createProxy(target, 
				advisors, 
				ITestBean.class);
		itb.setName("");
		assertEquals(0, itb.getAge());
		int newAge = 32;
		itb.setAge(newAge);
		assertEquals(1, itb.getAge());
	}

	
	public static class Echo {
		public Object echo(Object o) throws Exception {
			if (o instanceof Exception) {
				throw (Exception) o;
			}
			return o;
		}
	}
	
	@Aspect
	public static class ExceptionHandling {
		public int successCount;
		public int failureCount;
		public int afterCount;
		
		@AfterReturning("execution(* echo(*))")
		public void succeeded() {
			++successCount;
		}
		
		@AfterThrowing("execution(* echo(*))")
		public void failed() {
			++failureCount;
		}
		
		@After("execution(* echo(*))")
		public void invoked() {
			++afterCount;
		}
	}
	
	public void testAfterAdviceTypes() throws Exception {
		Echo target = new Echo();
	
		ExceptionHandling afterReturningAspect = new ExceptionHandling();
		List<Advisor> advisors = getFixture().getAdvisors(afterReturningAspect);
		Echo echo = (Echo) createProxy(target, 
				advisors, 
				Echo.class);
		assertEquals(0, afterReturningAspect.successCount);
		assertEquals("", echo.echo(""));
		assertEquals(1, afterReturningAspect.successCount);
		assertEquals(0, afterReturningAspect.failureCount);
		try {
			echo.echo(new ServletException());
			fail();
		}
		catch (ServletException ex) {
			// Ok
		}
		catch (Exception ex) {
			fail();
		}
		assertEquals(1, afterReturningAspect.successCount);
		assertEquals(1, afterReturningAspect.failureCount);
		assertEquals(afterReturningAspect.failureCount + afterReturningAspect.successCount, afterReturningAspect.afterCount);
	}

}
