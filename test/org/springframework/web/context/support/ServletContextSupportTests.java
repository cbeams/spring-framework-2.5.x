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

package org.springframework.web.context.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.mock.web.MockServletContext;

/**
 * Tests for various ServletContext-related support classes.
 * @author Juergen Hoeller
 * @since 22.12.2004
 */
public class ServletContextSupportTests extends TestCase {

	public void testServletContextFactoryBean() {
		MockServletContext sc = new MockServletContext();

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		MutablePropertyValues pvs = new MutablePropertyValues();
		wac.registerSingleton("servletContext", ServletContextFactoryBean.class, pvs);
		wac.refresh();

		Object value = wac.getBean("servletContext");
		assertEquals(sc, value);
	}

	public void testServletContextAttributeFactoryBean() {
		MockServletContext sc = new MockServletContext();
		sc.setAttribute("myAttr", "myValue");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("attributeName", "myAttr");
		wac.registerSingleton("importedAttr", ServletContextAttributeFactoryBean.class, pvs);
		wac.refresh();

		Object value = wac.getBean("importedAttr");
		assertEquals("myValue", value);
	}

	public void testServletContextAttributeFactoryBeanWithAttributeNotFound() {
		MockServletContext sc = new MockServletContext();

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("attributeName", "myAttr");
		wac.registerSingleton("importedAttr", ServletContextAttributeFactoryBean.class, pvs);

		try {
			wac.refresh();
			fail("Should have thrown BeanCreationException");
		}
		catch (BeanCreationException ex) {
			// expected
			assertTrue(ex.getCause() instanceof IllegalStateException);
			assertTrue(ex.getCause().getMessage().indexOf("myAttr") != -1);
		}
	}

	public void testServletContextAttributeExporter() {
		TestBean tb = new TestBean();
		Map attributes = new HashMap();
		attributes.put("attr1", "value1");
		attributes.put("attr2", tb);

		MockServletContext sc = new MockServletContext();
		ServletContextAttributeExporter exporter = new ServletContextAttributeExporter();
		exporter.setAttributes(attributes);
		exporter.setServletContext(sc);

		assertEquals("value1", sc.getAttribute("attr1"));
		assertSame(tb, sc.getAttribute("attr2"));
	}

	public void testServletContextPropertyPlaceholderConfigurer() {
		MockServletContext sc = new MockServletContext();
		sc.addInitParameter("key4", "mykey4");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "${key4}name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		wac.registerSingleton("tb1", TestBean.class, pvs);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, null);
		wac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my");
		wac.registerSingleton("configurer", ServletContextPropertyPlaceholderConfigurer.class, pvs);

		wac.refresh();

		TestBean tb1 = (TestBean) wac.getBean("tb1");
		TestBean tb2 = (TestBean) wac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals("mykey4namemyvarmyvar${", tb1.getName());
		assertEquals(tb2, tb1.getSpouse());
	}

	public void testServletContextPropertyPlaceholderConfigurerWithLocalOverriding() {
		MockServletContext sc = new MockServletContext();
		sc.addInitParameter("key4", "mykey4");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "${key4}name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		wac.registerSingleton("tb1", TestBean.class, pvs);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, null);
		wac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my\nkey4=yourkey4");
		wac.registerSingleton("configurer", ServletContextPropertyPlaceholderConfigurer.class, pvs);

		wac.refresh();

		TestBean tb1 = (TestBean) wac.getBean("tb1");
		TestBean tb2 = (TestBean) wac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals("yourkey4namemyvarmyvar${", tb1.getName());
		assertEquals(tb2, tb1.getSpouse());
	}

	public void testServletContextPropertyPlaceholderConfigurerWithContextOverride() {
		MockServletContext sc = new MockServletContext();
		sc.addInitParameter("key4", "mykey4");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "${key4}name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		wac.registerSingleton("tb1", TestBean.class, pvs);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, null);
		wac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my\nkey4=yourkey4");
		pvs.addPropertyValue("contextOverride", Boolean.TRUE);
		wac.registerSingleton("configurer", ServletContextPropertyPlaceholderConfigurer.class, pvs);

		wac.refresh();

		TestBean tb1 = (TestBean) wac.getBean("tb1");
		TestBean tb2 = (TestBean) wac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals("mykey4namemyvarmyvar${", tb1.getName());
		assertEquals(tb2, tb1.getSpouse());
	}

