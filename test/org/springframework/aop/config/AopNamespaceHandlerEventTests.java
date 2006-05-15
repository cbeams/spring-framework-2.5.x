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
import org.springframework.beans.factory.support.ReaderEventListener;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rob Harrop
 */
public class AopNamespaceHandlerEventTests extends TestCase {

	private MockReaderEventListener eventListener = new MockReaderEventListener();

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
		assertTrue(componentDefinitions[1] instanceof PointcutComponentDefinition);

		PointcutComponentDefinition pointcutComponentDefinition = (PointcutComponentDefinition) componentDefinitions[1];
		assertEquals("Incorrect number of BeanDefintions", 1, pointcutComponentDefinition.getBeanDefinitions().length);
	}

	public void testAdvisorEventsWithPointcutRef() throws Exception {
		loadBeansFrom("aopNamespaceHandlerAdvisorWithPointcutRefEventTests.xml");
		ComponentDefinition[] componentDefinitions = this.eventListener.getComponentDefinitions();
		assertEquals("Incorrect number of events fired", 4, componentDefinitions.length);
		AdvisorComponentDefinition acd = null;
		for (int i = 0; i < componentDefinitions.length; i++) {
			ComponentDefinition componentDefinition = componentDefinitions[i];
			if(componentDefinition instanceof AdvisorComponentDefinition) {
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
			if(componentDefinition instanceof AdvisorComponentDefinition) {
				acd = (AdvisorComponentDefinition) componentDefinition;
				break;
			}
		}

		assertNotNull("AdvisorComponentDefinition not found", acd);
		assertEquals(2, acd.getBeanDefinitions().length);
		assertEquals(1, acd.getBeanReferences().length);
	}

	public void testAspectEvent() throws Exception {
		//throw new UnsupportedOperationException("Test 'testAspectEvent' not implemented");
	}

	private void loadBeansFrom(String path) {
		this.reader.loadBeanDefinitions(new ClassPathResource(path, getClass()));
	}


	private static class MockReaderEventListener implements ReaderEventListener {

		private final List componentDefinitions = new ArrayList();

		public void componentRegistered(ComponentDefinition componentDefinition) {
			this.componentDefinitions.add(componentDefinition);
			System.out.println(componentDefinition.toString());
		}

		public ComponentDefinition[] getComponentDefinitions() {
			return (ComponentDefinition[]) componentDefinitions.toArray(new ComponentDefinition[componentDefinitions.size()]);
		}
	}
}
