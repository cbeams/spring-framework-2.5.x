/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.aspectj;

import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.aopalliance.aop.AspectException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * 
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class MethodInvocationProceedingJoinPointTests extends TestCase {
	
	public void testingBindingWithJoinPoint() {
		try {
			AbstractAspectJAdvice.currentJoinPoint();
			fail("Needs to be bound by interceptor action");
		}
		catch (AspectException ex) {
		}
	}
	
	public void testingBindingWithProceedingJoinPoint() {
		try {
			AbstractAspectJAdvice.currentJoinPoint();
			fail("Needs to be bound by interceptor action");
		}
		catch (AspectException ex) {
		}
	}
	
	public void testCanGetMethodSignatureFromJoinPoint() {
		final Object raw = new TestBean();
		ProxyFactory pf = new ProxyFactory(raw);
		pf.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				assertSame(target, AbstractAspectJAdvice.currentJoinPoint().getTarget());
				assertSame(target, AbstractAspectJAdvice.currentJoinPoint().getThis());
				assertSame(target, raw);
				
				assertSame(method.getName(), AbstractAspectJAdvice.currentJoinPoint().getSignature().getName());
				assertEquals(method.getModifiers(), AbstractAspectJAdvice.currentJoinPoint().getSignature().getModifiers());
				
				MethodSignature msig = (MethodSignature) AbstractAspectJAdvice.currentJoinPoint().getSignature();
				assertSame("Return same MethodSignature repeatedly", msig, AbstractAspectJAdvice.currentJoinPoint().getSignature());
				assertSame("Return same JoinPoint repeatedly", AbstractAspectJAdvice.currentJoinPoint(), AbstractAspectJAdvice.currentJoinPoint());
				assertEquals(method.getDeclaringClass(), msig.getDeclaringType());
				assertTrue(Arrays.equals(method.getParameterTypes(), msig.getParameterTypes()));
				assertEquals(method.getReturnType(), msig.getReturnType());
				assertTrue(Arrays.equals(method.getExceptionTypes(), msig.getExceptionTypes()));
				try {
					msig.getParameterNames();
					fail("Can't determine parameter names");
				}
				catch (UnsupportedOperationException ex) {
					// Expected
				}
				msig.toLongString();
				msig.toShortString();
			}
		});
		ITestBean itb = (ITestBean) pf.getProxy();
		// Any call will do
		itb.getAge();
	}
	
	public void testCanGetSourceLocationFromJoinPoint() {
		final Object raw = new TestBean();
		ProxyFactory pf = new ProxyFactory(raw);
		pf.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				SourceLocation sloc = AbstractAspectJAdvice.currentJoinPoint().getSourceLocation();
				assertEquals("Same source location must be returned on subsequent requests",  sloc, AbstractAspectJAdvice.currentJoinPoint().getSourceLocation());
				assertEquals(TestBean.class, sloc.getWithinType());
				try {
					sloc.getLine();
					fail("Can't get line number");
				}
				catch (UnsupportedOperationException ex) {
					// Expected
				}
				
				try {
					sloc.getFileName();
					fail("Can't get file name");
				}
				catch (UnsupportedOperationException ex) {
					// Expected
				}
			}
		});
		ITestBean itb = (ITestBean) pf.getProxy();
		// Any call will do
		itb.getAge();
	}
	
	public void testCanGetStaticPartFromJoinPoint() {
		final Object raw = new TestBean();
		ProxyFactory pf = new ProxyFactory(raw);
		pf.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				StaticPart staticPart = AbstractAspectJAdvice.currentJoinPoint().getStaticPart();
				assertEquals("Same static part must be returned on subsequent requests",  staticPart, AbstractAspectJAdvice.currentJoinPoint().getStaticPart());
				assertEquals(ProceedingJoinPoint.METHOD_EXECUTION, staticPart.getKind());
				assertSame(AbstractAspectJAdvice.currentJoinPoint().getSignature(), staticPart.getSignature());
				assertEquals(AbstractAspectJAdvice.currentJoinPoint().getSourceLocation(), staticPart.getSourceLocation());
			}
		});
		ITestBean itb = (ITestBean) pf.getProxy();
		// Any call will do
		itb.getAge();
	}

}
