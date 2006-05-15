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

package org.springframework.context.support;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.ResourceTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

/**
 * @author Juergen Hoeller
 */
public class ClassPathXmlApplicationContextTests extends TestCase {

	public void testSingleConfigLocation() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/simpleContext.xml");
		assertTrue(ctx.containsBean("someMessageSource"));
	}

	public void testMultipleConfigLocations() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {
					"/org/springframework/context/support/contextB.xml",
					"/org/springframework/context/support/contextC.xml",
					"/org/springframework/context/support/contextA.xml"});
		assertTrue(ctx.containsBean("service"));
		assertTrue(ctx.containsBean("logicOne"));
		assertTrue(ctx.containsBean("logicTwo"));
	}

	public void testConfigLocationPattern() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/context*.xml");
		assertTrue(ctx.containsBean("service"));
		assertTrue(ctx.containsBean("logicOne"));
		assertTrue(ctx.containsBean("logicTwo"));
	}

	public void testSingleConfigLocationWithClass() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"simpleContext.xml", getClass());
		assertTrue(ctx.containsBean("someMessageSource"));
	}

	public void testContextWithInvalidLazyClass() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"invalidClass.xml", getClass());
		assertTrue(ctx.containsBean("someMessageSource"));
		try {
			ctx.getBean("someMessageSource");
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			assertTrue(ex.contains(ClassNotFoundException.class));
		}
	}

	public void testContextWithClassNameThatContainsPlaceholder() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classWithPlaceholder.xml", getClass());
		assertTrue(ctx.containsBean("someMessageSource"));
		ctx.getBean("someMessageSource");
	}

	public void testMultipleConfigLocationsWithClass() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {"contextB.xml", "contextC.xml", "contextA.xml"}, getClass());
		assertTrue(ctx.containsBean("service"));
		assertTrue(ctx.containsBean("logicOne"));
		assertTrue(ctx.containsBean("logicTwo"));
	}

	public void testFactoryBeanAndApplicationListener() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/context*.xml");
		ctx.getBeanFactory().registerSingleton("manualFBAAL", new FactoryBeanAndApplicationListener());
		assertEquals(2, ctx.getBeansOfType(ApplicationListener.class).size());
	}

	public void testMessageSourceAware() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/context*.xml");
		MessageSource messageSource = (MessageSource) ctx.getBean("messageSource");
		Service service1 = (Service) ctx.getBean("service");
		assertEquals(ctx, service1.getMessageSource());
		Service service2 = (Service) ctx.getBean("service2");
		assertEquals(ctx, service2.getMessageSource());
		AutowiredService autowiredService1 = (AutowiredService) ctx.getBean("autowiredService");
		assertEquals(messageSource, autowiredService1.getMessageSource());
		AutowiredService autowiredService2 = (AutowiredService) ctx.getBean("autowiredService2");
		assertEquals(messageSource, autowiredService2.getMessageSource());
	}

	public void testResourceArrayPropertyEditor() throws IOException {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/context*.xml");
		Service service = (Service) ctx.getBean("service");
		assertEquals(3, service.getResources().length);
		List resources = Arrays.asList(service.getResources());
		assertTrue(resources.contains(
		    new FileSystemResource(new ClassPathResource("/org/springframework/context/support/contextA.xml").getFile())));
		assertTrue(resources.contains(
		    new FileSystemResource(new ClassPathResource("/org/springframework/context/support/contextB.xml").getFile())));
		assertTrue(resources.contains(
		    new FileSystemResource(new ClassPathResource("/org/springframework/context/support/contextC.xml").getFile())));
	}

	public void testChildWithProxy() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"/org/springframework/context/support/context*.xml");
		ClassPathXmlApplicationContext child = new ClassPathXmlApplicationContext(
				new String[] {"/org/springframework/context/support/childWithProxy.xml"}, ctx);
		assertTrue(AopUtils.isAopProxy(child.getBean("assemblerOne")));
		assertTrue(AopUtils.isAopProxy(child.getBean("assemblerTwo")));
	}

	public void testSelfReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("test"));
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, pvs));
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(test, test.getSpouse());
	}

	public void testResourceAndInputStream() throws IOException {
		ClassPathXmlApplicationContext ctx =
		    new ClassPathXmlApplicationContext("/org/springframework/beans/factory/xml/resource.xml") {
			public Resource getResource(String location) {
				if ("classpath:org/springframework/beans/factory/xml/test.properties".equals(location)) {
					return new ClassPathResource("test.properties", ClassPathXmlApplicationContextTests.class);
				}
				return super.getResource(location);
			}
		};
		ResourceTestBean resource1 = (ResourceTestBean) ctx.getBean("resource1");
		ResourceTestBean resource2 = (ResourceTestBean) ctx.getBean("resource2");
		assertTrue(resource1.getResource() instanceof ClassPathResource);
		StringWriter writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource1.getResource().getInputStream()), writer);
		assertEquals("contexttest", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource1.getInputStream()), writer);
		assertEquals("contexttest", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource2.getResource().getInputStream()), writer);
		assertEquals("contexttest", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource2.getInputStream()), writer);
		assertEquals("contexttest", writer.toString());
	}

	public void testGenericApplicationContextWithXmlBeanDefinitions() {
		GenericApplicationContext ctx = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ctx);
		reader.loadBeanDefinitions(new ClassPathResource("contextB.xml", getClass()));
		reader.loadBeanDefinitions(new ClassPathResource("contextC.xml", getClass()));
		reader.loadBeanDefinitions(new ClassPathResource("contextA.xml", getClass()));
		ctx.refresh();
		assertTrue(ctx.containsBean("service"));
		assertTrue(ctx.containsBean("logicOne"));
		assertTrue(ctx.containsBean("logicTwo"));
	}

}
