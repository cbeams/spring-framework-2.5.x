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

package org.springframework.aop.aspectj.autoproxy;

import junit.framework.TestCase;

import org.springframework.aop.config.NamespaceHandlerUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.ReaderContext;
import org.springframework.beans.factory.support.ReaderEventListener;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rob Harrop
 */
public class AspectJNamespaceHandlerTests extends TestCase {

	private ParserContext parserContext;

	private MockReaderEventListener readerEventListener = new MockReaderEventListener();

	private BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
	protected void setUp() throws Exception {
		SourceExtractor sourceExtractor = new SourceExtractor() {
			public Object extract(Object sourceCandidate) {
				return sourceCandidate;
			}
		};
		BeanDefinitionReader reader = new DummyBeanDefinitionReader();
		ReaderContext readerContext = new ReaderContext(reader, null, null, this.readerEventListener, sourceExtractor);

		this.parserContext = new ParserContext(readerContext, null, false);
	}

	public void testRegisterAutoProxyCreator() throws Exception {
		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());
	}

	public void testRegisterAspectJAutoProxyCreator() throws Exception {
		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect number of definitions registered", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("Incorrect APC class", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

	public void testRegisterAspectJAutoProxyCreatorWithExistingAutoProxyCreator() throws Exception {
		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals(1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect definition count", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("APC class not swicthed", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

	public void testRegisterAutoProxyCreatorWhenAspectJAutoProxyCreatorAlreadyExists() throws Exception {
		NamespaceHandlerUtils.registerAspectJAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals(1, registry.getBeanDefinitionCount());

		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(this.parserContext);
		assertEquals("Incorrect definition count", 1, registry.getBeanDefinitionCount());

		AbstractBeanDefinition definition = (AbstractBeanDefinition) registry.getBeanDefinition(NamespaceHandlerUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
		assertEquals("Incorrect APC class", AspectJInvocationContextExposingAdvisorAutoProxyCreator.class, definition.getBeanClass());
	}

	private static class MockReaderEventListener implements ReaderEventListener {

		private final List componentDefinitions = new ArrayList();

		public void componentRegistered(ComponentDefinition componentDefinition) {
			this.componentDefinitions.add(componentDefinition);
		}

		public ComponentDefinition[] getComponentDefinitions() {
			return (ComponentDefinition[]) componentDefinitions.toArray(new ComponentDefinition[componentDefinitions.size()]);
		}
	}

	private class DummyBeanDefinitionReader implements BeanDefinitionReader {

		public BeanDefinitionRegistry getBeanFactory() {
			return registry;
		}

		public ResourceLoader getResourceLoader() {
			return null;
		}

		public ClassLoader getBeanClassLoader() {
			return null;
		}

		public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
			return 0;
		}

		public int loadBeanDefinitions(Resource[] resources) throws BeanDefinitionStoreException {
			return 0;

		}

		public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
			return 0;

		}

		public int loadBeanDefinitions(String[] locations) throws BeanDefinitionStoreException {
			return 0;

		}
	}
}
