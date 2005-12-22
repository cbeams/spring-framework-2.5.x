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

/**
 * Support class for implementing custom {@link NamespaceHandler NamespaceHandlers}. Provides
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

    // TODO: polish error handling

    /**
     * Locates the {@link BeanDefinitionParser} from the register implementations using
     * the local name of the supplied {@link Element}.
     */
    public final BeanDefinitionParser findParserForElement(Element element) {
        BeanDefinitionParser parser = (BeanDefinitionParser) this.parsers.get(element.getLocalName());

        if(parser == null) {
            throw new IllegalArgumentException("Cannot locate BeanDefinitionParser for element [" +
                    element.getLocalName() + "].");
        }

        return parser;
    }

    /**
     * Locates the {@link BeanDefinitionParser} from the register implementations using
     * the local name of the supplied {@link Element}.
     */
    public final BeanDefinitionDecorator findDecoratorForElement(Element element) {
        BeanDefinitionDecorator decorator = (BeanDefinitionDecorator) this.decorators.get(element.getLocalName());

        if(decorator == null) {
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
