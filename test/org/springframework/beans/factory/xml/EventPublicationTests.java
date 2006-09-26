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

package org.springframework.beans.factory.xml;

import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanComponentDefinition;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MapBasedReaderEventListener;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class EventPublicationTests extends TestCase {

	private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

	private final MapBasedReaderEventListener eventListener = new MapBasedReaderEventListener();


	protected void setUp() throws Exception {
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.setEventListener(this.eventListener);
		reader.loadBeanDefinitions(new ClassPathResource("beanEvents.xml", getClass()));
	}

	public void testBeanEventReceived() throws Exception {
		ComponentDefinition componentDefinition1 = this.eventListener.getComponentDefinition("testBean");
		assertTrue(componentDefinition1 instanceof BeanComponentDefinition);
		assertEquals(1, componentDefinition1.getBeanDefinitions().length);
		BeanDefinition beanDefinition1 = componentDefinition1.getBeanDefinitions()[0];
		assertEquals("Rob Harrop", beanDefinition1.getPropertyValues().getPropertyValue("name").getValue());
		assertEquals(1, componentDefinition1.getBeanReferences().length);
		assertEquals("testBean2", componentDefinition1.getBeanReferences()[0].getBeanName());
		assertEquals(0, componentDefinition1.getInnerBeanDefinitions().length);

		ComponentDefinition componentDefinition2 = this.eventListener.getComponentDefinition("testBean2");
		assertTrue(componentDefinition2 instanceof BeanComponentDefinition);
		assertEquals(1, componentDefinition1.getBeanDefinitions().length);
		BeanDefinition beanDefinition2 = componentDefinition2.getBeanDefinitions()[0];
		assertEquals("Juergen Hoeller", beanDefinition2.getPropertyValues().getPropertyValue("name").getValue());
		assertEquals(0, componentDefinition2.getBeanReferences().length);
		assertEquals(1, componentDefinition2.getInnerBeanDefinitions().length);
		BeanDefinition innerBd = componentDefinition2.getInnerBeanDefinitions()[0];
		assertEquals("Eva Schallmeiner", innerBd.getPropertyValues().getPropertyValue("name").getValue());
	}

	public void testAliasEventReceived() throws Exception {
		List aliases = this.eventListener.getAliases("testBean");
		assertEquals(2, aliases.size());
		assertTrue(aliases.contains("testBeanAlias1"));
		assertTrue(aliases.contains("testBeanAlias2"));
	}

	public void testImportEventReceived() throws Exception {
		List imports = this.eventListener.getImports();
		assertEquals(1, imports.size());
		assertTrue(imports.contains("beanEventsImported.xml"));
	}

}
