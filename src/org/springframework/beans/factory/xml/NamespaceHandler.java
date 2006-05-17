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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Base interface used by the {@link DefaultBeanDefinitionDocumentReader} handling custom namespaces
 * in a Spring XML configuration file. Implementations are expected to return implementations
 * of the {@link BeanDefinitionParser} interface for custom top-level
 * tags and implementations of the {@link BeanDefinitionDecorator} interface for custom nested tags.
 * <p/>
 * <p>The parser will call {@link #findParserForElement} when it encounters a custom tag directly
 * under the <code>&lt;beans&gt;</code> tags and {@link #findDecoratorForElement} when it encounters
 * a custom tag directly under a <code>&lt;bean&gt;</code> tag.
 *
 * @author Rob Harrop
 * @author Erik Wiersma
 * @see DefaultBeanDefinitionDocumentReader
 * @see NamespaceHandlerResolver
 * @since 2.0
 */
public interface NamespaceHandler {

	/**
	 * Invoked by the {@link DefaultBeanDefinitionDocumentReader} after construction but before
	 * any custom elements are parsed.
	 */
	void init();

	/**
	 * Parse the specified {@link Element} and register resulting <code>BeanDefinitions</code>
	 * with the {@link org.springframework.beans.factory.support.BeanDefinitionRegistry} embedded in the supplied {@link ParserContext}.
	 * <p>Implementations should return the primary <code>BeanDefinition</code> that results
	 * from the parse phase if they which to be used nested inside <code>&lt;property&gt;</code> tag.
	 * Implementations may return <code>null</code> if they will <strong>not</strong> be used in
	 * a nested scenario.
	 *
	 * @return the primary <code>BeanDefinition</code>
	 */
	BeanDefinition parse(Element element, ParserContext parserContext);

	/**
	 * Parse the specified {@link Node} and decorate the supplied <code>BeanDefinition</code>,
	 * returning the decorated definition. The {@link Node} may be either an {@link org.w3c.dom.Attr} or an
	 * {@link Element}.
	 * <p>Implementations may choose to return a completely new definition, which will replace
	 * the original definition in the resulting <code>BeanFactory</code>.
	 * <p>The supplied {@link ParserContext} can be used to register any additional beans
	 * needed to support the main definition.
	 */
	BeanDefinitionHolder decorate(Node element, BeanDefinitionHolder definition, ParserContext parserContext);

}
