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

import junit.framework.TestCase;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.ITestBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.aop.support.AopUtils;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class ScopedProxyTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new DefaultListableBeanFactory();
	}

	public void testProxyAssignable_SPR2108() throws Exception {
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

	private void loadBeans(String path) {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
	}

}
