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

package org.springframework.aop.config;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MapBasedReaderEventListener;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.io.ClassPathResource;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandlerEventTests extends TestCase {

	private MapBasedReaderEventListener eventListener = new MapBasedReaderEventListener();

	private XmlBeanDefinitionReader reader;

	private DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

	protected void setUp() throws Exception {
		this.reader = new XmlBeanDefinitionReader(this.beanFactory);
		this.reader.setEventListener(this.eventListener);
	}

	public void testPointcutEvents() throws Exception {
		loadBeansFrom("aopNamespaceHandlerPointcutEventTests.xml");
		ComponentDefinition[] componentDefinitions = this.eventListener.getComponentDefinitions();
		assertEquals("Incorrect number of events fired", 2, componentDefinitions.length);
		ComponentDefinition pcDefinition = this.eventListener.getComponentDefinition("myPointcut");
		assertTrue(pcDefinition instanceof PointcutComponentDefinition);

		PointcutComponentDefinition pointcutComponentDefinition = (PointcutComponentDefinition) pcDefinition;
		assertEquals("Incorrect number of BeanDefintions", 1, pointcutComponentDefinition.getBeanDefinitions().length);
	}

	public void testAdvisorEventsWithPointcutRef() throws Exception {
		loadBeansFrom("aopNamespaceHandlerAdvisorWithPointcutRefEventTests.xml");
		ComponentDefinition[] componentDefinitions = this.eventListener.getComponentDefinitions();
		assertEquals("Incorrect number of events fired", 4, componentDefinitions.length);
		AdvisorComponentDefinition acd = null;
		for (int i = 0; i < componentDefinitions.length; i++) {
			ComponentDefinition componentDefinition = componentDefinitions[i];
			if (componentDefinition instanceof AdvisorComponentDefinition) {
				acd = (AdvisorComponentDefinition) componentDefinition;
				break;
			}
		}

		assertNotNull("AdvisorComponentDefinition not found", acd);
		assertEquals(1, acd.getBeanDefinitions().length);
		assertEquals(2, acd.getBeanReferences().length);
	}

	public void testAdvisorEventsWithDirectPointcut() throws Exception {
		loadBeansFrom("aopNamespaceHandlerAdvisorWithDirectPointcutEventTests.xml");
		ComponentDefinition[] componentDefinitions = this.eventListener.getComponentDefinitions();
		assertEquals("Incorrect number of events fired", 3, componentDefinitions.length);
		AdvisorComponentDefinition acd = null;
		for (int i = 0; i < componentDefinitions.length; i++) {
			ComponentDefinition componentDefinition = componentDefinitions[i];
			if (componentDefinition instanceof AdvisorComponentDefinition) {
				acd = (AdvisorComponentDefinition) componentDefinition;
				break;
			}
		}

		assertNotNull("AdvisorComponentDefinition not found", acd);
		assertEquals(2, acd.getBeanDefinitions().length);
		assertEquals(1, acd.getBeanReferences().length);
	}

	public void testAspectEvent() throws Exception {
		loadBeansFrom("aopNamespaceHandlerAspectEventTests.xml");
		ComponentDefinition componentDefinition = this.eventListener.getComponentDefinition("countAgeCalls");
		assertNotNull(componentDefinition);
		BeanDefinition[] beanDefinitions = componentDefinition.getBeanDefinitions();
		assertEquals(6, beanDefinitions.length);
		RuntimeBeanReference[] beanReferences = componentDefinition.getBeanReferences();
		assertEquals(6, beanReferences.length);
		Set expectedReferences = new HashSet();
		expectedReferences.add("pc");
		expectedReferences.add("countingAdvice");
		for (int i = 0; i < beanReferences.length; i++) {
			RuntimeBeanReference beanReference = beanReferences[i];
			expectedReferences.remove(beanReference.getBeanName());
		}
		assertEquals("Incorrect references found", 0, expectedReferences.size());
	}

	private void loadBeansFrom(String path) {
		this.reader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
	}
}
