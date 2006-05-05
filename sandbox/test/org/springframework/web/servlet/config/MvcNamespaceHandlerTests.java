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

package org.springframework.web.servlet.config;

import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Alef Arendsen
 * @since 2.0
 */
public class MvcNamespaceHandlerTests extends TestCase {
	
	public void testUrlMappingParserWithDefaults() {
		
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:org/springframework/web/servlet/config/urlMapping-defaults.xml");
		
		ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		String[] handlerNames = bf.getBeanNamesForType(SimpleUrlHandlerMapping.class);
		
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[0]);

		assertEquals("false", definition.getPropertyValues().getPropertyValue("urlDecode").getValue());
		assertEquals("false", definition.getPropertyValues().getPropertyValue("lazyInitHandlers").getValue());
		assertEquals("false", definition.getPropertyValues().getPropertyValue("alwaysUseFullPath").getValue());
	}
	
	public void testUrlMappingWithTwoMappings() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
		"classpath:org/springframework/web/servlet/config/urlMapping-attributes.xml");

		assertEquals(4, context.getBeanDefinitionNames().length);
		
		ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		String[] handlerNames = bf.getBeanNamesForType(SimpleUrlHandlerMapping.class);
		assertEquals(2, handlerNames.length);
		
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[0]);		
		checkHandlerMapping(definition);
		definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[1]);
		checkHandlerMapping(definition);
		
		context.getBean(handlerNames[0]);
		context.getBean(handlerNames[1]);
	}
	
	private void checkHandlerMapping(RootBeanDefinition definition) {
		
		assertEquals(SimpleUrlHandlerMapping.class, definition.getBeanClass());
		assertTrue(definition.getPropertyValues().contains("mappings"));
		Object orderObject = definition.getPropertyValues().getPropertyValue("order").getValue();
		assertTrue(orderObject instanceof Integer);
		int order = ((Integer)orderObject).intValue();		
		assertTrue(order == 0 || order == 1);
		
		if (order == 0) {
			Properties props = (Properties)definition.getPropertyValues().getPropertyValue("mappings").getValue();
			assertEquals(1, props.size());
			assertTrue(props.containsKey("/index.html"));
			assertEquals("myController", props.getProperty("/index.html"));
		} else if (order == 1) {
			Properties props = (Properties)definition.getPropertyValues().getPropertyValue("mappings").getValue();
			assertTrue(props.containsKey("/*.html"));
			assertEquals("myController", props.getProperty("/*.html"));
			assertTrue(props.containsKey("/**/*.html"));
			assertEquals("myController", props.getProperty("/**/*.html"));
		} else {
			fail("Order should be either 0 or 1");
		}
	}
	
	public void testUrlMappingWithInterceptor() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
			"classpath:org/springframework/web/servlet/config/urlMapping-attributes.xml");

		ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		String[] handlerNames = bf.getBeanNamesForType(SimpleUrlHandlerMapping.class);
		assertEquals(2, handlerNames.length);
		
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[0]);

		Object value = definition.getPropertyValues().getPropertyValue("interceptors").getValue();
		assertNotNull(value);
		assertTrue(value instanceof List);
		assertEquals(2, ((List)value).size());
		
		// see if configuration succeeds
		context.getBean(handlerNames[0]);
		
	}
	
	public void testUrlMappingParserWithoutDefaults() {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:org/springframework/web/servlet/config/urlMapping-attributes.xml");
		
		ConfigurableListableBeanFactory bf = (ConfigurableListableBeanFactory)context.getAutowireCapableBeanFactory();
		String[] handlerNames = bf.getBeanNamesForType(SimpleUrlHandlerMapping.class);
		
		RootBeanDefinition definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[0]);
		
		if (!"true".equals(definition.getPropertyValues().getPropertyValue("urlDecode").getValue())) {
			definition = (RootBeanDefinition)bf.getBeanDefinition(handlerNames[1]);
		}

		assertEquals("true", definition.getPropertyValues().getPropertyValue("urlDecode").getValue());
		assertEquals("true", definition.getPropertyValues().getPropertyValue("lazyInitHandlers").getValue());
		assertEquals("true", definition.getPropertyValues().getPropertyValue("alwaysUseFullPath").getValue());
		assertEquals(new Integer(0), definition.getPropertyValues().getPropertyValue("order").getValue());
		
		assertEquals("myController", definition.getPropertyValues().getPropertyValue("defaultHandler").getValue());
	}
	
	public static class MyController implements Controller {
		
		public MyController() {
			
		}
		
		public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
			return null;
		}
	}
	
	public static class MyInterceptor extends HandlerInterceptorAdapter {
		
		
	}
}


