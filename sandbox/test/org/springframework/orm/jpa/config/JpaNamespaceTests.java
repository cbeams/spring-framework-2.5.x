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

package org.springframework.orm.jpa.config;

import junit.framework.TestCase;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.TopLinkJpaVendorAdapter;

/**
 * @author Costin Leau
 * 
 */
public class JpaNamespaceTests extends TestCase {

	private XmlBeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource("jpaNamespaceHandlerTests.xml", getClass()));
	}

	public void testSimpleDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("simple");
		assertEquals(LocalEntityManagerFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "persistenceUnitName", "myPersistenceUnit");
	}

	public void testMediumDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("medium");
		assertEquals(LocalEntityManagerFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "persistenceUnitName", "anotherPU");
	}

	public void testComplexDefinition() throws Exception {
		RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory.getBeanDefinition("complex");
		assertEquals(LocalContainerEntityManagerFactoryBean.class, beanDefinition.getBeanClass());
		assertPropertyValue(beanDefinition, "persistenceUnitName", "customVendor");
		assertPropertyValue(beanDefinition, "persistenceXmlLocation", "META-INF/persistence.xml");
		assertPropertyValue(beanDefinition, "persistenceUnitRootLocation", "classpath:/");
		assertPropertyValue(beanDefinition, "loadTimeWeaver", "myBeanWeaver");

		AbstractBeanDefinition jpaAdapter = (AbstractBeanDefinition) beanDefinition.getPropertyValues().getPropertyValue(
				"jpaVendorAdapter").getValue();
		assertSame(TopLinkJpaVendorAdapter.class, jpaAdapter.getBeanClass());
	}

	private void assertPropertyValue(RootBeanDefinition beanDefinition, String propertyName, Object expectedValue) {
		assertEquals("Property [" + propertyName + "] incorrect.", expectedValue,
				beanDefinition.getPropertyValues().getPropertyValue(propertyName).getValue());
	}

}
