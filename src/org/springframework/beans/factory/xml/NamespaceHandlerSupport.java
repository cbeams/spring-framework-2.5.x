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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Support class for implementing custom {@link NamespaceHandler NamespaceHandlers}. Parsing and
 * decorating of individual {@link Node Nodes} is done via {@link BeanDefinitionParser} and
 * {@link BeanDefinitionDecorator} strategy interfaces respectively. Provides
 * {@link #registerBeanDefinitionParser} and {@link #registerBeanDefinitionDecorator} methods
 * for registering a {@link BeanDefinitionParser} or {@link BeanDefinitionDecorator} to handle
 * a specific element.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see #registerBeanDefinitionParser(String, BeanDefinitionParser)
 * @see #registerBeanDefinitionDecorator(String, BeanDefinitionDecorator)
 */
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

	/**
	 * Stores the {@link BeanDefinitionParser} implementations keyed by the
	 * local name of the {@link Element Elements} they handle.
	 */
	private final Map parsers = new HashMap();

	/**
	 * Stores the {@link BeanDefinitionDecorator} implementations keyed by the
	 * local name of the {@link Element Elements} they handle.
	 */
	private final Map decorators = new HashMap();

	/**
	 * Decorates the supplied {@link Node} by delegating to the {@link BeanDefinitionDecorator} that
	 * is registered to handle that {@link Node}.
	 */
	public final BeanDefinitionHolder decorate(Element element, BeanDefinitionHolder definition, ParserContext parserContext) {
		return findDecoratorForElement(element).decorate(element, definition, parserContext);
	}

	/**
	 * Parses the supplied {@link Element} by delegating to the {@link BeanDefinitionParser} that is
	 * registered for that {@link Element}.
	 */
	public final BeanDefinition parse(Element element, ParserContext parserContext) {
		return findParserForElement(element).parse(element, parserContext);
	}

	/**
	 * Locates the {@link BeanDefinitionParser} from the register implementations using
	 * the local name of the supplied {@link Element}.
	 */
	protected final BeanDefinitionParser findParserForElement(Element element) {
		BeanDefinitionParser parser = (BeanDefinitionParser) this.parsers.get(element.getLocalName());

		if (parser == null) {
			throw new IllegalArgumentException("Cannot locate BeanDefinitionParser for element [" +
							element.getLocalName() + "].");
		}

		return parser;
	}

	/**
	 * Locates the {@link BeanDefinitionParser} from the register implementations using
	 * the local name of the supplied {@link Element}.
	 */
	protected final BeanDefinitionDecorator findDecoratorForElement(Element element) {
		BeanDefinitionDecorator decorator = (BeanDefinitionDecorator) this.decorators.get(element.getLocalName());

		if (decorator == null) {
			throw new IllegalArgumentException("Cannot locate BeanDefinitionDecorator for element [" +
							element.getLocalName() + "].");
		}

		return decorator;
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionParser} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parsers.put(elementName, parser);
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator decorator) {
		this.decorators.put(elementName, decorator);
	}
}
