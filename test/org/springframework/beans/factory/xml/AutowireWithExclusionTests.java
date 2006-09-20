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

package org.springframework.beans.factory.xml;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.CountingFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public class AutowireWithExclusionTests extends TestCase {

	public void testByTypeAutowireWithAutoSelfExclusion() throws Exception {
		CountingFactory.reset();
		XmlBeanFactory beanFactory = getBeanFactory("autowire-with-exclusion.xml");
		beanFactory.preInstantiateSingletons();
		TestBean rob = (TestBean) beanFactory.getBean("rob");
		TestBean sally = (TestBean) beanFactory.getBean("sally");
		assertEquals(sally, rob.getSpouse());
		assertEquals(1, CountingFactory.getFactoryBeanInstanceCount());
	}

	public void testByTypeAutowireWithExclusion() throws Exception {
		CountingFactory.reset();
		XmlBeanFactory beanFactory = getBeanFactory("autowire-with-exclusion.xml");
		beanFactory.preInstantiateSingletons();
		TestBean rob = (TestBean) beanFactory.getBean("rob");
		assertEquals("props1", rob.getSomeProperties().getProperty("name"));
		assertEquals(1, CountingFactory.getFactoryBeanInstanceCount());
	}

	public void testConstructorAutowireWithAutoSelfExclusion() throws Exception {
		XmlBeanFactory beanFactory = getBeanFactory("autowire-constructor-with-exclusion.xml");
		TestBean rob = (TestBean) beanFactory.getBean("rob");
		TestBean sally = (TestBean) beanFactory.getBean("sally");
		assertEquals(sally, rob.getSpouse());
	}

	public void testConstructorAutowireWithExclusion() throws Exception {
		XmlBeanFactory beanFactory = getBeanFactory("autowire-constructor-with-exclusion.xml");
		TestBean rob = (TestBean) beanFactory.getBean("rob");
		assertEquals("props1", rob.getSomeProperties().getProperty("name"));
	}

	private XmlBeanFactory getBeanFactory(String configPath) {
		return new XmlBeanFactory(new ClassPathResource(configPath, getClass()));
	}

}
