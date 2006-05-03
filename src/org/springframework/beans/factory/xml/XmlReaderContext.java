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

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.ProblemReporter;
import org.springframework.beans.factory.support.ReaderContext;
import org.springframework.beans.factory.support.ReaderEventListener;
import org.springframework.beans.factory.support.SourceExtractor;
import org.springframework.core.io.Resource;

/**
 * Extension of {@link ReaderContext} specific to use with an {@link XmlBeanDefinitionReader}. Provides
 * access to the {@link NamespaceHandlerResolver} configured in the {@link XmlBeanDefinitionReader}.
 * @author Rob Harrop
 * @since 2.0
 */
public class XmlReaderContext extends ReaderContext {

	private final NamespaceHandlerResolver namespaceHandlerResolver;

	public XmlReaderContext(XmlBeanDefinitionReader reader, Resource resource,
													ProblemReporter problemReporter, ReaderEventListener eventListener,
													SourceExtractor sourceExtractor, NamespaceHandlerResolver namespaceHandlerResolver) {
		super(reader, resource, problemReporter, eventListener, sourceExtractor);
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}

	public NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return this.namespaceHandlerResolver;
	}
}
