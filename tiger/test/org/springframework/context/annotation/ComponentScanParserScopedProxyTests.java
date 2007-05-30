/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.context.annotation;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Mark Fisher
 */
public class ComponentScanParserScopedProxyTests extends TestCase {

	public void testDefaultScopedProxy() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/scopedProxyDefaultTests.xml");
		ScopedProxyTestBean bean = (ScopedProxyTestBean) context.getBean("scopedProxyTestBean");
		// should not be a proxy
		assertFalse(AopUtils.isAopProxy(bean));
	}

	public void testNoScopedProxy() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/scopedProxyNoTests.xml");
		ScopedProxyTestBean bean = (ScopedProxyTestBean) context.getBean("scopedProxyTestBean");
		// should not be a proxy
		assertFalse(AopUtils.isAopProxy(bean));
	}
	
	public void testInterfacesScopedProxy() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/scopedProxyInterfacesTests.xml");
		// should cast to the interface
		FooService bean = (FooService) context.getBean("scopedProxyTestBean");
		// should be dynamic proxy
		assertTrue(AopUtils.isJdkDynamicProxy(bean));
	}
	
	public void testTargetClassScopedProxy() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"org/springframework/context/annotation/scopedProxyTargetClassTests.xml");
		ScopedProxyTestBean bean = (ScopedProxyTestBean) context.getBean("scopedProxyTestBean");
		// should be a class-based proxy
		assertTrue(AopUtils.isCglibProxy(bean));
	}

	public void testInvalidConfigScopedProxy() {
		try {
			new ClassPathXmlApplicationContext(
					"org/springframework/context/annotation/scopedProxyInvalidConfigTests.xml");
			fail("should have thrown Exception; scope-resolver and scoped-proxy both provided");
		}
		catch (FatalBeanException e) {
			// expected
		}
	}
	
}
