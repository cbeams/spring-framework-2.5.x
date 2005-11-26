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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * @author Rob Harrop
 */
public abstract class AbstractSingleBeanDefinitionParser implements BeanDefinitionParser {

    public static final String ID_ATTRIBUTE = "id";

    public final void parse(Element element, BeanDefinitionRegistry registry) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        BeanDefinition definition = doParse(element);
        registry.registerBeanDefinition(id, definition);
    }

    protected abstract BeanDefinition doParse(Element element);
}
