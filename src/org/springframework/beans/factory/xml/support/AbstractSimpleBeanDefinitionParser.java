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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.MutablePropertyValues;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
 * @author robh
 */
public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected final BeanDefinition doParse(Element element) {
        RootBeanDefinition definition = new RootBeanDefinition(getBeanClass(element));

        MutablePropertyValues mpvs = new MutablePropertyValues();
        NamedNodeMap attributes = element.getAttributes();
        for(int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();

            if(ID_ATTRIBUTE.equals(name)) {
                continue;
            }

            mpvs.addPropertyValue(attribute.getLocalName(), attribute.getValue());
        }
        definition.setPropertyValues(mpvs);
        return definition;
    }

    protected abstract Class getBeanClass(Element element);
}
