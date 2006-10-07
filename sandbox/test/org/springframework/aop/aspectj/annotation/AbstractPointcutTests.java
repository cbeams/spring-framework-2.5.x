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
package org.springframework.aop.aspectj.annotation;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

/**
 * @author Rod Johnson
 * @since 2.0
 */
public abstract class AbstractPointcutTests extends TestCase {

	/**
	 * 
	 */
	public AbstractPointcutTests() {
		super();
	}

	/**
	 * Return a proxy that verifies that the given
	 * pointcut always matches
	 * @param target 
	 * @param pointcutClass
	 * @param exp
	 * @param paramNames
	 * @param paramTypes
	 * @return
	 */
	protected Object getMatchingProxy(Object target, final Class pointcutClass, final String exp,
			final String[] paramNames, final Class[] paramTypes) {
		AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut(pointcutClass, paramNames, paramTypes);
		ajexp.setExpression(exp);

		ProxyFactory pf = new ProxyFactory(target);
		// pf.setTarget(target);
		// pf.addInterface(T);
		pf.addAdvice(new CountAdvice());
		final TriggerAdvice ta = new TriggerAdvice();
		pf.addAdvisor(new DefaultPointcutAdvisor(ajexp, ta));

		// Add a barrier to stop us ever getting to the target
		pf.addAdvice(new MethodInterceptor() {
			public Object invoke(MethodInvocation mi) throws Throwable {
				throw new IllegalStateException("'" + exp + "' should match " + mi.getMethod());
			}
		});

		// proxies.add(pf);

		return pf.getProxy();
	}
	
//	protected Object getNonMatchingProxy(Object target, final String exp) {
//	AspectJExpressionPointcut ajexp = new AspectJExpressionPointcut();
//	ajexp.setExpression(exp);
//
//	ProxyFactory pf = new ProxyFactory(target);
//	// pf.setTarget(target);
//	// pf.addInterface(T);
//	pf.addAdvice(new CountAdvice());
//	Advice killer = new MethodInterceptor() {
//		public Object invoke(MethodInvocation mi) throws Throwable {
//			throw new IllegalStateException("'" + exp + "' should NOT match " + mi.getMethod());
//		}
//	};
//	pf.addAdvisor(new DefaultPointcutAdvisor(ajexp, killer));
//
//	// Add a pass all
//	pf.addAdvice(new MethodInterceptor() {
//		public Object invoke(MethodInvocation mi) throws Throwable {
//			return getPrimitiveReturnIfNecessary(mi);
//		}
//
//	});
//
//	proxies.add(pf);
//
//	return pf.getProxy();
//}

	protected Object getPrimitiveReturnIfNecessary(MethodInvocation mi) {
		if (mi.getMethod().getReturnType() == int.class) {
			return 0;
		}
		return null;
	}

	private class CountAdvice implements MethodBeforeAdvice {
		public int count;

		public void before(Method method, Object[] args, Object target) throws Throwable {
			++count;
		}
	}

	private class TriggerAdvice implements MethodInterceptor {

		private Object invokedOn;

		private int invocationCount;

		public Object invoke(MethodInvocation mi) throws Throwable {
			// if (invocationCount++ > 0) {
			// throw new IllegalStateException();
			// }
			// invokedOn = mi.getThis();

			return getPrimitiveReturnIfNecessary(mi);
		}
	}
}