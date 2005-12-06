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

package org.springframework.beans.factory.xml.support;

import org.w3c.dom.Element;

/**
 * Base interface used by the {@link org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser}
 * handling custom namespaces in a Spring XML configuration file. Implementations are expected
 * to return implementations of the {@link BeanDefinitionParser} interface for custom top-level
 * tags and implementations of the {@link BeanDefinitionDecorator} interface for custom nested tags.
 * <p>The parser will call {@link #findParserForElement} when it encounters a custom tag directly
 * under the <code>&lt;beans&gt;</code> tags and {@link #findDecoratorForElement} when it encounters
 * a custom tag directly under a <code>&lt;bean&gt;</code> tag.
 *
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 2.0
 * @see org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser
 * @see NamespaceHandlerResolver
 */
public interface NamespaceHandler {

    /**
     * Find the {@link BeanDefinitionParser} for the specified {@link Element}.
     */
    BeanDefinitionParser findParserForElement(Element element);

    /**
     * Find the {@link BeanDefinitionDecorator} for the specified {@link Element}.
     */
    BeanDefinitionDecorator findDecoratorForElement(Element element);

}
