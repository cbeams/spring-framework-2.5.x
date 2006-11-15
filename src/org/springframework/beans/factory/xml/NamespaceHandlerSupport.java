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

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

/**
 * Support class for implementing custom {@link NamespaceHandler NamespaceHandlers}. Parsing and
 * decorating of individual {@link Node Nodes} is done via {@link BeanDefinitionParser} and
 * {@link BeanDefinitionDecorator} strategy interfaces respectively. Provides the
 * {@link #registerBeanDefinitionParser}, {@link #registerBeanDefinitionDecorator} methods
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
	 * Stores the {@link BeanDefinitionParser} implementations keyed by the local
	 * name of the {@link Attr Attrs} they handle.
	 */
	private final Map attributeDecorators = new HashMap();

	/**
	 * Decorates the supplied {@link Node} by delegating to the {@link BeanDefinitionDecorator} that
	 * is registered to handle that {@link Node}.
	 */
	public final BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {
		return findDecoratorForNode(node).decorate(node, definition, parserContext);
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
	 * the local name of the supplied {@link Node}. Supports both {@link Element Elements}
	 * and {@link Attr Attrs}.
	 */
	protected final BeanDefinitionDecorator findDecoratorForNode(Node node) {
		BeanDefinitionDecorator decorator = null;
		if (node instanceof Element) {
			decorator = (BeanDefinitionDecorator) this.decorators.get(node.getLocalName());
		}
		else if (node instanceof Attr) {
			decorator = (BeanDefinitionDecorator) this.attributeDecorators.get(node.getLocalName());
		}
		else {
			throw new IllegalArgumentException(
					"Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]");
		}

		if (decorator == null) {
			throw new IllegalArgumentException("Cannot locate BeanDefinitionDecorator for " +
					(node instanceof Element ? "element" : "attribute") + " [" + node.getLocalName() + "]");
		}

		return decorator;
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionParser} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parsers.put(elementName, parser);
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator decorator) {
		this.decorators.put(elementName, decorator);
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to
	 * handle the specified attribute. The attribute name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionDecoratorForAttribute(String attributeName, BeanDefinitionDecorator decorator) {
		this.attributeDecorators.put(attributeName, decorator);
	}

}
