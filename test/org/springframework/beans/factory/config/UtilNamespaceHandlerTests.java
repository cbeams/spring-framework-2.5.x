/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Properties;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MapBasedReaderEventListener;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.TestBean;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 */
public class UtilNamespaceHandlerTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	private MapBasedReaderEventListener listener = new MapBasedReaderEventListener();

	public void setUp() {
		this.beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.setEventListener(this.listener);
		reader.loadBeanDefinitions(new ClassPathResource("testUtilNamespace.xml", getClass()));
	}

	public void testLoadProperties() throws Exception {
		Properties props = (Properties) this.beanFactory.getBean("myProperties");
		assertEquals("Incorrect property value", "bar", props.get("foo"));
	}

	public void testConstant() throws Exception {
		Integer min = (Integer) this.beanFactory.getBean("min");
		assertEquals(Integer.MIN_VALUE, min.intValue());
	}

	public void testEvents() throws Exception {
		ComponentDefinition propertiesComponent = this.listener.getComponentDefinition("myProperties");
		assertNotNull("Event for 'myProperties' not sent", propertiesComponent);
		AbstractBeanDefinition propertiesBean = (AbstractBeanDefinition) propertiesComponent.getBeanDefinitions()[0];
		assertEquals("Incorrect BeanDefinition", PropertiesFactoryBean.class, propertiesBean.getBeanClass());

		ComponentDefinition constantComponent = this.listener.getComponentDefinition("min");
		assertNotNull("Event for 'min' not sent", propertiesComponent);
		AbstractBeanDefinition constantBean = (AbstractBeanDefinition) constantComponent.getBeanDefinitions()[0];
		assertEquals("Incorrect BeanDefinition", FieldRetrievingFactoryBean.class, constantBean.getBeanClass());
	}

	public void testNestedProperties() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		Properties props = bean.getSomeProperties();
		assertEquals("Incorrect property value", "bar", props.get("foo"));
	}

	public void testPropertyPath() throws Exception {
		String name = (String) this.beanFactory.getBean("name");
		assertEquals("Rob Harrop", name);
	}

	public void testNestedPropertyPath() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("testBean");
		assertEquals("Rob Harrop", bean.getName());
	}

	public void testSimpleMap() throws Exception {
		Map map = (Map) this.beanFactory.getBean("simpleMap");
		assertEquals("bar", map.get("foo"));
	}

	public void testSimpleList() throws Exception {
		List list = (List) this.beanFactory.getBean("simpleList");
		assertEquals("Rob Harrop", list.get(0));
	}

	public void testSimpleSet() throws Exception {
		Set list = (Set) this.beanFactory.getBean("simpleSet");
		assertTrue(list.contains("Rob Harrop"));
	}

	public void testMapWithRef() throws Exception {
		Map map = (Map) this.beanFactory.getBean("mapWithRef");
		assertTrue(map instanceof TreeMap);
		assertEquals(this.beanFactory.getBean("testBean"), map.get("bean"));
	}

	public void testNestedInCollections() throws Exception {
		TestBean bean = (TestBean) this.beanFactory.getBean("nestedCustomTagBean");

		Integer min = new Integer(Integer.MIN_VALUE);

		Map map = bean.getSomeMap();
		assertEquals(min, map.get("min"));

		List list = bean.getSomeList();
		assertEquals(min, list.get(0));

		Set set = bean.getSomeSet();
		assertTrue(set.contains(min));
	}
}
