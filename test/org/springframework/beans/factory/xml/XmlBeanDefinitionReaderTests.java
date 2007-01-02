/*
 * Copyright 2002-2007 the original author or authors.
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
import org.xml.sax.InputSource;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 */
public class XmlBeanDefinitionReaderTests extends TestCase {

	public void testSetParserClassSunnyDay() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(factory).setDocumentReaderClass(DefaultBeanDefinitionDocumentReader.class);
	}

	public void testSetParserClassToNull() {
		try {
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			new XmlBeanDefinitionReader(factory).setDocumentReaderClass(null);
			fail("Should have thrown IllegalArgumentException (null parserClass)");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testSetParserClassToUnsupportedParserType() throws Exception {
		try {
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			new XmlBeanDefinitionReader(factory).setDocumentReaderClass(String.class);
			fail("Should have thrown IllegalArgumentException (unsupported parserClass)");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testWithOpenInputStream() {
		try {
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			Resource resource = new InputStreamResource(getClass().getResourceAsStream("test.xml"));
			new XmlBeanDefinitionReader(factory).loadBeanDefinitions(resource);
			fail("Should have thrown BeanDefinitionStoreException (can't determine validation mode)");
		}
		catch (BeanDefinitionStoreException expected) {
		}
	}

	public void testWithOpenInputStreamAndExplicitValidationMode() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		Resource resource = new InputStreamResource(getClass().getResourceAsStream("test.xml"));
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_DTD);
		reader.loadBeanDefinitions(resource);
	}

	public void testWithInputSource() {
		try {
			DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
			InputSource resource = new InputSource(getClass().getResourceAsStream("test.xml"));
			new XmlBeanDefinitionReader(factory).loadBeanDefinitions(resource);
			fail("Should have thrown BeanDefinitionStoreException (can't determine validation mode)");
		}
		catch (BeanDefinitionStoreException expected) {
		}
	}
	                                                                           
	public void testWithInputSourceAndExplicitValidationMode() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		InputSource resource = new InputSource(getClass().getResourceAsStream("test.xml"));
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_DTD);
		reader.loadBeanDefinitions(resource);
	}

	public void testWithFreshInputStream() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		Resource resource = new ClassPathResource("test.xml", getClass());
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(resource);
	}

	public void testDtdValidationAutodetect() throws Exception {
		doTestValidation("validateWithDtd.xml");
	}

	public void testXsdValidationAutodetect() throws Exception {
		doTestValidation("validateWithXsd.xml");
	}

	private void doTestValidation(String resourceName) throws Exception {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		Resource resource = new ClassPathResource(resourceName, getClass());
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(resource);
		TestBean bean = (TestBean) factory.getBean("testBean");
		assertNotNull(bean);
	}

}
