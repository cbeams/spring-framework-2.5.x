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

package org.springframework.aop.scope;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ScopedProxyTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new DefaultListableBeanFactory();
	}

	/* SPR-2108 */
	public void testProxyAssignable() throws Exception {
		loadBeans("scopedMap.xml");
		Object baseMap = this.beanFactory.getBean("singletonMap");
		assertTrue(baseMap instanceof Map);
	}

	public void testSimpleProxy() throws Exception {
		loadBeans("scopedMap.xml");
		Object simpleMap = this.beanFactory.getBean("simpleMap");
		assertTrue(simpleMap instanceof Map);
		assertTrue(simpleMap instanceof HashMap);
	}

	public void testCreateJdkScopedProxy() throws Exception {
		loadBeans("scopedTestBean.xml");
		ITestBean bean = (ITestBean) this.beanFactory.getBean("testBean");
		assertNotNull(bean);
		assertTrue(AopUtils.isJdkDynamicProxy(bean));
	}

	public void testScopedList() {
		loadBeans("scopedList.xml");
		this.beanFactory.registerScope("request", new Scope() {
			public String getConversationId() {
				throw new UnsupportedOperationException();
			}
			public Object get(String name, ObjectFactory objectFactory) {
				throw new UnsupportedOperationException();
			}
			public Object remove(String name) {
				throw new UnsupportedOperationException();
			}
			public void registerDestructionCallback(String name, Runnable callback) {
				throw new UnsupportedOperationException();
			}
		});
		this.beanFactory.getBean("testBean");
	}

	private void loadBeans(String path) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
	}

}
