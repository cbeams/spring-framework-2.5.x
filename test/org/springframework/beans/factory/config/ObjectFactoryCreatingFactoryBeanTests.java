/*
 * Copyright 2002-2004 the original author or authors.
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

import java.util.Date;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for ObjectFactoryCreatingFactoryBean.
 * @author Colin Sampaleanu
 * @since 2004-05-11
 */
public class ObjectFactoryCreatingFactoryBeanTests extends TestCase {

	private BeanFactory beanFactory;

	protected void setUp() throws Exception {
		this.beanFactory = new XmlBeanFactory(new ClassPathResource(
				"ObjectFactoryCreatingFactoryBeanTests.xml", getClass()));
	}
	
	public void testBasicOperation() throws BeansException {
		TestBean testBean = (TestBean) beanFactory.getBean("testBean");
		ObjectFactory objectFactory = testBean.getObjectFactory();
		
		Date date1 = (Date) objectFactory.getObject();
		Date date2 = (Date) objectFactory.getObject();
		assertTrue(date1 != date2);
	}

	public static class TestBean {
		public ObjectFactory objectFactory;

		public ObjectFactory getObjectFactory() {
			return objectFactory;
		}

		public void setObjectFactory(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}
	}
}
