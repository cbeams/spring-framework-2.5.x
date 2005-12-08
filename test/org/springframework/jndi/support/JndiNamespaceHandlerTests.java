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

package org.springframework.jndi.support;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;

/**
 * @author Rob Harrop
 */
public class JndiNamespaceHandlerTests extends TestCase {

	private XmlBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("jndiNamespaceHandlerTests.xml", getClass()));
	}

	public void testSimpleDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("simple");
		assertEquals(JndiObjectFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "jndiName", "jdbc/MyDataSource");
		assertNull("Property resourceRef should not have been set", beanDefinition.getPropertyValues().getPropertyValue("resourceRef"));
	}

	public void testComplexDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("complex");
		assertEquals(JndiObjectFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "jndiName", "jdbc/MyDataSource");
		assertPropertyValue(beanDefinition, "resourceRef", "true");
		assertPropertyValue(beanDefinition, "cache", "true");
		assertPropertyValue(beanDefinition, "lookupOnStartup", "true");
		assertPropertyValue(beanDefinition, "expectedType", "com.myapp.DefaultFoo");
		assertPropertyValue(beanDefinition, "proxyInterface", "com.myapp.Foo");
	}

	public void testWithEnvironment() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("withEnvironment");
		assertPropertyValue(beanDefinition, "jndiEnvironment", "foo=bar");
	}

	private void assertPropertyValue(RootBeanDefinition beanDefinition, String propertyName, Object expectedValue) {
		assertEquals("Property [" + propertyName + "] incorrect.", expectedValue, beanDefinition.getPropertyValues().getPropertyValue(propertyName).getValue());
	}
}
