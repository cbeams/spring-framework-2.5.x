/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.aop.target.scope;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.RequestHolder;


public class HttpRequestAndSessionScopedProxyFactoryBeanTests extends TestCase {

	
	public void testPutBeanInRequest() throws Exception {
		String targetBeanName = "target";
		HttpServletRequest request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue("name", "abc");
		beanFactory.registerBeanDefinition(targetBeanName, bd);
		HttpRequestScopedProxyFactoryBean fb = new HttpRequestScopedProxyFactoryBean();
		fb.setTargetBeanName(targetBeanName);
		fb.setSessionKey(targetBeanName);
		fb.setBeanFactory(beanFactory);
		fb.afterPropertiesSet();
		Advised advised = (Advised)fb.getObject();
		TestBean target = (TestBean)advised.getTargetSource().getTarget();
		assertEquals("abc", target.getName());
		target = (TestBean)request.getAttribute(targetBeanName);
		assertEquals("abc", target.getName());
		RequestHolder.clear();
	}
	
	public void testPutBeanInSession() throws Exception {
		String targetBeanName = "target";
		HttpServletRequest request = new MockHttpServletRequest();
		RequestHolder.bind(request);
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, false);
		bd.getPropertyValues().addPropertyValue("name", "abc");
		beanFactory.registerBeanDefinition(targetBeanName, bd);
		HttpSessionScopedProxyFactoryBean fb = new HttpSessionScopedProxyFactoryBean();
		fb.setTargetBeanName(targetBeanName);
		fb.setSessionKey(targetBeanName);
		fb.setBeanFactory(beanFactory);
		fb.afterPropertiesSet();
		Advised advised = (Advised)fb.getObject();
		TestBean target = (TestBean)advised.getTargetSource().getTarget();
		assertEquals("abc", target.getName());
		target = (TestBean)request.getSession().getAttribute(targetBeanName);
		assertEquals("abc", target.getName());
		RequestHolder.clear();
	}
}
