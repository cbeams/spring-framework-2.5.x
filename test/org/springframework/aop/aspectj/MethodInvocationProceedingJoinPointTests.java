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
			ExposeJoinPointInterceptor.currentJoinPoint();
			fail("Needs to be bound by interceptor action");
		}
		catch (AspectException ex) {
		}
	}
	
	public void testingBindingWithProceedingJoinPoint() {
		try {
			ExposeJoinPointInterceptor.currentProceedingJoinPoint();
			fail("Needs to be bound by interceptor action");
		}
		catch (AspectException ex) {
		}
	}
	
	public void testCanGetMethodSignatureFromJoinPoint() {
		final Object raw = new TestBean();
		ProxyFactory pf = new ProxyFactory(raw);
		pf.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
		pf.addAdvisor(ExposeJoinPointInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				assertSame(target, ExposeJoinPointInterceptor.currentProceedingJoinPoint().getTarget());
				assertSame(target, ExposeJoinPointInterceptor.currentProceedingJoinPoint().getThis());
				assertSame(target, raw);
				
				assertSame(method.getName(), ExposeJoinPointInterceptor.currentProceedingJoinPoint().getSignature().getName());
				assertEquals(method.getModifiers(), ExposeJoinPointInterceptor.currentProceedingJoinPoint().getSignature().getModifiers());
				
				MethodSignature msig = (MethodSignature) ExposeJoinPointInterceptor.currentJoinPoint().getSignature();
				assertSame("Return same MethodSignature repeatedly", msig, ExposeJoinPointInterceptor.currentJoinPoint().getSignature());
				assertSame("Return same JoinPoint repeatedly", ExposeJoinPointInterceptor.currentJoinPoint(), ExposeJoinPointInterceptor.currentJoinPoint());
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
		pf.addAdvisor(ExposeJoinPointInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				SourceLocation sloc = ExposeJoinPointInterceptor.currentJoinPoint().getSourceLocation();
				assertEquals("Same source location must be returned on subsequent requests",  sloc, ExposeJoinPointInterceptor.currentJoinPoint().getSourceLocation());
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
		pf.addAdvisor(ExposeJoinPointInterceptor.ADVISOR);
		pf.addAdvice(new MethodBeforeAdvice() {
			public void before(Method method, Object[] args, Object target) throws Throwable {
				StaticPart staticPart = ExposeJoinPointInterceptor.currentJoinPoint().getStaticPart();
				assertEquals("Same static part must be returned on subsequent requests",  staticPart, ExposeJoinPointInterceptor.currentJoinPoint().getStaticPart());
				assertEquals(ProceedingJoinPoint.METHOD_EXECUTION, staticPart.getKind());
				assertSame(ExposeJoinPointInterceptor.currentJoinPoint().getSignature(), staticPart.getSignature());
				assertEquals(ExposeJoinPointInterceptor.currentJoinPoint().getSourceLocation(), staticPart.getSourceLocation());
			}
		});
		ITestBean itb = (ITestBean) pf.getProxy();
		// Any call will do
		itb.getAge();
	}

}