	public void testServletContextPropertyPlaceholderConfigurerWithContextOverrideAndAttributes() {
		MockServletContext sc = new MockServletContext();
		sc.addInitParameter("key4", "mykey4");
		sc.setAttribute("key4", "attrkey4");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "${key4}name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		wac.registerSingleton("tb1", TestBean.class, pvs);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, null);
		wac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my\nkey4=yourkey4");
		pvs.addPropertyValue("contextOverride", Boolean.TRUE);
		pvs.addPropertyValue("searchContextAttributes", Boolean.TRUE);
		wac.registerSingleton("configurer", ServletContextPropertyPlaceholderConfigurer.class, pvs);

		wac.refresh();

		TestBean tb1 = (TestBean) wac.getBean("tb1");
		TestBean tb2 = (TestBean) wac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals("attrkey4namemyvarmyvar${", tb1.getName());
		assertEquals(tb2, tb1.getSpouse());
	}

	public void testServletContextPropertyPlaceholderConfigurerWithAttributes() {
		MockServletContext sc = new MockServletContext();
		sc.addInitParameter("key4", "mykey4");

		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		wac.registerSingleton("tb1", TestBean.class, pvs);

		ConstructorArgumentValues cas = new ConstructorArgumentValues();
		cas.addIndexedArgumentValue(1, "${age}");
		cas.addGenericArgumentValue("${var}name${age}");

		pvs = new MutablePropertyValues();
		List friends = new ManagedList();
		friends.add("na${age}me");
		friends.add(new RuntimeBeanReference("${ref}"));
		pvs.addPropertyValue("friends", friends);

		Set someSet = new ManagedSet();
		someSet.add("na${age}me");
		someSet.add(new RuntimeBeanReference("${ref}"));
		pvs.addPropertyValue("someSet", someSet);

		Map someMap = new ManagedMap();
		someMap.put("key1", new RuntimeBeanReference("${ref}"));
		someMap.put("key2", "${age}name");
		MutablePropertyValues innerPvs = new MutablePropertyValues();
		innerPvs.addPropertyValue("touchy", "${os.name}");
		someMap.put("key3", new RootBeanDefinition(TestBean.class, innerPvs));
		MutablePropertyValues innerPvs2 = new MutablePropertyValues(innerPvs);
		someMap.put("${key4}", new BeanDefinitionHolder(new ChildBeanDefinition("tb1", innerPvs2), "child"));
		pvs.addPropertyValue("someMap", someMap);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, cas, pvs);
		wac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "var=${m}var\nref=tb2\nm=my");
		pvs.addPropertyValue("searchContextAttributes", Boolean.TRUE);
		wac.registerSingleton("configurer", ServletContextPropertyPlaceholderConfigurer.class, pvs);
		sc.setAttribute("age", new Integer(98));

		wac.refresh();

		TestBean tb1 = (TestBean) wac.getBean("tb1");
		TestBean tb2 = (TestBean) wac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals(98, tb2.getAge());
		assertEquals("namemyvarmyvar${", tb1.getName());
		assertEquals("myvarname98", tb2.getName());
		assertEquals(tb2, tb1.getSpouse());
		assertEquals(2, tb2.getFriends().size());
		assertEquals("na98me", tb2.getFriends().iterator().next());
		assertEquals(tb2, ((List) tb2.getFriends()).get(1));
		assertEquals(2, tb2.getSomeSet().size());
		assertTrue(tb2.getSomeSet().contains("na98me"));
		assertTrue(tb2.getSomeSet().contains(tb2));
		assertEquals(4, tb2.getSomeMap().size());
		assertEquals(tb2, tb2.getSomeMap().get("key1"));
		assertEquals("98name", tb2.getSomeMap().get("key2"));
		TestBean inner1 = (TestBean) tb2.getSomeMap().get("key3");
		TestBean inner2 = (TestBean) tb2.getSomeMap().get("mykey4");
		assertEquals(0, inner1.getAge());
		assertEquals(null, inner1.getName());
		assertEquals(System.getProperty("os.name"), inner1.getTouchy());
		assertEquals(98, inner2.getAge());
		assertEquals("namemyvarmyvar${", inner2.getName());
		assertEquals(System.getProperty("os.name"), inner2.getTouchy());
	}

}
