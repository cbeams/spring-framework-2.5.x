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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public final class ParserContext {

	private final XmlReaderContext readerContext;

	private final BeanDefinitionParserDelegate delegate;

	private BeanDefinition containingBeanDefinition;

	private CompositeComponentDefinition containingComponent;


	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		this.readerContext = readerContext;
		this.delegate = delegate;
	}

	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate,
			BeanDefinition containingBeanDefinition) {

		this.readerContext = readerContext;
		this.delegate = delegate;
		this.containingBeanDefinition = containingBeanDefinition;
	}


	public XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	public BeanDefinitionRegistry getRegistry() {
		return getReaderContext().getRegistry();
	}

	public BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}

	public BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}

	public void setContainingComponent(CompositeComponentDefinition containingComponent) {
		this.containingComponent = containingComponent;
	}

	public CompositeComponentDefinition getContainingComponent() {
		return this.containingComponent;
	}

	public boolean isNested() {
		return (getContainingBeanDefinition() != null);
	}

	public Object extractSource(Object sourceCandidate) {
		return getReaderContext().extractSource(sourceCandidate);
	}

	public void registerComponent(ComponentDefinition component) {
		CompositeComponentDefinition containingComponent = getContainingComponent();
		if (containingComponent != null) {
			containingComponent.addNestedComponent(component);
		}
		else {
			getReaderContext().fireComponentRegistered(component);
		}
	}

}
